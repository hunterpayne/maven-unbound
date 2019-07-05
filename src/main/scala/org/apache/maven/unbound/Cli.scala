/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.unbound

import java.io.{
  File, FileInputStream, FileOutputStream, FileWriter, OutputStreamWriter }

import scala.io.Source
import scala.xml.XML

import com.typesafe.config.{ ConfigFactory, ConfigRenderOptions }

/**
  * The Maven Unbound Command Line Interface(CLI).  It converts pom.json
  * and pom.conf files to pom.xml files and visa-versa.  It reads the parsed
  * files to extract the list of sub-modules to recurse down a heirarchy
  * performing the same conversions on all necessary modules of a multi-module
  * project.  Normally this class is only used when building a command line
  * interface but can be used in other cases as well.
  */
object Cli {

  val xmlFileName = "/pom.xml"
  val hoconFileName = "/pom.conf"
  val jsonFileName = "/pom.json"

  /**
    * Converts a project at path at from Hocon to XML by writing a new pom.xml
    * in at
    * @param at the path where we are writing the new pom.xml file
    * @param project the parsed form of the Project to write to disk
    */
  def createPomHoconFiles(at: String, project: Project): Unit = {

    val hocon = ConfigFactory.parseString(JsonWriter.writeConcisePOM(project))
    val writer = new OutputStreamWriter(
      new FileOutputStream(new File(at + hoconFileName)), "UTF-8")
    val options = ConfigRenderOptions.defaults().setOriginComments(false)
    try {
      writer.write(hocon.root().render(options))
      writer.flush()
      println(s"generated ${at}${hoconFileName} from ${at}${xmlFileName}")
    } finally {
      writer.close()
    }
    project.modules.foreach { module =>
      recurseXml(at + File.separator + module, (s, p) =>
        createPomHoconFiles(s, p)) }
  }

  /**
    * Converts a project at path at from Json to XML by writing a new pom.xml
    * in at
    * @param at the path where we are writing the new pom.xml file
    * @param project the parsed form of the Project to write to disk
    */
  def createPomJsonFiles(at: String, project: Project): Unit = {

    val writer = new FileWriter(new File(at + jsonFileName))
    try {
      JsonWriter.writePOM(project, writer)
      writer.flush()
      println(s"generated ${at}${jsonFileName} from ${at}${xmlFileName}")
    } finally {
      writer.close()

      project.modules.foreach { module =>
        recurseXml(at + File.separator + module, (s, p) =>
          createPomJsonFiles(s, p)) }
    }
  }

  /**
    * Recurses down a multi-module Maven project converting XML
    * POM files into Hocon or Json
    * @param s the path where to look for POM files
    * @param createFile a function to converts XML POM files into something
    * else and writing those new files to disk in the directory specified by s
    */
  def recurseXml(s: String, createFile: (String, Project) => Unit): Unit = {

    val xml = new File(s + xmlFileName)

    if (xml.exists()) {

      val xmlIn = new FileInputStream(xml)
      try {
        val project = new Project(XML.load(xmlIn))
        createFile(s, project)
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        xmlIn.close()
      }
    } else {

      println(s"no pom.json or pom.conf in ${s}")
    }
  }

  /**
    * Converts a project at path at from XML to something else by writing a
    * new POM file in some other format
    * @param at the path where we are writing the new POM file
    * @param project the parsed form of the Project to write to disk
    * @param form the type of file to write
    */
  def createPomXmlFiles(at: String, project: Project, from: String): Unit = {

    val writer = new FileWriter(at + xmlFileName)
    try {
      project.writePOM(writer)
      writer.flush()
      println(s"generated ${at}${xmlFileName} from ${at}${from}")
    } finally {
      writer.close()
      project.modules.foreach { mod => recurse(at + File.separator + mod) }
    }
  }

  /**
    * Recurses down a multi-module Maven project converting Hocon or Json
    * POM files into XML.
    * @param s the path where to look for POM files
    */
  def recurse(s: String): Unit = {

    val conf = new File(s + hoconFileName)
    val json = new File(s + jsonFileName)

    if (json.exists()) {

      val jsonStr = Source.fromFile(json).getLines.mkString
      val project = JsonReader.readPOM(jsonStr)
      createPomXmlFiles(s, project, jsonFileName)

    } else if (conf.exists()) {

      val project = HoconReader.readPOM(loadConfig(conf))
      createPomXmlFiles(s, project, hoconFileName)

    } else {

      println(s"no pom.json or pom.conf in ${s}")
    }
  }

  /** entry point of the CLI */
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
    }
  }
}
