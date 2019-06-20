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

package org.apache.maven

import java.io.{ File, InputStream, IOException }
import javax.xml.parsers.{
  DocumentBuilder, DocumentBuilderFactory, ParserConfigurationException }

import scala.xml.{
  Comment, Elem, MetaData, Node, Null, Text, TopScope, UnprefixedAttribute }

import com.typesafe.config._
import org.json4s._
import org.w3c.dom.{
  Comment => DomComment, Document, Element, Entity, Node => DomNode,
  ProcessingInstruction, Text => DomText }
import org.xml.sax.SAXException

package object unbound {

  def ensureDefault[T](seq: Seq[T], default: T): Seq[T] =
    if (!seq.isEmpty) seq
    else Seq[T](default)

  def emptyToNull(s: String): String = if (s != "") s else null

  def emptyToDefault(s: String, d: String): String = if (s != "") s else d

  def emptyToDefaultBool(s: String, d: Boolean): Boolean =
    if (s != "") (s == "true") else d

  def loadConfig(files: File*): Config = {
    require(!files.isEmpty)

    val config: Config = ConfigFactory.parseFile(files.head)
    files.tail.foldLeft(config) { case(conf, file) =>
      conf.withFallback(ConfigFactory.parseFile(file)) }.resolve()
  }

  // supported type conversions:
  // - Boolean
  // - Number
  // - String
  // - Seq[Any]
  // - Properties
  // - Map[String, Any]
  // - Seq[Dependency]
  def elemToConfig(elem: Elem): Config = {
    import scala.collection.JavaConverters._
    def toAnyRef(s: String): Any = s match {
      case "true" => true
      case "false" => false
      case s =>
        try { s.toInt } catch {
          case nfe: NumberFormatException =>
            try { s.toDouble } catch {
              case nfe2: NumberFormatException => s
            }
        }
    }

    def appendNode(path: String, parent: Config, el: Node): ConfigValue =
      if (el != null) {
        el match {
          case e: Elem =>
            // a simple value
            if (e.child.forall { _.isInstanceOf[Text] }) {
              ConfigValueFactory.fromAnyRef(toAnyRef(e.text.trim))

              // an Archiver
            } else if (e.label == SL.Archive.toString) {
              val a = new Archiver(e)
              ConfigFactory.parseString(JsonWriter.writeArchiver(a)).root()

              // a Dependency object
            } else if (e.child.forall {
              case ele: Elem => ele.label == SL.DependencyStr.toString
              case t: Text => t.text.trim == ""
              case _ => false
            }) {
              val listInJava = e.child.map {
                appendNode(path, parent, _) }.filter { _ != null}.asJava
              ConfigValueFactory.fromIterable(listInJava)

              // a property map (ie Map[String, String])
            } else if (e.child.forall {
              case el: Elem => el.label == SL.PropertyStr.toString
              case t: Text => t.text.trim == ""
              case _ => false
            }) {
              val mapInJava = (e \ SL.PropertyStr).map { el =>
                ((el \ SL.Name).text, (el \ SL.ValueStr).text) }.toMap.asJava
              ConfigValueFactory.fromMap(mapInJava)

              // a list
            } else if (e.child.forall {
              case el: Elem => e.label.startsWith(el.label)
              case t: Text => t.text.trim == ""
              case _ => false
            }) {
              val listInJava = e.child.map {
                appendNode(path, parent, _) }.filter { _ != null }.asJava
              ConfigValueFactory.fromIterable(listInJava)

            } else {
              val implAttr = (e \ ("@" + SL.Implementation))
              val mapInScala: Map[String, ConfigValue] =
                e.child.map {
                  case ele: Elem =>
                    val newPath =
                      if (path != "") path + "." + ele.label else ele.label
                    (ele.label, appendNode(newPath, parent, ele))
                  case t: Text => (SL.TextStr.toString, null)
                }.filter { case(k, v) => v != null }.toMap
              val map = mapInScala.foldLeft(parent) { case(c, (k, v)) =>
                c.withValue(k, v) }.root()

              // resource transformer
              if (e.label == SL.Transformer.toString && !implAttr.isEmpty) {
                map.withValue(
                  SL.Implementation,
                  ConfigValueFactory.fromAnyRef(implAttr.text.trim))
              } else {
                map
              }
            }
          case t: Text =>
            if (t.text.trim != "")
              ConfigValueFactory.fromAnyRef(toAnyRef(t.text.trim))
            else null
          case c: Comment =>
            val epty = ConfigFactory.empty()
            val list = new java.util.ArrayList[java.lang.String]()
            list.add(c.text)
            epty.root().withOrigin(epty.origin().withComments(list))
          case n: Node =>
            println("n " + n)
            assert(false)
            null
        }
      } else {
        null
      }

    if (elem != null) {
      val empty = ConfigFactory.empty()
      val childConfs =
        elem.child.map { ch => (ch.label, appendNode(ch.label, empty, ch)) }
      childConfs.foldLeft(ConfigFactory.empty()) { case(c, (k, v)) =>
        if (v != null) c.withValue(k, v) else c }
    } else {
      null
    }
  }

  private def singular(s: String): String =
    if (s.endsWith("ies")) s.substring(0, s.length - 3) + "y"
    else s.substring(0, s.length - 1)

  private def removeQuotes(s: String): String =
    if (!s.startsWith("\"") || !s.endsWith("\""))
      s.replaceAllLiterally("\\n", scala.compat.Platform.EOL)
    else s.substring(1, s.length - 1).replaceAllLiterally(
      "\\n", scala.compat.Platform.EOL)

  private def isDependencyProperty(key: String): Boolean = key match {
    case "groupId" | "artifactId" | "version" | "scope" | "optional" => true
    case _ => false
  }

  def configToElem(config: Config): Elem = {
    import scala.collection.JavaConverters._

    val children: Seq[(String, ConfigValue)] = config.root().asScala.toSeq

    def makeElem(key: String, value: ConfigValue): Elem =
      if (key == SL.Archive.toString) {
        HoconReader.readArchiver(value.atKey(SL.Archive)).xml
      } else if (key.contains(".")) {
        val tokens: Array[String] = key.split('.').reverse
        val last = tokens.head
        tokens.tail.foldLeft(makeElem(last, value)) { case(c, token) =>
          new Elem(null, token, Null, TopScope, c)
        }
      } else if (value != null) {
        value match {
          case l: ConfigList =>
            val childElems = l.asScala.map { it => makeElem(singular(key), it) }
            new Elem(null, key, Null, TopScope, childElems: _*)
          case m: ConfigObject =>
            val mS = m.asScala
            var elemKey = key
            var attrs: MetaData = Null
            val childElems =
              if (mS.values.forall { v =>
                v.valueType() == ConfigValueType.STRING }) {
                if (mS.keySet.forall { isDependencyProperty(_) }) {
                  // a Dependency object (for configurations)
                  elemKey = SL.DependencyStr
                  mS.map { case(k, v) => new Elem(
                    null, k, Null, TopScope, Text(removeQuotes(v.render())))
                  }.toSeq
                } else if (mS.keySet.find {
                  _ == SL.Implementation.toString }.isDefined) {
                  // a resource transformer
                  elemKey = SL.Transformer
                  val impl =
                    mS.find { case(k, v) =>
                      k == SL.Implementation.toString }.map { _._2 }.get
                  attrs = new UnprefixedAttribute(
                    SL.Implementation, removeQuotes(impl.render()), Null)
                  mS.filter { case(k, _) =>
                    k != SL.Implementation.toString }.map { case(k, v) =>
                      new Elem(
                        null, k, Null, TopScope, Text(removeQuotes(v.render())))
                  }.toSeq
                } else if (mS.keySet.find {
                  _ == SL.Archive.toString }.isDefined) {
                  // a Maven archiver
                  val arch = HoconReader.readArchiver(m.toConfig())
                  Seq(arch.xml) ++
                  mS.filter { _._1 == SL.Archive.toString }.map { case(k, v) =>
                    makeElem(k, v) }
                } else {
                  // a Properties object
                  mS.map { case(k, v) =>
                    new Elem(
                      null, SL.PropertyStr, Null, TopScope,
                      new Elem(null, SL.Name, Null, TopScope, new Text(k)),
                      new Elem(
                        null, SL.ValueStr, Null, TopScope,
                        new Text(removeQuotes(v.render()))))
                  }.toSeq
                }
              } else {
                // a normal Map (ie non-string value type)
                mS.map { case(k, v) => makeElem(k, v) }.toSeq
              }
            new Elem(null, elemKey, attrs, TopScope, childElems: _*)
          case v: ConfigValue =>
            val value = removeQuotes(v.render())
            new Elem(null, key, Null, TopScope, new Text(value))
        }
      } else {
        null
      }

    val childElems: Seq[Elem] =
      children.map { entry => makeElem(entry._1, entry._2) }
    new Elem(null, SL.Configuration, Null, TopScope, childElems: _*)
  }

  def jsonToConfig(jobject: JObject): Config = {
    import scala.collection.JavaConverters._

    def fromJson(v: JValue): Any =
      if (v != null) {
        v match {
          case JNull => null
          case s: JString => s.s
          case b: JBool => b.value
          case d: JDecimal => d.num
          case d: JDouble => d.num
          case i: JInt => i.num
          case o: JObject => ConfigValueFactory.fromMap(fromJsonObject(o))
          case a: JArray =>
            val iter = a.arr.map { fromJson(_) }.asJava
            ConfigValueFactory.fromIterable(iter)
          case v: JValue => assert(false); null
        }
      } else {
        null
      }

    def fromJsonObject(jobj: JObject): java.util.Map[String, Any] =
      jobj.obj.map { case((key, v)) =>
        v match {
          case arr: JArray => (key, fromJson(arr))
          case obj: JObject => (key, fromJsonObject(obj))
          case v: JValue => (key, fromJson(v))
        }
      }.toMap.asJava

    ConfigValueFactory.fromMap(fromJsonObject(jobject)).toConfig()
  }

  def configToJson(conf: Config): JObject = {
    import scala.collection.JavaConverters._

    def makeJValuePrimitive(value: ConfigValue): JValue =
      value.unwrapped() match {
        case null => JNull
        case b: java.lang.Boolean => JBool(b.booleanValue())
        case b: java.lang.Byte => JInt(b.toInt)
        case s: java.lang.Short => JInt(s.toInt)
        case i: java.lang.Integer => JInt(i.toInt)
        case l: java.lang.Long => JDecimal(new java.math.BigDecimal(l.toLong))
        case f: java.lang.Float => JDouble(f.toDouble)
        case d: java.lang.Double => JDouble(d.toDouble)
        case s: String => JString(s)
        case _ => assert(false); null
      }

    def makeJValue(value: ConfigValue): JValue = {
      value match {
        case l: ConfigList =>
          JArray(l.asScala.map { it => makeJValue(it) }.toList)
        case m: ConfigObject =>
          JObject(m.asScala.map { it => (it._1, makeJValue(it._2)) }.toList)
        case _ => makeJValuePrimitive(value)
      }
    }

    if (conf != null) {
      val children: Seq[java.util.Map.Entry[String, ConfigValue]] =
        conf.entrySet().asScala.toSeq
      val childElems: Seq[JField] =
        children.map { entry => (entry.getKey(), makeJValue(entry.getValue())) }
      new JObject(childElems.toList)
    } else {
      null
    }
  }

  object SL extends Labels
}
