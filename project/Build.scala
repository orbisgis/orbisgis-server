import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "orbisgis-server"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "org.orbisgis.server" % "orbiswms-lib" % "1.0-SNAPSHOT" changing(),
      "com.sun.xml.bind" % "jaxb-impl" % "2.2.2",
      "com.kitfox.svg" % "svg-salamander" % "1.0" from "http://repo.orbisgis.org/com/kitfox/svg/svg-salamander/1.0/svg-salamander-1.0.jar",
      "javax.media" % "jai_core" % "1.1.3" from "http://repo.orbisgis.org/javax/media/jai_core/1.1.3/jai_core-1.1.3.jar",
      "junit" % "junit" % "4.10" % "test"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
       externalResolvers += "IRSTV" at "http://repo.orbisgis.org" ,
       externalResolvers += "Local repo" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
	   javacOptions ++= Seq("-source", "1.6"),
	   excludeFilter in unmanagedSources := "test",
	   unmanagedSourceDirectories in Test += file("app/wms/src/test/java")
    )
}
