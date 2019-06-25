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

class ProjectSpec extends FlatSpec with Matchers {

  behavior of "case class defaults"

  it should "correct for Project" in {

    val correct = Project(
      true, "4.0.0", null, "group", "artifact", "version", "jar", 
      null, null, null, null,
      null, Seq[License](), Seq[Developer](), Seq[Contributor](), 
      Seq[MailingList](), Seq[String](), null, null, null, null, 
      Map[String, String](), Seq[Dependency](), Seq[Dependency](), 
      Seq[Repository](), Seq[Repository](), null, null, Seq[Profile]())
    Project(
      groupId = "group", artifactId = "artifact", 
      version = "version").toString should be(correct.toString)

    val xmlProject: Project = new Project(XML.loadString(
      """<project><groupId>group</groupId><artifactId>artifact</artifactId><version>version</version></project>"""))
    xmlProject.toString should be(correct.toString)

    val jsonProject: Project = JsonReader.readPOM(
      """{ "project" : { "groupId" : "group", "artifactId" : "artifact", "version" : "version" } }""")
    jsonProject.toString should be(correct.toString)

    val hoconProject = 
      HoconReader.readPOM(ConfigFactory.parseString(
        """{ project : { groupId : "group", artifactId : "artifact", version : "version" } }"""))
    hoconProject.toString should be(correct.toString)
  }

  it should "correct for Build and Resources" in {

    val res = new Resource(
      null, false, ".", Seq[String]("src/main/resources"), Seq[String]())
    val testRes = new Resource(
      null, false, ".", Seq[String]("src/test/resources"), Seq[String]())

    val correct = new Build(
      "src/main/java", "src/main/scripts", "src/test/java", 
      "target/classes", "target/test-classes",
      Seq[Extension](), null, 
      Seq(res), Seq(testRes),
      "target", null,
      Seq[String](), Seq[Plugin](), Seq[Plugin]())
    Build().toString should be(correct.toString)
  }
}
