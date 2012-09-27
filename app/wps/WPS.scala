/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package wps

import org.apache.commons.io.{FileUtils => FU, FilenameUtils => FNU}
import java.io.{File, FileOutputStream}
import scala.collection.mutable.{Map => MutMap}
import org.gdms.sql.engine._

class WPS {

  val processes = MutMap[String, WPSProcess]()

  val scriptFolder = new File("scripts")

  init()

  private def init() {
  	scriptFolder.mkdirs
  	scriptFolder.listFiles.foreach { f =>
  	  val str = FU.readFileToString(f)
      val p = parseScript(str)

  	  processes.put(p.id, p)
  	}
  }

  def addScript(str: String) {
    val p = parseScript(str)
    processes.put(p.id, p)
  }

  def removeScript(str: String) {
  	processes.get(str).map { w => 
  		val f = new File(scriptFolder, w.id + ".bsql")
  		f.delete
  		processes.remove(str)
  	}.getOrElse(sys.error("Unknown script: " + str))
  }

  private def parseScript(str: String): WPSProcess = {
    val sc = Engine.parseScript(str)

    val idS = str.indexOf("@identifier ") + 12
    val id = str.substring(idS, str.indexOf('\n', idS))

    val titleS = str.indexOf("@title ") + 7
    val title = str.substring(titleS, str.indexOf('\n', titleS))

    val abstractS = str.indexOf("@abstract ") + 10;
    val abstractText = str.substring(abstractS, str.indexOf("@/abstract", abstractS)).replace("--", "").trim

    var inpos: Int = str.indexOf("@input ")
    var inputs: List[String] = Nil
    while (inpos >=0) {
      val input = str.substring(inpos + 13, str.indexOf('\n', inpos + 7))
        inputs = input :: inputs
        inpos = str.indexOf("@input ", inpos + 8)
    }

    var outpos: Int = str.indexOf("@output ")
    var outputs: List[String] = Nil
    while (outpos >=0) {
      val output = str.substring(outpos + 14, str.indexOf('\n', outpos + 8))
        outputs = output :: outputs
        outpos = str.indexOf("@output ", outpos + 9)
    }

    val target = new File(scriptFolder, id + ".bsql")
    sc.save(new FileOutputStream(target))

    WPSProcess(id, title, abstractText, sc, inputs.reverse, outputs.reverse)
  }
}