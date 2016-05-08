# Description

This is a simple algorithm I wrote to cast shadows on 2D objects. It still has some bugs which need to be ironed
out, but for the most part, it works. This system was being created to be used as lighting for top-down perspective
games, but with some modification, could be used to create pseudo-3D graphics, such as the original Wolfenstein 3D.

## Known Issues

- The main problem is that the rays that are cast are not 100% accurate. Sometimes they will "miss" the vertex they are aiming at and you will have pieces of objects that are not covered by the shadow mask.
