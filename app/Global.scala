import play.api._
import controllers.Application
import java.io.File
import org.orbisgis.core.workspace.CoreWorkspace;
import scala.collection.JavaConversions._
import java.util.HashMap;

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    // init the main (and only) loaded OrbisGIS workspace
    val c = new CoreWorkspace()
    c.setWorkspaceFolder("workspace")

    Application.loadStyles
    Application.wmsCt.init(c, Application.styles)
  }

  override def onStop(app: Application) {
    Application.wmsCt.destroy
  }
}
