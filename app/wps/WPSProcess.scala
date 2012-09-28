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

import java.io.File
import org.gdms.data.DataSourceFactory
import org.gdms.data.importer.FileImportDefinition
import org.gdms.sql.engine.SQLScript
import org.gdms.sql.engine.Engine

case class WPSProcess(id: String, title: String, abstractText: String, script: SQLScript, inputs: List[String], outputs: List[String]) {
  
  def toShortXml() = <wps:Process wps:processVersion="1">
    <ows:Identifier>{ id }</ows:Identifier>
    <ows:Title>{ title }</ows:Title>
    <ows:Abstract>{ abstractText }</ows:Abstract>
  </wps:Process>

  def execute(inputData: List[(String, File)]): List[(String, File)] = {
  	val dsf = new DataSourceFactory
  	val sm = dsf.getSourceManager
  	inputData.map(i => sm.importFrom(i._1, new FileImportDefinition(i._2)))

  	script.setDataSourceFactory(dsf)
  	script.execute

  	val files = outputs.map{ o =>
  	  val f = File.createTempFile("wps-out-", ".json")
  	  sm.exportTo(o, f)
  	  (o,f)
  	}

  	sm.getSourceNames.map(sm.delete)
  	dsf.freeResources

  	files
  }
}