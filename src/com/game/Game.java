package com.game;

import javax.swing.JFrame;

import com.game.gfx.Screen;

public class Game implements Runnable {
	public static final int WIDTH = 640;
	public static final int HEIGHT = (WIDTH * 3) / 4;
	public static final String TITLE = "Raycast 2D Lighting Example";
	
	public static boolean running;
	
	public Screen screen;
	
	public Game() {
		running = false;
		screen = new Screen();
	}
	
	public void start() {
		if(running) return;
		running = true;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		while(running) {
			screen.update();
			screen.repaint();
		}
		
		dispose();
	}
	
	public void stop() {
		if(!running) return;
		running = false;
	}
	
	public void dispose() {
		
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame(TITLE);
		Game game = new Game();
		
		frame.add(game.screen);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setAlwaysOnTop(true);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.pack();
		
		game.start();
	}
}
