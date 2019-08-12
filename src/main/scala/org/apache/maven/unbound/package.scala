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

/**
  * This is the Maven Unbound package.  It contains a Scala implementation
  * of the Maven POM object and serialization/deserialization functionality
  * for translating POM files as XML to and from Json and Hocon formats.
  *
  * @author Hunter Payne
  */
package object unbound {

  /**
    * Take a list of something and if its empty returns a list containing only
    * default instead, otherwise it returns the original list
    * @param seq the list of T
    * @param default the default value wrapped in a list to return if the
    * list is empty
    */
  def ensureDefault[T](seq: Seq[T], default: T): Seq[T] =
    if (!seq.isEmpty) seq else Seq[T](default)

  /**
    * translates empty string to null, otherwise returns the original string
    */
  def emptyToNull(s: String): String = if (s != "") s else null

  /**
    * translates the empty string to a default, otherwise returns the original
    * string
    * @param s string to test and return if not empty
    * @param d string to return if s is the empty string
    */
  def emptyToDefault(s: String, d: String): String = if (s != "") s else d

  /**
    * if s is the string &quot;true&quot; returns true, if s is not the empty
    * string return false, otherwise return d
    * @param s string to test for &quot;true&quot;
    * @param d value to return if s is the empty string
    */
  def emptyToDefaultBool(s: String, d: Boolean): Boolean =
    if (s != "") (s == "true") else d

  /**
    * Utility method to load Config objects from a list of Hocon files
    */
  def loadConfig(files: File*): Config = {
    require(!files.isEmpty)

    // val config: Config = ConfigFactory.parseFile(files.head)
    // val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    // println("loading config " + files.head.getAbsolutePath)
    val parseOpts = ConfigParseOptions.defaults() /* .appendIncluder(
      new ConfigIncluder() {
        def include(
          context: ConfigIncludeContext, what: String): ConfigObject = {
          println("ctx " + context + " what " + what)
          context.relativeTo(what).parse(context.parseOptions)
        }

        def withFallback(fallback: ConfigIncluder): ConfigIncluder = {
          println("with fallback " + fallback)
          fallback.withFallback(this)
          this
          // fallback
        }
      }) */
    val config = ConfigFactory.parseFile(files.head, parseOpts)

    files.tail.foldLeft(config) { case(conf, file) =>
      // conf.withFallback(ConfigFactory.parseFile(file))
      conf.withFallback(ConfigFactory.parseFile(files.head, parseOpts))
      // conf.withFallback(ConfigFactory.load(
      // file.toURI(), ConfigParseOptions.defaults(), resolveOpts))
    }.resolve()
  }

  /**
    * Converts an XML Element into a Typesafe Config object.  Generally it
    * converts Elements into Maps keyed by the names of the child tags.  It
    * also looks for special patterns of XML to do more specific type
    * conversions. Supported type conversions: <ul>
    * <li>Boolean the strings true or false</li>
    * <li>Number both Integers and Doubles</li>
    * <li>String Text nodes of the XML document</li>
    * <li>Seq[Any] Elements whose child labels are the singlar form of elem's
    * label
    * <li>Properties Elements whose grand child labels are all name or value
    * <li>Seq[Dependency] Special case where child elements all have the
    * label &quot;dependency&quot;
    * <li>Archiver Special case where elem's label is &quot;archive&quot;
    * <li>Fileset Special case where elem's label is &quot;fileset&quot;
    * <li>Resource Transformer Special case where the elem's label is
    * transformers, the child's label is transformer and there is an attribute
    * named implementation defined
    * <li>Map[String, Any] general/default case
    * <ul>
    * @see Configuring Plugins
    * [[https://maven.apache.org/guides/mini/guide-configuring-plugins.html]]
    * @see Archiver [[http://maven.apache.org/shared/maven-archiver/index.html]]
    * @see Resource Transformers
    * [[https://maven.apache.org/plugins-archives/maven-shade-plugin-2.0/examples/resource-transformers.html]]
    */
  def elemToConfig(elem: Elem): Config = {
    import scala.collection.JavaConverters._

    // handles Booleans and Numbers
    def toAnyRef(s: String): Any = s match {
      case "true" => true
      case "false" => false
      case s =>
        if (s.startsWith("0")) {
          unencodeXml(s)
        } else {
          try { s.toInt } catch {
            case nfe: NumberFormatException =>
              try { s.toDouble } catch {
                case nfe2: NumberFormatException => unencodeXml(s)
              }
          }
        }
    }

    // converts a node el at path path to a ConfigValue inside of parent
    def appendNode(path: String, parent: Config, el: Node): ConfigValue =
      if (el != null) {
        el match {
          case e: Elem =>
            // a simple string value
            val attrs = e.attributes.asAttrMap
            if (e.child.forall { _.isInstanceOf[Text] } && attrs.isEmpty) {
              ConfigValueFactory.fromAnyRef(toAnyRef(e.text.trim))

              // an Archiver
            } else if (e.label == SL.Archive.toString && attrs.isEmpty) {
              val a = new Archiver(e)
              ConfigFactory.parseString(JsonWriter.writeArchiver(a)).root()

              // a fileset
            } else if (e.label == SL.FilesetStr.toString && attrs.isEmpty) {
              val f = new Fileset(e)
              ConfigFactory.parseString(JsonWriter.writeFileset(f)).root()

              // a Dependency object
            } else if (attrs.isEmpty && e.child.forall {
              case ele: Elem => ele.label == SL.DependencyStr.toString
              case t: Text => t.text.trim == ""
              case c: Comment => true
              case _ => false
            }) {
              val listInJava = e.child.map {
                appendNode(path, parent, _) }.filter { _ != null}.asJava
              ConfigValueFactory.fromIterable(listInJava)

              // a property map (ie Map[String, String])
            } else if (attrs.isEmpty && e.child.forall {
              case el: Elem => el.label == SL.PropertyStr.toString
              case t: Text => t.text.trim == ""
              case c: Comment => true
              case _ => false
            }) {
              val pairSeqInScala = (e \ SL.PropertyStr).map { el =>
                ((el \ SL.Name).text, (el \ SL.ValueStr).text) }
              val mapInJava = (pairSeqInScala :+ (
                (SL.PropertiesStr.toString, SL.TrueStr.toString))).
                toMap.asJava
              ConfigValueFactory.fromMap(mapInJava)

              // a list
            } else if (attrs.isEmpty && e.child.forall {
              case el: Elem => e.label.startsWith(el.label)
              case t: Text => t.text.trim == ""
              case c: Comment => true
              case _ => false
            }) {
              val listInJava = e.child.map {
                appendNode(path, parent, _) }.filter { _ != null }.asJava
              ConfigValueFactory.fromIterable(listInJava)

            } else {
              // check if this is a resource transformer
              val implAttr = (e \ ("@" + SL.Implementation))
              val mapInScala: Map[String, ConfigValue] =
                e.child.map {
                  case ele: Elem =>
                    val newPath =
                      if (path != "") path + "." + ele.label else ele.label
                    (ele.label, appendNode(newPath, parent, ele))
                  case t: Text => (SL.TextStr.toString, null)
                  case _ => (null, null)
                }.filter { case(k, v) => v != null }.toMap
              val map = mapInScala.foldLeft(parent) { case(c, (k, v)) =>
                c.withValue(k, v) }.root()

              // resource transformer
              if (e.label == SL.Transformer.toString && !implAttr.isEmpty) {
                map.withValue(
                  SL.Implementation,
                  ConfigValueFactory.fromAnyRef(implAttr.text.trim))
                // handle attributes here
              } else if (!attrs.isEmpty) {
                val keys = attrs.keySet.toSeq
                attrs.foldLeft(map) { case(c, (n, v)) =>
                  c.withValue(n, ConfigValueFactory.fromAnyRef(v))
                }.withValue(
                  SL.AttributeKeys,
                  ConfigValueFactory.fromIterable(keys.asJava))
              } else {
                map
              }
            }
          case t: Text =>
            if (t.text.trim != "")
              ConfigValueFactory.fromAnyRef(toAnyRef(t.text.trim))
            else null
          case c: Comment =>
            /*
            val epty = ConfigFactory.empty()
            val list = new java.util.ArrayList[java.lang.String]()
            list.add(c.text)
            epty.root().withOrigin(epty.origin().withComments(list))
             */
            null
            // unhandled
          case n: Node =>
            println("n " + n + " class " + n.getClass.getName)
            assert(false)
            null
        }
      } else {
        null
      }

    // check if null and start recursing down the heirarchy of children
    if (elem != null) {
      // build each child's ConfigObject
      val childConfs =
        elem.child.map { ch =>
          (ch.label, appendNode(ch.label, ConfigFactory.empty(), ch)) }
      // fold them together into 1 big Config at the paths
      childConfs.foldLeft(ConfigFactory.empty()) { case(c, (k, v)) =>
        if (v != null && !k.startsWith("#")) { c.withValue(k, v) } else c }
    } else {
      null
    }
  }

  // check return the plural form of s, currently only handles y to ies
  // special case
  protected[unbound] def singular(s: String): String =
    if (s.endsWith("ies")) s.substring(0, s.length - 3) + "y"
    else s.substring(0, s.length - 1)

  // translate from json string to a jvm string
  def unencodeJson(s: String): String = {
    val sb = new StringBuilder()
    var i = 0
    // the scala compiler didn't want to do this with a flatMap and a flag so
    // I had to do it with a while loop, the compiler kept moving the flag
    // before the flatMap when it was being used in an if condition after the
    // flatMap even though it was a var
    while (i < s.length - 1) {
      s.substring(i, i + 2) match {
        case "\\b" => sb.append('\b'); i = i + 2
        case "\\f" => sb.append('\f'); i = i + 2
        case "\\n" => sb.append('\n'); i = i + 2
        case "\\r" => sb.append('\r'); i = i + 2
        case "\\t" => sb.append('\t'); i = i + 2
        case "\\\"" => sb.append('"'); i = i + 2
        case "\\\\" => sb.append('\\'); i = i + 2
        case ch => sb.append(ch(0)); i = i + 1
      }
    }
    if (i == s.length - 1) sb.append(s.last)
    sb.toString
  }

  // translate from hocon string to a jvm string
  private def removeQuotes(s: String): String = {
    val stripped =
      if (!s.startsWith("\"") || !s.endsWith("\"")) s
      else s.substring(1, s.length - 1)
    unencodeJson(stripped)
  }

  // translate from a xml string to a jvm string
  private def unencodeXml(s: String): String =
    s.replaceAllLiterally("&lt;", "<").
      replaceAllLiterally("&gt;", ">").
      replaceAllLiterally("&amp;", "&").
      replaceAllLiterally("&quot;", "\"").
      replaceAllLiterally("&apos;", "'")

  // check if a key is one that's special to dependencies
  private def isDependencyProperty(key: String): Boolean = key match {
    case "groupId" | "artifactId" | "version" | "scope" | "optional" => true
    case _ => false
  }

  /**
    * Translates a Typesafe Config object to a XML Element.  Handles all the
    * same special cases as elemToConfig.
    * Supported type conversions: <ul>
    * <li>Boolean &lt;key&gt;true/false&lt;/key&gt;</li>
    * <li>Number &lt;key&gt;number&lt;/key&gt;</li>
    * <li>String &lt;key&gt;value&lt;/key&gt;</li>
    * <li>Seq[Any] &lt;key(plural)&gt;&lt;key(singular)&gt;value(0)
    * &lt;/key(singular)&gt;&lt;key(singular)&gt;value(1)&lt;/key(singular)&gt;
    * ...&lt;/key(plural)&gt;
    * <li>Properties Identified by the presence of a child at path
    * &quot;properties&quot; with the value true, otherwise represented as a
    * Map[String, String] in the Scala code.  It returns the Maven property
    * XML format which is: &lt;key&gt;&lt;property&gt;&lt;name&gt;property
    * key1&lt;/name&gt;&lt;value&gt;property
    * value1&lt;/value&gt;&lt;/property&gt;
    * &lt;property&gt;&lt;name&gt;property key2&lt;/name&gt;
    * &lt;value&gt;property value2&lt;/value&gt;&lt;/property&gt;...&lt;/key&gt;
    * <li>Seq[Dependency] Special case where child elements all have the
    * keys of a valid &quot;dependency&quot; object in the POM
    * <li>Archiver Special case where a child key is &quot;archive&quot;
    * <li>Fileset Special case where a child key is &quot;fileset&quot;
    * <li>Resource Transformer Special case where a child key is
    * implementation
    * <li>Map[String, Any] general/default case, returns whatever
    * the value translates to inside a XML element labeled by the key
    * <ul>
    */
  def configToElem(config: Config): Elem = {
    import scala.collection.JavaConverters._

    // convert the Config value at key into a XML Element
    def makeElem(key: String, value: ConfigValue): Elem =
      // check for a child key called archive
      if (key == SL.Archive.toString) {
        HoconReader.readArchiver(value.atKey(SL.Archive)).xml
        // check for child keys with . so they can be split and encoded into
        // XML correctly
      } else if (key.contains(".")) {
        val tokens: Array[String] = key.split('.').reverse
        val last = tokens.head
        tokens.tail.foldLeft(makeElem(last, value)) { case(c, token) =>
          new Elem(null, token, Null, TopScope, c)
        }
      } else if (value != null) {
        value match {
          // a list so create a parent element called key with children labeled
          // key in singular form
          case l: ConfigList =>
            val childElems = l.asScala.map { it => makeElem(singular(key), it) }
            new Elem(null, key, Null, TopScope, childElems: _*)
            // a map so look for special cases and if they don't match convert
            // as a generic map built using recursion
          case m: ConfigObject =>
            val mS = m.asScala
            var elemKey = key
            var attrs: MetaData = Null
            val childElems =
              if (mS.keySet.find {
                _ == SL.PropertiesStr.toString }.isDefined) {
                // a Properties object
                mS.filter{
                  _._1 != SL.PropertiesStr.toString }.map { case(k, v) =>
                    new Elem(
                      null, SL.PropertyStr, Null, TopScope,
                      new Elem(null, SL.Name, Null, TopScope, new Text(k)),
                      new Elem(
                        null, SL.ValueStr, Null, TopScope,
                        new Text(removeQuotes(v.render()))))
                }.toSeq
              } else if (mS.values.forall { v =>
                v.valueType() == ConfigValueType.STRING }) {
                if (!mS.isEmpty &&
                  mS.keySet.forall { isDependencyProperty(_) }) {
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
                  _ == SL.Archive.toString }.isDefined &&
                  !mS.keySet.find {
                    _ == SL.AttributeKeys.toString }.isDefined) {
                  // a Maven archiver
                  val arch = HoconReader.readArchiver(m.toConfig())
                  Seq(arch.xml) ++
                  mS.filter { _._1 != SL.Archive.toString }.map { case(k, v) =>
                    makeElem(k, v) }

                } else if (mS.keySet.find {
                  _ == SL.FilesetStr.toString }.isDefined &&
                   !mS.keySet.find {
                     _ == SL.AttributeKeys.toString }.isDefined) {
                  // a Maven fileset
                  val fs = HoconReader.readFileset(m.toConfig())
                  Seq(fs.xml) ++
                  mS.filter { _._1 != SL.FilesetStr.toString
                  }.map { case(k, v) => makeElem(k, v) }

                } else {
                  // a normal Map (ie non-string value type)
                  mS.map { case(k, v) => makeElem(k, v) }.toSeq
                }
              } else if (mS.keySet.find {
                _ == SL.Archive.toString }.isDefined &&
                !mS.keySet.find { _ == SL.AttributeKeys.toString }.isDefined) {
                // a Maven archiver
                val arch = HoconReader.readArchiver(m.toConfig())
                Seq(arch.xml) ++
                mS.filter { _._1 != SL.Archive.toString }.map { case(k, v) =>
                  makeElem(k, v) }

              } else if (mS.keySet.find {
                _ == SL.FilesetStr.toString }.isDefined &&
                !mS.keySet.find { _ == SL.AttributeKeys.toString }.isDefined) {
                // a Maven fileset
                val fs = HoconReader.readFileset(m.toConfig())
                Seq(fs.xml) ++
                mS.filter { _._1 != SL.FilesetStr.toString }.map { case(k, v) =>
                  makeElem(k, v) }

              } else {
                // look for attributeKeys and if its present make those
                // children into attributes instead of elements
                val c = m.toConfig
                if (c.hasPath(SL.AttributeKeys)) {
                  val attrKeys = c.getStringList(SL.AttributeKeys)

                  if (attrKeys != null && !attrKeys.isEmpty) {
                    val attrValues: Seq[(String, String)] =
                      attrKeys.asScala.filter(
                        _ != SL.Implementation.toString).map { attr =>
                        (attr, removeQuotes(mS(attr).render())) }
                    val md: MetaData = Null
                    attrs =
                      attrValues.foldLeft(md) { case(n, (k, v)) =>
                        new UnprefixedAttribute(k, v, n) }
                    mS.filter { case(k, _) =>
                      k != SL.AttributeKeys.toString && !attrKeys.contains(k)
                    }.map { case(k, v) => makeElem(k, v) }.toSeq

                  } else {
                    // a normal Map (ie non-string value type)
                    mS.map { case(k, v) => makeElem(k, v) }.toSeq
                  }
                } else {

                  // a normal Map (ie non-string value type)
                  mS.map { case(k, v) => makeElem(k, v) }.toSeq
                }
              }

            new Elem(null, elemKey, attrs, TopScope, childElems: _*)
            // a simple value so convert to a string and wrap into a Element
            // labeled key
          case v: ConfigValue =>
            val value = removeQuotes(v.render())
            new Elem(null, key, Null, TopScope, new Text(value))
        }
      } else {
        null
      }

    // start the recurse by visiting the top level children of the config
    if (config != null) {
      val children: Seq[(String, ConfigValue)] = config.root().asScala.toSeq
      val childElems: Seq[Elem] =
        children.map { entry => makeElem(entry._1, entry._2) }
      new Elem(null, SL.Configuration, Null, TopScope, childElems: _*)
    } else {
      null
    }
  }

  /**
    * Converts a Json4s object into a Typesafe config in a very direct way
    * Handles no special cases as there is an isomorphism between Json and
    * Hocon.  Hocon is really just a special case of Json afterall.
    */
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

    if (jobject != null)
      ConfigValueFactory.fromMap(fromJsonObject(jobject)).toConfig()
    else null
  }

  /**
    * Converts a Typesafe Config object to a Json4s Json object usually for
    * serialization.  Does a direct translation.
    */
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
      val root = conf.root()
      val childKeys: Seq[String] = root.keySet().asScala.toSeq
      val childElems: Seq[JField] =
        childKeys.map { key => (key, makeJValue(root.get(key))) }
      new JObject(childElems.toList)
    } else {
      null
    }
  }

  /** a static set of internalized strings used by Unbound */
  object SL extends Labels
}
