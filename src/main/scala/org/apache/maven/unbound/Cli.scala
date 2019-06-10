
package org.apache.maven.unbound

import java.io.{ File, FileWriter }

import scala.io.Source
import scala.xml.XML

import com.typesafe.config.ConfigFactory

object Cli {

  def createPomHoconFiles(at: String, project: Project): Unit = {

    val hocon = ConfigFactory.parseString(JsonWriter.writePOM(project))
    val writer = new FileWriter(new File(at + "/pom.conf"))
    try {
      writer.write(hocon.root().render())
      writer.flush()
    } finally {
      writer.close()
    }
    project.modules.foreach { module =>
      recurseXml(at + "/" + module, (s, p) => createPomHoconFiles(s, p)) }
  }

  def createPomJsonFiles(at: String, project: Project): Unit = {

    val jsonStr = JsonWriter.writePOM(project)
    val writer = new FileWriter(new File(at + "/pom.json"))
    try {
      writer.write(jsonStr)
      writer.flush()
    } finally {
      writer.close()

      project.modules.foreach { module => 
        recurseXml(at + "/" + module, (s, p) => createPomJsonFiles(s, p)) }
    }
  }

  def recurseXml(s: String, createFile: (String, Project) => Unit): Unit = {

    val xml = new File(s + "/pom.xml")

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
    val writer = new FileWriter(new File(at + "/pom.xml"))
    try {
      writer.write(xmlStr)
      writer.flush()
    } finally {
      writer.close()

      project.modules.foreach { module => recurse(at + "/" + module) }
    }
  }

  def recurse(s: String): Unit = {

    val conf = new File(s + "/pom.conf")
    val json = new File(s + "/pom.json")

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
