package wps

import scala.xml.NodeSeq
import java.util.Date

sealed abstract class Status(time: Date) {
	def toXml: NodeSeq = <wps:Status creationTime={ time.toString }>{ internalXml }</wps:Status>
	def internalXml: NodeSeq
}

case class Succeeded(time: Date) extends Status(time) {
	def internalXml = <wps:ProcessSucceeded/>
}
case class Failed(time: Date) extends Status(time) {
	def internalXml = <wps:ProcessFailed/>
}