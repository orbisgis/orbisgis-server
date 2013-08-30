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
4. Compile the maven project "mapcatalog" in mapcatalog repository of orbisgis-server with :

 ```
mvn clean install
```
5. Run `play` in the "play" repository of orbisgis-server to start the play console (which is an sbt console),
6. Check it finds the dependencies and compiles correctly with:

 ```
compile
```
6.a You can launch the h2 database client, to monitor the database with :

 ```
h2-browser
```
7. start it using:
 ```
start
```
8. go to [`http://localhost:9000/`](http://localhost:9000/) for the application home page.
 

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
