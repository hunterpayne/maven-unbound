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

import java.io._

import scala.xml.XML

import org.scalatest.{ FlatSpec, Matchers }

import com.typesafe.config.{ 
  Config, ConfigFactory, ConfigResolveOptions, ConfigParseOptions }

class WritingSpec extends FlatSpec with Matchers {

  behavior of "OutputStreams and Writers"

  it should "be writable for XML" in {

    val is = getClass().getClassLoader.getResourceAsStream("pom3-json.json")
    try {
      val project1: Project = JsonReader.readPOM(is)
      val sw = new StringWriter()
      project1.writePOM(sw)

      val xmlStr = sw.toString
      val project2 = new Project(XML.loadString(xmlStr))
      project2.toString should be (project1.toString)

      val baos = new ByteArrayOutputStream(xmlStr.size * 2)
      project2.writePOM(baos, "UTF-8")
      val xmlStr2 = baos.toString("UTF-8")

      val project3 = new Project(XML.loadString(xmlStr2))
      project3.toString should be (project1.toString)

    } finally {
      is.close()
    }    
  }

  it should "be writable for Json" in {

    val is = getClass().getClassLoader.getResourceAsStream("pom3-json.json")
    try {
      val project1: Project = JsonReader.readPOM(is)
      val sw = new StringWriter()
      JsonWriter.writePOM(project1, sw)

      val jsonStr = sw.toString
      val project2 = JsonReader.readPOM(jsonStr)
      project2.toString should be (project1.toString)

      val baos = new ByteArrayOutputStream(jsonStr.size * 2)
      JsonWriter.writePOM(project2, baos, "UTF-8")
      val jsonStr2 = baos.toString("UTF-8")

      val project3 = JsonReader.readPOM(jsonStr2)
      project3.toString should be (project1.toString)

    } finally {
      is.close()
    }
  }

  /*
   // there is currently no case class to hocon writer
  it should "can be written to Hocon" in {

    val is = getClass().getClassLoader.getResourceAsStream("pom3-json.json")
    try {
      val project1: Project = HoconReader.readPOM(is)
      val sw = new StringWriter()
      project1.writePOM(sw)

      val xmlStr = sw.toString
      val project2 = new Project(XML.load(xmlStr))
      project2.toString should be (project1.toString)

      val baos = new ByteArrayOutputStream(xmlStr.size * 2)
      project2.writePOM(baos, "UTF-8")
      val xmlStr2 = baos.toString("UTF-8")

      val project3 = new Project(XML.load(xmlStr2))
      project3.toString should be (project1.toString)

    } finally {
      is.close()
    }    
  }
   */
}
