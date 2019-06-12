
package org.apache.maven.unbound

import java.io.{ File, FileWriter }

import scala.io.Source
import scala.xml.XML

import com.typesafe.config.{ ConfigFactory, ConfigRenderOptions }

object Cli {

  val xmlFileName = "/pom.xml"
  val hoconFileName = "/pom.conf"
  val jsonFileName = "/pom.json"

  def createPomHoconFiles(at: String, project: Project): Unit = {

    val hocon = ConfigFactory.parseString(JsonWriter.writePOM(project))
    val writer = new FileWriter(new File(at + hoconFileName))
    val options = ConfigRenderOptions.defaults().setOriginComments(false)
    try {
      writer.write(hocon.root().render(options))
      writer.flush()
      println(s"generated ${at}${hoconFileName}")
    } finally {
      writer.close()
    }
    project.modules.foreach { module =>
      recurseXml(at + "/" + module, (s, p) => createPomHoconFiles(s, p)) }
  }

  def createPomJsonFiles(at: String, project: Project): Unit = {

    val jsonStr = JsonWriter.writePrettyPOM(project)
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

    // generate json or hocon files
    if (args.forall { _.startsWith("--generate") }) {

      // read xml and generate json
      if (args.find(_ == "--generate-json").isDefined)
        recurseXml(".", (s, p) => createPomJsonFiles(s, p))

      // read xml and generate hocon
      if (args.find(_ == "--generate-hocon").isDefined)
        recurseXml(".", (s, p) => createPomHoconFiles(s, p))
    
      val unkn = args.find { s =>
        s != "--generate-hocon" && s != "--generate-json" }
      if (unkn.isDefined) {
        println(
          "can't generate with ${unkn.get}, supported types are hocon and json")
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
