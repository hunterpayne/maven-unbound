
package org.apache.maven.unbound

import java.io.{ File, FileWriter, Writer }

import scala.util.Try

import scala.xml.{ Elem, PrettyPrinter, XML }

trait Writeable {

  val xml: Elem

  // max width: 80 chars
  // indent:     2 spaces
  def toXmlString: String = {
    val printer = new PrettyPrinter(80, 2)
    val sb = new StringBuilder()
    printer.format(xml, sb)
    sb.toString()
  }

  def writePOM(writer: Writer): Unit = writer.write(toXmlString)

  def writePOM(filename: String): Unit = {
    val writer = new FileWriter(new File(filename))
    try { writePOM(writer) } finally { writer.close() }
    //XML.save(filename, toXmlString, "UTF-8", true, null)
  }
}
