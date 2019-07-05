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

import scala.xml.{ Elem, XML }

import org.scalatest.{ FlatSpec, Matchers }

import com.typesafe.config.{ 
  Config, ConfigFactory, ConfigResolveOptions, ConfigParseOptions }

class InheritAttributeSpec extends FlatSpec with Matchers {

  behavior of "writing of projects with inheritance attributes set"

  it should "be writable for XML" in {

    val xml = """<project 
child.project.url.inherit.append.path="false" 
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xmlns="http://maven.apache.org/POM/4.0.0">
      <modelVersion>4.0.0</modelVersion>
      <groupId>org.apache.maven</groupId>
      <artifactId>unbound</artifactId>
      <version>1.0.0</version>
      <packaging>jar</packaging>
      <name>Unbound</name>
      <scm child.scm.url.inherit.append.path="false" child.scm.developerConnection.inherit.append.path="false" child.scm.connection.inherit.append.path="false">
      </scm>
      <distributionManagement>
        <site child.site.url.inherit.append.path="false"></site>
      </distributionManagement>
    </project>
"""
    val correct = Project(
      false, "4.0.0", null, "org.apache.maven", "unbound", "1.0.0", "jar",
      "Unbound", scm = Scm(false, false, false),
      distributionManagement = DistributionManagement(site = Site(false)))

    val project1: Project = new Project(XML.loadString(correct.toXmlString))
    project1.toString should be (correct.toString)

    val project2: Project = new Project(XML.loadString(xml))
    project2.toString should be (correct.toString)
  }
}
