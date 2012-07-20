import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "orbisgis-server"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "org.orbisgis" % "orbisgis-core-monomap" % "4.0-SNAPSHOT" changing(),
      "com.kitfox.svg" % "svg-salamander" % "1.0" from "http://repo.orbisgis.org/com/kitfox/svg/svg-salamander/1.0/svg-salamander-1.0.jar",
      "javax.media" % "jai_core" % "1.1.3" from "http://repo.orbisgis.org/javax/media/jai_core/1.1.3/jai_core-1.1.3.jar"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
       resolvers ++= Seq( "IRSTV" at "http://repo.orbisgis.org",
        "Local repo" at "file://"+Path.userHome.absolutePath+"/.m2/repository"),
        unmanagedSourceDirectories in Compile <+= baseDirectory.apply(b => new File(b, "wms/src/main/java")),
	javacOptions ++= Seq("-source", "1.6")
    )
}
