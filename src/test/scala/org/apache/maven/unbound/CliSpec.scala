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

import java.io.{ File, FileReader }

import scala.xml.XML

import org.scalatest.{ FlatSpec, Matchers }

import com.typesafe.config.{ 
  Config, ConfigFactory, ConfigResolveOptions, ConfigParseOptions }

class CliSpec extends FlatSpec with Matchers {

  behavior of "the command line interface(CLI)"

  it should "transform XML pom files to Json" in {

    val xmlFile = new File("src/test/resources/pom.xml")
    val jsonFile = new File("src/test/resources/pom.json")

    Cli.recurseXml(
      "src/test/resources", (s, _, p) => Cli.createPomJsonFiles(s, p))
    val projectJson: Project = JsonReader.readPOM(new FileReader(jsonFile))
    val projectXml = new Project(XML.load(new FileReader(xmlFile)))
    projectJson.toString should be(projectXml.toString)
    projectJson.makeModelObject() // just to make sure it doesn't NPE
    if (jsonFile.exists()) jsonFile.delete()
  }

  it should "transform XML pom files to Hocon" in {

    val xmlFile = new File("src/test/resources/pom.xml")
    val hoconFile = new File("src/test/resources/pom.conf")

    Cli.recurseXml(
      "src/test/resources", (s, c, p) => Cli.createPomHoconFiles(s, c, p))
    val projectHocon: Project = HoconReader.readPOM(loadConfig(hoconFile))
    val projectXml = new Project(XML.load(new FileReader(xmlFile)))
    projectHocon.toString.replaceAllLiterally("Vector(", "List(") should be(
      projectXml.toString)
    projectHocon.makeModelObject() // just to make sure it doesn't NPE
    if (hoconFile.exists()) hoconFile.delete()
  }

  it should "transform Json pom files to XML" in {

    val xmlFile = new File("src/test/resources/json/pom.xml")
    val jsonFile = new File("src/test/resources/json/pom.json")

    Cli.recurse("src/test/resources/json")
    val projectJson: Project = JsonReader.readPOM(new FileReader(jsonFile))
    val projectXml = new Project(XML.load(new FileReader(xmlFile)))
    projectJson.toString should be(projectXml.toString)
    projectJson.makeModelObject() // just to make sure it doesn't NPE
    if (xmlFile.exists()) xmlFile.delete()
  }

  it should "transform Hocon pom files to XML" in {

    val xmlFile = new File("src/test/resources/hocon/pom.xml")
    val hoconFile = new File("src/test/resources/hocon/pom.conf")

    Cli.recurse("src/test/resources/hocon")
    val projectHocon: Project = HoconReader.readPOM(loadConfig(hoconFile))
    val projectXml = new Project(XML.load(new FileReader(xmlFile)))
    projectHocon.toString.replaceAllLiterally("Vector(", "List(") should be(
      projectXml.toString)
    projectHocon.makeModelObject() // just to make sure it doesn't NPE
    if (xmlFile.exists()) xmlFile.delete()
  }
}
