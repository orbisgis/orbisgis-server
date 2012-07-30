package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import scala.util.control.Exception._
import java.net._
import java.io.File
import org.orbisgis.utils.FileUtils
import org.gdms.source.Source

object API extends Controller {

  private def buildInfo(s: Source) = {
    Map("name" -> Json.toJson(s.getName), "uri" -> Json.toJson(s.getURI.toString),
      "type" -> Json.toJson(s.getTypeName))
  }

  def listSources = Action {

    val maps = Application.sm.getSourceNames.view.map(Application.sm.getSource).
      filter(!_.isSystemTableSource).map(buildInfo)

    val json = Json.toJson(maps.map(Json.toJson(_)))

    Ok(json)
  }

  def registerSource = Action(parse.json) { request ⇒
    val name = (request.body \ "name").asOpt[String]
    val uri = (request.body \ "uri").asOpt[String].flatMap(a ⇒ catching(classOf[URISyntaxException]) opt new URI(a))

    uri.map { u ⇒
      (name.getOrElse(FileUtils.getFileNameWithoutExtensionU(new File(u.getPath))), u)
    }.map {
      case (n, u) ⇒
        if (Application.sm.exists(n)) {
          BadRequest("There already is a source with name " + n + ".")
        } else {
          Application.sm.register(n, u)
          Ok(Json.toJson(buildInfo(Application.sm.getSource(n))))
        }
    }.getOrElse {
      BadRequest("Malformed input json.")
    }
  }
  
  def deleteSource(name: String) = Action {
    if (Application.sm.exists(name)) {
      Application.sm.remove(name)
      NoContent
    } else {
      BadRequest("There is no source with name " + name + ".")
    }
  }
}