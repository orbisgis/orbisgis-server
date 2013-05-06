OrbisGIS Server
=====================================

An OrbisGIS-based server for OGC web services (WMS, WPS...).

## Build instructions

1. Install play 2.1.1,
2. Install Maven (latest version),
3. Compile the maven project "wms" in wms repository of orbisgis-server with :

 ```
mvn clean install
```
4. Run `play` in the "play" repository of orbisgis-server to start the play console (which is an sbt console),
5. Check it finds the dependencies and compiles correctly with:

 ```
compile
```
5. start it using:
 ```
start
```
5. go to [`http://localhost:9000/wms/manage`](http://localhost:9000/wms/manage) for the main management panel.
6. 
 

##Configuration instructions

Some things can be configured while launching the application. A default configuration file is embedded in the
application and can be found in the sources under conf/application.conf. If you want to change some 
of the variables present here, you can copy this file somewhere on your server and run it with 

```
start -Dconfig.file=path/to/application.conf
```

You'll find the available keys in the file stored in the sources. In particular, this file can be used to 
configure some things included in the GetCapabilities document answered by the WMS Server. Moreoever, log
verbosity can be set here too. If you change the configuration file, you have to restart the whole
application.
