import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "orbisgis-server-play"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.sun.xml.bind" % "jaxb-impl" % "2.2.5",
      "com.kitfox.svg" % "svg-salamander" % "1.0" from "http://repo.orbisgis.org/com/kitfox/svg/svg-salamander/1.0/svg-salamander-1.0.jar",
      "javax.media" % "jai_core" % "1.1.3" from "http://repo.orbisgis.org/javax/media/jai_core/1.1.3/jai_core-1.1.3.jar",
      "junit" % "junit" % "4.10" % "test",
      "org.orbisgis.server" % "orbiswms-lib" % "1.1-SNAPSHOT" changing(),
      "org.orbisgis.server" % "mapcatalog" % "2.5.3-SNAPSHOT" changing(),
      "com.typesafe" %% "play-plugins-mailer" % "2.1.0",
        javaCore
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
       externalResolvers += "IRSTV" at "http://repo.orbisgis.org",
       externalResolvers += "Local repo" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
	     javacOptions ++= Seq("-source", "1.6"),
       javaOptions in Test ++= Seq("-Dconfig.file=conf/test.conf"),
       //cannot test with this
	     //excludeFilter in unmanagedSources := "test",
	     unmanagedSourceDirectories in Test += file("app/wms/src/test/java")
    )
}