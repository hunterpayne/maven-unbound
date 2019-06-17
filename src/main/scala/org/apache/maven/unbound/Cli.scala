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

import java.io.{ File, FileInputStream, FileWriter }

import scala.io.Source
import scala.xml.XML

import com.typesafe.config.{ ConfigFactory, ConfigRenderOptions }

object Cli {

  val xmlFileName = "/pom.xml"
  val hoconFileName = "/pom.conf"
  val jsonFileName = "/pom.json"

  def createPomHoconFiles(at: String, project: Project): Unit = {

    val hocon = ConfigFactory.parseString(JsonWriter.writeConcisePOM(project))
    val writer = new FileWriter(new File(at + hoconFileName))
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

  def createPomJsonFiles(at: String, project: Project): Unit = {

    val jsonStr = JsonWriter.writePOM(project)
    val writer = new FileWriter(new File(at + jsonFileName))
    try {
      writer.write(jsonStr)
      writer.flush()
      println(s"generated ${at}${jsonFileName} from ${at}${xmlFileName}")
    } finally {
      writer.close()

      project.modules.foreach { module =>
        recurseXml(at + File.separator + module, (s, p) =>
          createPomJsonFiles(s, p)) }
    }
  }

  def recurseXml(s: String, createFile: (String, Project) => Unit): Unit = {

    val xml = new File(s + xmlFileName)

    if (xml.exists()) {

      val xmlIn = new FileInputStream(xml)
      try {
        val project = new Project(XML.load(xmlIn))
        createFile(s, project)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    } else {

      println(s"no pom.json or pom.conf in ${s}")
    }
  }

  def createPomXmlFiles(at: String, project: Project, from: String): Unit = {

    val xmlStr = project.toXmlString
    val writer = new FileWriter(new File(at + xmlFileName))
    try {
      writer.write(xmlStr)
      writer.flush()
      println(s"generated ${at}${xmlFileName} from ${at}${from}")
    } finally {
      writer.close()

      project.modules.foreach { module =>
        recurse(at + File.separator + module) }
    }
  }

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
