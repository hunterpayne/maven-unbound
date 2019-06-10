
package org.apache.maven.unbound

import java.io.{ File, FileWriter }

import scala.io.Source
import scala.xml.XML

import com.typesafe.config.ConfigFactory

object Cli {

  val xmlFileName = "/pom.xml"
  val hoconFileName = "/pom.conf"
  val jsonFileName = "/pom.json"

  def createPomHoconFiles(at: String, project: Project): Unit = {

    val hocon = ConfigFactory.parseString(JsonWriter.writePOM(project))
    val writer = new FileWriter(new File(at + hoconFileName))
    try {
      writer.write(hocon.root().render())
      writer.flush()
      println(s"generated ${at}${hoconFileName}")
    } finally {
      writer.close()
    }
    project.modules.foreach { module =>
      recurseXml(at + "/" + module, (s, p) => createPomHoconFiles(s, p)) }
  }

  def createPomJsonFiles(at: String, project: Project): Unit = {

    println("serializing project " + project)
    val jsonStr = JsonWriter.writePOM(project)
    val writer = new FileWriter(new File(at + jsonFileName))
    try {
      writer.write(jsonStr)
      writer.flush()
      println(s"generated ${at}${jsonFileName}")
    } finally {
      writer.close()

      project.modules.foreach { module => 
        recurseXml(at + "/" + module, (s, p) => createPomJsonFiles(s, p)) }
    }
  }

  def recurseXml(s: String, createFile: (String, Project) => Unit): Unit = {

    val xml = new File(s + xmlFileName)

    if (xml.exists()) {

      val xmlStr = Source.fromFile(xml).getLines.mkString
      val project = new Project(XML.loadString(xmlStr))
      createFile(s, project)

    } else {

      println(s"no pom.json or pom.conf in ${s}")
    }
  }

  def createPomXmlFiles(at: String, project: Project): Unit = {

    val xmlStr = project.toXmlString
    val writer = new FileWriter(new File(at + xmlFileName))
    try {
      writer.write(xmlStr)
      writer.flush()
      println(s"generated ${at}${xmlFileName}")
    } finally {
      writer.close()

      project.modules.foreach { module => recurse(at + "/" + module) }
    }
  }

  def recurse(s: String): Unit = {

    val conf = new File(s + hoconFileName)
    val json = new File(s + jsonFileName)

    if (json.exists()) {

      val jsonStr = Source.fromFile(json).getLines.mkString
      val project = JsonReader.readPOM(jsonStr)
      createPomXmlFiles(s, project)

    } else if (conf.exists()) {

      val project = HoconReader.readPOM(loadConfig(conf))
      createPomXmlFiles(s, project)

    } else {

      println(s"no pom.json or pom.conf in ${s}")
    }
  }

  def main(args: Array[String]): Unit = {

    if (args.size == 1 && args(0).startsWith("--generate")) {

      // generate json or hocon files
      if (args(0) == "--generate-json") {
        // read xml and generate json
        recurseXml(".", (s, p) => createPomJsonFiles(s, p))

      } else if (args(0) == "--generate-hocon") {
        // read xml and generate hocon
        recurseXml(".", (s, p) => createPomHoconFiles(s, p))

      } else {
        println(
          "can't generate that file type, supported types are hocon and json")
        println("use please use --generate-hocon or --generate-json")
      }
    } else {

      recurse(".")
      if (!args.isEmpty) {
        val cmd = args.mkString("mvn ", " ", "")

        import sys.process._
        cmd!
      }
    }
  }
}
