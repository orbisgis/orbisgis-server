import play.api._
import controllers._
import java.io.File
import org.orbisgis.core.workspace.CoreWorkspace;
import scala.collection.JavaConversions._
import java.util.HashMap;

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    // init the main (and only) loaded OrbisGIS workspace
    val c = new CoreWorkspace()
    c.setWorkspaceFolder("workspace")

    WMS.loadStyles
    WMS.wmsCt.init(c, WMS.styles, WMS.sourceStyles)
  }

  override def onStop(app: Application) {
    WMS.wmsCt.destroy
  }
}
