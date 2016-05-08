package com.game.gfx;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import com.game.Game;

public class Screen extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public static Point mousePos;
	
	private List<Polygon> obstacles;
	private List<Point2D.Double> rays;
	
	public Screen() {
		Dimension d = new Dimension(Game.WIDTH, Game.HEIGHT);
		
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
		
		mousePos = new Point();
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent m) {
				mousePos = m.getPoint();
				{ // Create rays from mouse position to each polygon vertex and screen corner.
					rays.clear();
					
					for(Polygon p : obstacles) {
						for(int v = 0; v < p.npoints; v++) {
							Point2D.Double target = new Point2D.Double(p.xpoints[v], p.ypoints[v]);
							double theta = Math.atan2((target.y - mousePos.y), (target.x - mousePos.x));
							raycast(theta);
							
							// Also send out rays +- 0.0000001 radians from the target theta to detect edges.
							raycast(theta + 0.08);
							raycast(theta - 0.08);
						}
					}
					
					// Cast rays towards the screen corners.
					raycast(Math.atan2(-mousePos.y, -mousePos.x));
					raycast(Math.atan2(-mousePos.y, (Game.WIDTH - mousePos.x)));
					raycast(Math.atan2((Game.HEIGHT - mousePos.y), (Game.WIDTH - mousePos.x)));
					raycast(Math.atan2((Game.HEIGHT - mousePos.y), -mousePos.x));
					// End screen corner ray casting.
					
					sortRays();
				} // End ray creation.
			}
		});
		
		obstacles = new ArrayList<>();
		rays = Collections.synchronizedList(new ArrayList<>());
		
		{ // Create test polygon for raycast.
			Polygon p1 = new Polygon();
			p1.addPoint(50, 50);
			p1.addPoint(80, 30);
			p1.addPoint(150, 55);
			p1.addPoint(130, 90);
			p1.addPoint(100, 100);
			obstacles.add(p1);
			
			Polygon p2 = new Polygon();
			p2.addPoint(200, 200);
			p2.addPoint(250, 200);
			p2.addPoint(250, 250);
			p2.addPoint(200, 250);
			obstacles.add(p2);
			
			Polygon p3 = new Polygon();
			p3.addPoint(400, 350);
			p3.addPoint(425, 300);
			p3.addPoint(450, 350);
			obstacles.add(p3);
			
			Polygon p4 = new Polygon();
			p4.addPoint(300, 100);
			p4.addPoint(550, 100);
			p4.addPoint(550, 150);
			p4.addPoint(300, 150);
			obstacles.add(p4);
		} // End polygon creation.
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setColor(Color.WHITE);
		g2d.clearRect(0, 0, Game.WIDTH, Game.HEIGHT);
		
		g2d.setColor(Color.BLUE);
		for(Polygon p : obstacles) {
			g2d.fill(p);
		}
		
		if(!rays.isEmpty()) { // Create the shadow mask.
			BufferedImage overlay = new BufferedImage(Game.WIDTH, Game.HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D og2d = overlay.createGraphics();
			og2d.setColor(Color.BLACK);
			og2d.clearRect(0, 0, Game.WIDTH, Game.HEIGHT);
			
			// Create a polygon from the endpoints of all the rays.
			Path2D.Double mask = new Path2D.Double();
			mask.moveTo(rays.get(0).x, rays.get(0).y);
			for(Point2D.Double end : rays) {
				mask.lineTo(end.x, end.y);
			}
			mask.closePath();
			
			og2d.setColor(Color.WHITE);
			og2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.0f));
			og2d.fill(mask);
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
			g2d.drawImage(overlay, 0, 0, null);
		} // End creation of shadow mask.
		
//		synchronized(rays) {
//			g2d.setColor(Color.RED);
//			for(Point2D.Double end : rays) {
//				Line2D.Double line = new Line2D.Double(mousePos.x, mousePos.y, end.x, end.y);
//				g2d.draw(line);
//				g2d.fillOval((int)(end.x - 5), (int)(end.y - 5), 10, 10);
//			}
//		}
		
		g2d.setColor(Color.BLUE);
		g2d.fillOval((mousePos.x - 5), (mousePos.y - 5), 10, 10);
	}
	
	public void update() {
		
	}
	
	private void raycast(double theta) {
		Point2D.Double currLoc = new Point2D.Double(mousePos.x, mousePos.y);
		boolean validRay = true;
		
		while(validRay) {
			double x = currLoc.x + Math.cos(theta);
			double y = currLoc.y + Math.sin(theta);
			
			// Check to see if the ray has gone out of the window.
			if((x < 0) || (x >= Game.WIDTH) || 
			   (y < 0) || (y >= Game.HEIGHT)) {
				validRay = false;
			}
			
			// Check to see if the ray has collided with an object.
			for(Polygon c : obstacles) {
				if(c.contains(new Point2D.Double(x, y))) {
					validRay = false;
				}
			}
			
			if(validRay) {
				currLoc.x = x;
				currLoc.y = y;
			}
		}
		
		rays.add(new Point2D.Double(currLoc.x, currLoc.y));
	}
	
	private void sortRays() {
		// Fuck it, do a bubble sort.
		if(!rays.isEmpty()) {
			for(int i = 0; i < (rays.size() - 1); i++) {
				for(int j = (i + 1); j < rays.size(); j++) {
					double iTheta = Math.atan2((rays.get(i).y - mousePos.y), (rays.get(i).x - mousePos.x));
					double jTheta = Math.atan2((rays.get(j).y - mousePos.y), (rays.get(j).x - mousePos.x));
					if(jTheta < iTheta) {
						Point2D.Double temp = rays.get(i);
						rays.set(i, rays.get(j));
						rays.set(j, temp);
					}
				}
			}
		}
	}
}
