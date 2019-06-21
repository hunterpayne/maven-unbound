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

import scala.xml.{ Elem, Node, XML }

import org.scalatest.{ FlatSpec, Matchers }

import com.typesafe.config.{ 
  Config, ConfigFactory, ConfigObject, ConfigResolveOptions, ConfigParseOptions }

class ConfigurationSpec extends FlatSpec with Matchers {

  behavior of "Translating XML to Typesafe Config(Hocon)"

  it should "translate empty Configurations" in {

    elemToConfig(null) should be(null)
    val elem = XML.loadString("<configuration></configuration>")
    elemToConfig(elem) should be(ConfigFactory.empty())
  }

  it should "translate basic types" in {

    val elem = XML.loadString("""
      <configuration>
        <name>some-string</name>
        <num>18</num>
        <george>true</george>
        <float>-179.435</float>
      </configuration>""")

    val conf = elemToConfig(elem)
    conf.getString("name") should be("some-string")
    conf.getInt("num") should be(18)
    conf.getBoolean("george") should be(true)
    conf.getDouble("float") should be(-179.435)
    conf.entrySet().size() should be(4)
  }

  it should "translate lists of basic types of objects" in {

    val elem = XML.loadString("""
      <configuration>
        <options>
          <option>some-string</option>
          <option>1800</option>
          <option>true</option>
          <option>false</option>
        </options>
      </configuration>""")

    val conf = elemToConfig(elem)
    val list = conf.getStringList("options")
    list.size should be(4)
    list.get(0) should be("some-string")
    list.get(1) should be("1800")
    list.get(2) should be("true")
    list.get(3) should be("false")
    conf.entrySet().size() should be(1)
  }

  it should "translate lists of objects" in {

    val elem = XML.loadString("""
      <configuration>
        <foobars>
          <foobar>
            <name>ring</name>
          </foobar>
          <foobar>
            <name>around</name>
            <url>http://the.rosey/</url>
          </foobar>
          <foobar>
            <name>pocket full</name>
            <url>http://of.poseies/</url>
          </foobar>
        </foobars>
      </configuration>""")

    val conf = elemToConfig(elem)
    val list: java.util.List[_ <: ConfigObject] = conf.getObjectList("foobars")
    list.size should be(3)
    list.get(0).get("name").unwrapped should be("ring")
    list.get(1).get("name").unwrapped should be("around")
    list.get(1).get("url").unwrapped should be("http://the.rosey/")
    list.get(2).get("name").unwrapped should be("pocket full")
    list.get(2).get("url").unwrapped should be("http://of.poseies/")
    conf.entrySet().size() should be(1)
  }

  it should "translate objects" in {
    val elem = XML.loadString("""
      <configuration>
        <person>
          <firstName>Jason</firstName>
          <lastName>van Zyl</lastName>
        </person>
        <place>
          <city>Forest Hill</city>
          <state>Maryland</state>
          <country>U.S.</country>
        </place>
        <thing>
          <id>Apache</id>
          <name>Apache Foundation</name>
        </thing>
      </configuration>""")

    val conf = elemToConfig(elem)
    val person = conf.getObject("person")
    person.get("firstName").unwrapped should be("Jason")
    person.get("lastName").unwrapped should be("van Zyl")
    val place = conf.getObject("place")
    place.get("city").unwrapped should be("Forest Hill")
    place.get("state").unwrapped should be("Maryland")
    place.get("country").unwrapped should be("U.S.")
    val thing = conf.getObject("thing")
    thing.get("id").unwrapped should be("Apache")
    thing.get("name").unwrapped should be("Apache Foundation")
    conf.entrySet().size() should be(7)
  }

  it should "translate maps of basic types" in {

    val elem = XML.loadString("""
      <configuration>
        <person>
          <firstName>Foo</firstName>
          <lastName>Bar</lastName>
          <age>174</age>
          <dead>true</dead>
        </person>
      </configuration>""")

    val conf = elemToConfig(elem)
    conf.getString("person.firstName") should be("Foo")
    conf.getString("person.lastName") should be("Bar")
    conf.getInt("person.age") should be(174)
    conf.getBoolean("person.dead") should be(true)
    conf.entrySet().size() should be(4)
  }

  it should "translate maps of objects" in {
    val elem = XML.loadString("""
      <configuration>
        <game>
          <person>
            <firstName>Jason</firstName>
            <lastName>van Zyl</lastName>
          </person>
          <place>
            <city>Forest Hill</city>
            <state>Maryland</state>
            <country>U.S.</country>
          </place>
          <thing>
            <id>Apache</id>
            <name>Apache Foundation</name>
          </thing>
        </game>
      </configuration>""")

    val conf = elemToConfig(elem)
    val game = conf.getObject("game")
    val person = game.toConfig().getObject("person")
    person.get("firstName").unwrapped should be("Jason")
    person.get("lastName").unwrapped should be("van Zyl")
    val place = game.toConfig().getObject("place")
    place.get("city").unwrapped should be("Forest Hill")
    place.get("state").unwrapped should be("Maryland")
    place.get("country").unwrapped should be("U.S.")
    val thing = game.toConfig().getObject("thing")
    thing.get("id").unwrapped should be("Apache")
    thing.get("name").unwrapped should be("Apache Foundation")
    val person2 = conf.getObject("game.person")
    person should be(person2)
    val place2 = conf.getObject("game.place")
    place should be(place2)
    val thing2 = conf.getObject("game.thing")
    thing should be(thing2)
    conf.entrySet().size() should be(7)
  }

  it should "translate properties" in {
    val elem = XML.loadString("""
      <configuration>
        <props>
          <property>
            <name>foo</name>
            <value>value1</value>
          </property>
          <property>
            <name>bar</name>
            <value>value2</value>
          </property>
          <property>
            <name>foobar</name>
            <value>value3</value>
          </property>
        </props>
      </configuration>""")

    val conf = elemToConfig(elem)
    val props = conf.getObject("props")
    props.size should be(3)
    props.get("foo").unwrapped should be("value1")
    props.get("bar").unwrapped should be("value2")
    props.get("foobar").unwrapped should be("value3")
    conf.entrySet().size() should be(3)
  }

  it should "translate archivers" in {
   
    val elem = XML.loadString("""
      <configuration>
        <archive>
          <addMavenDescriptor>false</addMavenDescriptor>
          <compress>false</compress>
          <forced>false</forced>
          <index>true</index>
          <manifest>
            <addClasspath>true</addClasspath>
            <addDefaultEntries>false</addDefaultEntries>
            <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
            <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
            <addBuildEnvironmentEntries>true</addBuildEnvironmentEntries>
            <addExtensions>true</addExtensions>
            <classpathLayoutType>custom</classpathLayoutType>
            <classpathPrefix>some</classpathPrefix>
            <customClasspathLayout>something</customClasspathLayout>
            <mainClass>com.someclass.Main</mainClass>
            <packageName>com.someclass</packageName>
            <useUniqueVersions>false</useUniqueVersions>
          </manifest>
          <manifestEntries>
            <some>key</some>
            <another>key2</another>
          </manifestEntries>
          <manifestFile>some.MF</manifestFile>
          <manifestSections>
            <manifestSection>
              <name>Section1</name>
              <manifestEntries>
                <entry>value3</entry>
                <another>value4</another>
              </manifestEntries>
            </manifestSection>
            <manifestSection>
              <name>Section2</name>
              <manifestEntries>
                <crypto>value5</crypto>
                <for>value6</for>
              </manifestEntries>
            </manifestSection>
          </manifestSections>
          <pomPropertiesFile>false</pomPropertiesFile>
        </archive>
      </configuration>""")

    val conf = elemToConfig(elem)
    val archiver = conf.getObject("archive").toConfig
    archiver.getBoolean("addMavenDescriptor") should be(false)
    archiver.getBoolean("compress") should be(false)
    archiver.getBoolean("forced") should be(false)
    archiver.getBoolean("index") should be(true)

    archiver.getBoolean("manifest.addClasspath") should be(true)
    archiver.getBoolean("manifest.addDefaultEntries") should be(false)
    archiver.getBoolean("manifest.addDefaultImplementationEntries") should be(true)
    archiver.getBoolean("manifest.addDefaultSpecificationEntries") should be(true)
    archiver.getBoolean("manifest.addBuildEnvironmentEntries") should be(true)
    archiver.getBoolean("manifest.addExtensions") should be(true)
    archiver.getString("manifest.classpathLayoutType") should be("custom")
    archiver.getString("manifest.classpathPrefix") should be("some")
    archiver.getString("manifest.customClasspathLayout") should be("something")
    archiver.getString("manifest.mainClass") should be("com.someclass.Main")
    archiver.getString("manifest.packageName") should be("com.someclass")
    archiver.getBoolean("manifest.useUniqueVersions") should be(false)

    val entries = archiver.getObject("manifestEntries")
    entries.size should be(2)
    entries.get("some").unwrapped should be("key")
    entries.get("another").unwrapped should be("key2")

    archiver.getString("manifestFile") should be("some.MF")
    val sections = archiver.getObjectList("manifestSections")
    sections.size should be(2)
    val section1 = sections.get(0).toConfig
    section1.getString("name") should be("Section1")
    val manEntries1 = section1.getObject("manifestEntries")
    manEntries1.size should be(2)
    manEntries1.get("entry").unwrapped should be("value3")
    manEntries1.get("another").unwrapped should be("value4")
    val section2 = sections.get(1).toConfig
    section2.getString("name") should be("Section2")
    val manEntries2 = section2.getObject("manifestEntries")
    manEntries2.size should be(2)
    manEntries2.get("crypto").unwrapped should be("value5")
    manEntries2.get("for").unwrapped should be("value6")

    archiver.getString("pomPropertiesFile") should be("false")
    conf.entrySet().size() should be(21) // 25???
  }

  it should "translate resource transformers" in {

    val elem = XML.loadString("""
      <configuration>
        <transformers>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
            <mainClass>org.apache.maven.unbound.Cli</mainClass>
          </transformer>
        </transformers>
      </configuration>""")

    val conf = elemToConfig(elem)
    val transformers = conf.getObjectList("transformers")
    transformers.size should be(1)
    transformers.get(0).get("implementation").unwrapped should be(
      "org.apache.maven.plugins.shade.resource.ManifestResourceTransformer")
    transformers.get(0).get("mainClass").unwrapped should be(
      "org.apache.maven.unbound.Cli")
  }

  it should "translate dependencies" in {
    val elem = XML.loadString("""
      <configuration>
        <!-- test comment -->
        <defineBridge>
          <dependency>
            <groupId>org.scala-sbt</groupId>
            <artifactId>compiler-bridge_${version.scala.epoch}</artifactId>
            <version>${version.scala.zinc}</version>
          </dependency>
        </defineBridge>
      </configuration>""")

    val conf = elemToConfig(elem)
    val bridges = conf.getObjectList("defineBridge")
    bridges.size should be(1)
    val bridge = bridges.get(0).toConfig
    bridge.getString("groupId") should be("org.scala-sbt")
    bridge.getString("artifactId") should be(
      "compiler-bridge_${version.scala.epoch}")
    bridge.getString("version") should be("${version.scala.zinc}")
  }

  behavior of "Translating Typesafe Config(Hocon) to XML"

  it should "translate empty Configurations" in {

    configToElem(null) should be(null)
    configToElem(ConfigFactory.empty()).toString should be("<configuration/>")
  }

  it should "translate basic types" in {

    val config = ConfigFactory.parseString("""{
      name : "some-string",
      num : 18,
      george : true,
      float : -179.435
    }""")

    val elem = configToElem(config)
    (elem \ "name").text should be("some-string")
    (elem \ "num").text should be("18")
    (elem \ "george").text should be("true")
    (elem \ "float").text should be("-179.435")
    elem.child.size should be(4)
  }

  it should "translate lists of basic types of objects" in {

    val config = ConfigFactory.parseString("""{
      options : [ "some-string", 1800, true, false ]
    }""")

    val elem = configToElem(config)
    val listElem = (elem \ "options").head
    listElem.child.size should be(4)
    listElem.child(0).label should be("option")
    listElem.child(0).text should be("some-string")
    listElem.child(1).label should be("option")
    listElem.child(1).text should be("1800")
    listElem.child(2).label should be("option")
    listElem.child(2).text should be("true")
    listElem.child(3).label should be("option")
    listElem.child(3).text should be("false")
  }

  /*
   // TODO fix
  it should "translate lists of objects" in {
    val config = ConfigFactory.parseString("""{
      foobars : [
        { name : "ring" }, 
        { name : "around", url : "http://the.rosey" }, 
        { name : "pocket full", url : "http://of.poseies" } 
      ]
    }""")
    val elem = configToElem(config)
    val list = (elem \ "foobars")
    list.size should be(1)
    val foobars = list.head
    foobars.label should be("foobars")
    val bars = (foobars \ "foobar").toArray
    bars.size should be(3)
    println("bars " + bars.mkString(","))
    bars(0).child.size should be(1)
    bars(0).label should be("foobar")
    (bars(0) \ "name") should be("ring")
    bars(1).child.size should be(2)
    (bars(1) \ "name").text should be("around")
    (bars(1) \ "url").text should be("http://the.rosey/")
    bars(2).child.size should be(2)
    (bars(2) \ "name").text should be("pocket full")
    (bars(2) \ "url").text should be("http://of.poseies/")
  }
   */

  /*
   // TODO fix
  it should "translate objects" in {

    val config = ConfigFactory.parseString("""{
      person : { 
        firstName : "Jason",
        lastName : "van Zyl"
      },
      place : { 
        city : "Forest Hill",
        state : "Maryland",
        country : "U.S."
      },
      thing : { 
        id : "Apache",
        name : "Apache Foundation"
      }
    }""")

    val elem = configToElem(config)
    println("elem " + elem)
    val pList = (elem \ "person").toArray
    pList.size should be(1)
    (pList(0) \ "firstName").text should be("Jason")
    (pList(0) \ "lastName").text should be("van Zyl")

    val plList = (elem \ "place").toArray
    plList.size should be(1)
    (plList(0) \ "city").text should be("Forest Hill")
    (plList(0) \ "state").text should be("Maryland")
    (plList(0) \ "country").text should be("U.S.")

    val tList = (elem \ "thing").toArray
    tList.size should be(1)
    (tList(0) \ "id").text should be("Apache")
    (tList(0) \ "name").text should be("Apache Foundation")
  }
   */

  it should "translate maps of basic types" in {

    val config = ConfigFactory.parseString("""{
      person : { 
        firstName : "Foo",
        lastName : "Bar",
        age : 174,
        dead : true
      }
    }""")
    val elem = configToElem(config)
    val list = (elem \ "person").toArray
    list.size should be(1)
    (list(0) \ "firstName").text should be("Foo")
    (list(0) \ "lastName").text should be("Bar")
    (list(0) \ "age").text should be("174")
    (list(0) \ "dead").text should be("true")
  }

/*
   // TODO fix
  it should "translate maps of objects" in {

    val config = ConfigFactory.parseString("""{ game : {
      person : { 
        firstName : "Jason",
        lastName : "van Zyl"
      },
      place : { 
        city : "Forest Hill",
        state : "Maryland",
        country : "U.S."
      },
      thing : { 
        id : "Apache",
        name : "Apache Foundation"
      }
    }}""")

    val elem = configToElem(config)
    val pList = (elem \ "game" \ "person").toArray
    pList.size should be(1)
    println("p " + pList(0))
    (pList(0) \ "firstName").text should be("Jason")
    (pList(0) \ "lastName").text should be("van Zyl")

    val plList = (elem \ "game" \ "place").toArray
    plList.size should be(1)
    (plList(0) \ "city").text should be("Forest Hill")
    (plList(0) \ "state").text should be("Maryland")
    (plList(0) \ "country").text should be("U.S.")

    val tList = (elem \ "game" \ "thing").toArray
    tList.size should be(1)
    (tList(0) \ "id").text should be("Apache")
    (tList(0) \ "name").text should be("Apache Foundation")
  }
*/

  it should "translate properties" in {

    val config = ConfigFactory.parseString("""{
      props : {
        name : "foo",
        bar : "value2",
        foobar : "value3"
      }
    }""")

    val elem = configToElem(config)
    val propsNodes = (elem \ "props").toArray
    propsNodes.size should be(1)
    val propertyNodes = (propsNodes(0) \ "property").toArray
    propertyNodes.size should be(3)

    def checkNode(n: Node): Unit = n match {
      case e: Elem =>
        e.label should be("property")
        e.child.size should be(2)
        (e \ "name").text match {
          case "name" =>
            (e \ "value").text should be("foo")
          case "bar" =>
            (e \ "value").text should be("value2")
          case "foobar" =>
            (e \ "value").text should be("value3")
        }
    }

    propertyNodes.foreach { checkNode(_) }
  }

  it should "translate archivers" in {

    val config = ConfigFactory.parseString("""{
      archive : {
        addMavenDescriptor : false,
        compress : false,
        forced : false,
        index : true,
        manifest : {
          addClasspath : true,
          addDefaultEntries : false,
          addDefaultImplementationEntries : true,
          addDefaultSpecificationEntries : true,
          addBuildEnvironmentEntries : true,
          addExtensions : true,
          classpathLayoutType : "custom",
          classpathPrefix : "some",
          customClasspathLayout : "something",
          mainClass : "com.someclass.Main",
          packageName : "com.someclass",
          useUniqueVersions : false
        },
        manifestEntries : {
          some : "key",
          another : "key2"
        },
        manifestFile : "some.MF",
        manifestSections : [
          { name : "Section1", manifestEntries : { 
            "entry" : "value3", "another" : "value4" } 
          },
          { name : "Section2", manifestEntries : { 
            "crypto" : "value5", "for" : "value6" } 
          }
        ],
        pomPropertiesFile : "false"
      }
    }""")

    val elem = configToElem(config)
    val aList = (elem \ "archive").toArray
    aList.size should be(1)
    (aList(0) \ "addMavenDescriptor").text should be("false")
    (aList(0) \ "compress").text should be("false")
    (aList(0) \ "forced").text should be("false")
    (aList(0) \ "index").text should be("true")
    (aList(0) \ "manifest" \ "addClasspath").text should be("true")
    (aList(0) \ "manifest" \ "addDefaultEntries").text should be("false")
    (aList(0) \ "manifest" \ "addDefaultImplementationEntries").text should be(
      "true")
    (aList(0) \ "manifest" \ "addDefaultSpecificationEntries").text should be(
      "true")
    (aList(0) \ "manifest" \ "addBuildEnvironmentEntries").text should be(
      "true")
    (aList(0) \ "manifest" \ "addExtensions").text should be("true")
    (aList(0) \ "manifest" \ "classpathLayoutType").text should be("custom")
    (aList(0) \ "manifest" \ "classpathPrefix").text should be("some")
    (aList(0) \ "manifest" \ "customClasspathLayout").text should be(
      "something")
    (aList(0) \ "manifest" \ "mainClass").text should be("com.someclass.Main")
    (aList(0) \ "manifest" \ "packageName").text should be("com.someclass")
    (aList(0) \ "manifest" \ "useUniqueVersions").text should be("false")
    (aList(0) \ "manifestEntries" \ "some").text should be("key")
    (aList(0) \ "manifestEntries" \ "another").text should be("key2")
    (aList(0) \ "manifestFile").text should be("some.MF")

    val sections = (aList(0) \ "manifestSections" \ "manifestSection").toArray
    sections.size should be(2)
    (sections(0) \ "name").text should be("Section1")
    (sections(0) \ "manifestEntries" \ "entry").text should be("value3")
    (sections(0) \ "manifestEntries" \ "another").text should be("value4")
    (sections(1) \ "name").text should be("Section2")
    (sections(1) \ "manifestEntries" \ "crypto").text should be("value5")
    (sections(1) \ "manifestEntries" \ "for").text should be("value6")

    (aList(0) \ "pomPropertiesFile").text should be("false")
  }

  it should "translate resource transformers" in {

    val config = ConfigFactory.parseString("""{
      transformers : [ { 
        implementation : "org.apache.maven.plugins.shade.resource.ManifestResourceTransformer", 
        mainClass : "org.apache.maven.unbound.Cli" 
      } ]
    }""")

    val elem = configToElem(config)
    val tList = (elem \ "transformers").toArray
    tList.size should be(1)
    val list = (tList(0) \ "transformer").toArray
    list.size should be(1)
    (list(0) \ "@implementation").text should be(
      "org.apache.maven.plugins.shade.resource.ManifestResourceTransformer")
    (list(0) \ "mainClass").text should be("org.apache.maven.unbound.Cli")
  }

  it should "translate dependencies" in {

    val config = ConfigFactory.parseString("""{
      defineBridge : [ { 
        groupId : "org.scala-sbt",
        artifactId : "compiler-bridge_${version.scala.epoch}",
        version : "${version.scala.zinc}"
      } ]
    }""")

    val elem = configToElem(config)
    val dList = (elem \ "defineBridge").toArray
    dList.size should be(1)
    val list = (dList(0) \ "dependency").toArray
    list.size should be(1)
    (list(0) \ "groupId").text should be("org.scala-sbt")
    (list(0) \ "artifactId").text should be(
      "compiler-bridge_${version.scala.epoch}")
    (list(0) \ "version").text should be("${version.scala.zinc}")
  }

  /*
  behavior of "Translating Json to Typesafe Config(Hocon)"

  it should "translate empty Configurations" in {
  }
  it should "translate basic types" in {
  }

  it should "translate lists of basic types" in {
  }

  it should "translate lists of objects" in {
  }

  it should "translate an object" in {
  }

  it should "translate maps of basic types" in {
  }

  it should "translate maps of objects" in {
  }

  it should "translate archivers" in {
  }

  it should "translate resource transformers" in {
  }

  it should "translate dependencies" in {
  }

  behavior of "Translating Typesafe Config(Hocon) to Json"

  it should "translate empty Configurations" in {
  }

  it should "translate basic types" in {
  }

  it should "translate lists of basic types of objects" in {
  }

  it should "translate lists of objects" in {
  }

  it should "translate objects" in {
  }

  it should "translate maps of basic types" in {
  }

  it should "translate maps of objects" in {
  }

  it should "translate properties" in {
  }

  it should "translate archivers" in {
  }

  it should "translate resource transformers" in {
  }

  it should "translate dependencies" in {
  }

   */
}

