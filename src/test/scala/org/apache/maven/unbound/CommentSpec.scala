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

import scala.io.Source
import scala.xml.{ Comment, Elem, XML }
import scala.xml.parsing.ConstructingParser

import com.typesafe.config.{
  Config, ConfigFactory, ConfigParseOptions, ConfigRenderOptions, ConfigResolveOptions }
import org.scalatest.{ FlatSpec, Matchers }

class CommentSpec extends FlatSpec with Matchers {

  def parseXmlString(s: Source): Elem =
    ConstructingParser.fromSource(s, true).document.docElem.asInstanceOf[Elem]

  behavior of "Comments"

  it should "parse from simple XML" in {

    val xmlStr = """<project><parent><!-- comment --></parent></project>"""
    val root = parseXmlString(Source.fromString(xmlStr))
    val DocComments(comments) = CommentExtractor(root)
    comments.size should be(1)
    val Comments(s, path) = comments(0)
    s.size should be(1)
    s(0) should be(" comment ")
    val pathElements = path.elems.toArray
    pathElements.size should be(3)
    pathElements(0) should be(ElementLabel("project"))
    pathElements(1) should be(ElementLabel("parent"))
    pathElements(2) should be(ListIndex(0))
  }

  it should "parse from complex XML" in {

    val root = parseXmlString(Source.fromResource("pom.xml"))
    val plugins = (root \ "build" \ "plugins")(0)
    CommentExtractor.isListElem(plugins.asInstanceOf[Elem]) should be(true)

    val DocComments(comments) = CommentExtractor(root)
    //comments.foreach { println }

    comments.size should be(17)
    val Comments(s0, path0) = comments(0)
    s0.size should be(1)
    s0(0) should be(" Resource identity sequence. ")
    val pathElements0 = path0.elems.toArray
    pathElements0.size should be(3)
    pathElements0(0) should be(ElementLabel("project"))
    pathElements0(1) should be(ElementLabel("properties"))
    pathElements0(2) should be(ListIndex(0))

    val Comments(s1, path1) = comments(1)
    s1.size should be(1)
    s1(0) should be(
      "    <maven.build.timestamp.format>yyyyMMddHHmmss" + 
        "</maven.build.timestamp.format> ")
    val pathElements1 = path1.elems.toArray
    pathElements1.size should be(3)
    pathElements1(0) should be(ElementLabel("project"))
    pathElements1(1) should be(ElementLabel("properties"))
    pathElements1(2) should be(ListIndex(0))

    val Comments(s12, path12) = comments(12)
    s12.size should be(1)
    s12(0) should be(" <icon></icon> ")
    val pathElements12 = path12.elems.toArray
    pathElements12.size should be(6)
    pathElements12(0) should be(ElementLabel("project"))
    pathElements12(1) should be(ElementLabel("build"))
    pathElements12(2) should be(ElementLabel("plugins"))
    pathElements12(3) should be(ListIndex(9))
    pathElements12(4) should be(ElementLabel("configuration"))
    pathElements12(5) should be(ListIndex(3))
  }

  it should "parse from Hocon" in {

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val config = 
      ConfigFactory.load("pom-conf", ConfigParseOptions.defaults(), resolveOpts)
    val DocComments(comments) = CommentExtractor(config)
    // comments.foreach { println }

    comments.size should be(19)
    val Comments(s0, path0) = comments(0)
    s0.size should be(1)
    s0(0) should be("url = \"\"")
    val pathElements0 = path0.elems.toArray
    pathElements0.size should be(2)
    pathElements0(0) should be(ElementLabel("project"))
    pathElements0(1) should be(ElementLabel("version"))

    val Comments(s1, path1) = comments(1)
    s1.size should be(1)
    s1(0) should be(" https://mvnrepository.com/artifact/com.chuusai/shapeless")
    val pathElements1 = path1.elems.toArray
    pathElements1.size should be(3)
    pathElements1(0) should be(ElementLabel("project"))
    pathElements1(1) should be(ElementLabel("dependencies"))
    pathElements1(2) should be(ListIndex(2))
  }

  it should "inject into XML" in {

    val comments = DocComments(Seq(Comments(
      Seq("comment"), 
      CommentPath(ElementLabel("project"), ElementLabel("dependencies")))))
    val root = XML.load("pom.xml")
    val newRoot = comments.insertXml(root)

    // make sure comments are in newRoot 
    val foundComments: Option[String] = 
      newRoot.child.find { 
        case pdeps: Elem => pdeps.label == "dependencies"
        case _ => false
      } match {
        case Some(deps) => deps.child.find { _.isInstanceOf[Comment] } match {
          case Some(comment) => Some(comment.asInstanceOf[Comment].commentText)
          case _ => None
        }
        case None => None
      }
    foundComments should be(Some("comment"))

    // and that root and newRoot contain the same elements
    val project1 = new Project(root)
    val project2 = new Project(newRoot)
    project2.toString should be(project1.toString)
  }

  it should "inject into Hocon" in {

    val comments = DocComments(Seq(Comments(
      Seq("comment"), 
      CommentPath(ElementLabel("project"), ElementLabel("dependencies")))))
    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val config =
      ConfigFactory.load("pom-conf", ConfigParseOptions.defaults(), resolveOpts)

    val config2 = comments.insertConf(config)
    val options = ConfigRenderOptions.defaults().setOriginComments(false)
    // println(config2.root().render(options))

    // make sure there are comments in string form
    val foundComments = 
      config2.getList("project.dependencies").origin().comments()
    foundComments.size should be(1)
    foundComments.get(0) should be("comment")

    // and that they contain the same data
    val project1 = HoconReader.readPOM(config)
    val project2 = HoconReader.readPOM(config2)
    project2.toString should be(project1.toString)
  }

  it should "convert XML to Hocon" in {

    val root = parseXmlString(Source.fromResource("comment-bug.xml"))
    val comments = CommentExtractor(root)
    val project = new Project(root)
    val hocon = ConfigFactory.parseString(JsonWriter.writeConcisePOM(project))
    val hocon1 = comments.insertConf(hocon)
    HoconReader.readPOM(
      hocon1).toString.replaceAllLiterally("Vector(", "List(") should be(
      project.toString.replaceAllLiterally("Vector(", "List("))

    val root2 = parseXmlString(Source.fromResource("comment-bug2.xml"))
    val comments2 = CommentExtractor(root2)
    val project2 = new Project(root2)
    val hocon2 = ConfigFactory.parseString(JsonWriter.writeConcisePOM(project2))
    val hocon3 = comments2.insertConf(hocon2)
    HoconReader.readPOM(
      hocon3).toString.replaceAllLiterally("Vector(", "List(") should be(
      project2.toString.replaceAllLiterally("Vector(", "List("))

    val root3 = parseXmlString(Source.fromResource("comment-bug3.xml"))
    val comments3 = CommentExtractor(root3)
    val project3 = new Project(root3)
    val hocon4 = ConfigFactory.parseString(JsonWriter.writeConcisePOM(project3))
    val hocon5 = comments3.insertConf(hocon4)
    HoconReader.readPOM(
      hocon5).toString.replaceAllLiterally("Vector(", "List(") should be(
      project3.toString.replaceAllLiterally("Vector(", "List("))
  }
}
