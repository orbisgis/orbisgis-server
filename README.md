OrbisGIS Server
=====================================

An OrbisGIS-based server for OGC web services (WMS, WPS...).

## Build instructions

1. Install play 2.0.2,
2. run `play` at the root of the repository to start the play console (which is an sbt console),
3. check it finds the dependencies and compiles correctly with:

 ```
compile
```
4. start it using:
 ```
start
```
5. go to [`http://localhost:9000/wms/manage`](http://localhost:9000/wms/manage) for the main management panel.