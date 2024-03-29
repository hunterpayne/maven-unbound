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

class IncludeSpec extends FlatSpec with Matchers {

  behavior of "Include feature of Hocon"

  it should "bring in values into project" in {

    val correct = Project(
      true, "4.0.0", null, "org.apache.maven", "unbound", "1.0.0", "jar",
      "Unbound")
    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project = HoconReader.readPOM(ConfigFactory.load(
      "pom-include", ConfigParseOptions.defaults(), resolveOpts))
    project.toString should be (correct.toString)
  }

  it should "merge complex poms" in {

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project = HoconReader.readPOM(ConfigFactory.load(
      "pom-include2", ConfigParseOptions.defaults(), resolveOpts))

    val is = getClass().getClassLoader.getResourceAsStream("pom-include2.xml")
    try {
      val project2 = new Project(XML.load(is))
      val removedVectors =
        project.toString.replaceAllLiterally("Vector(", "List(")
      val removedVectors2 =
        project2.toString.replaceAllLiterally("Vector(", "List(")
      removedVectors should be (removedVectors2)
    } finally {
      is.close()
    }
  }

  it should "merge overlaping poms" in {

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project = HoconReader.readPOM(ConfigFactory.load(
      "pom-include3", ConfigParseOptions.defaults(), resolveOpts))

    val is = getClass().getClassLoader.getResourceAsStream("pom-include3.xml")
    try {
      val project2 = new Project(XML.load(is))
      val removedVectors =
        project.toString.replaceAllLiterally("Vector(", "List(")
      val removedVectors2 =
        project2.toString.replaceAllLiterally("Vector(", "List(")
      removedVectors should be (removedVectors2)

    } finally {
      is.close()
    }
  }

  it should "merge more complex poms" in {

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project = HoconReader.readPOM(ConfigFactory.load(
      "pom-include4", ConfigParseOptions.defaults(), resolveOpts))

    val is = getClass().getClassLoader.getResourceAsStream("pom-include4.xml")
    try {
      val project2 = new Project(XML.load(is))
      val removedVectors =
        project.toString.replaceAllLiterally("Vector(", "List(")
      val removedVectors2 =
        project2.toString.replaceAllLiterally("Vector(", "List(")
      removedVectors should be (removedVectors2)

    } finally {
      is.close()
    }
  }
}
