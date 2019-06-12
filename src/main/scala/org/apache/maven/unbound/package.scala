
package org.apache.maven

import java.io.File

import scala.xml.{ Elem, Node, Text, Null, TopScope }

import org.json4s._

import com.typesafe.config._

package object unbound {

  def ensureDefault[T](seq: Seq[T], default: T): Seq[T] =
    if (!seq.isEmpty) seq
    else Seq[T](default)

  def emptyToNull(s: String): String = if (s != "") s else null

  def emptyToDefault(s: String, d: String): String = if (s != "") s else d

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

    def appendNode(path: String, parent: Config, el: Node): ConfigValue =
      el match {
        case e: Elem =>
          // a simple value
          if (e.child.forall { _.isInstanceOf[Text] }) {
            ConfigValueFactory.fromAnyRef(e.text.trim)

            // a Dependency object
          } else if (e.child.forall {
            case ele: Elem => ele.label == "dependency"
            case t: Text => t.text.trim == ""
            case _ => false
          }) {
            val listInJava = e.child.map {
              appendNode(path, parent, _) }.filter { _ != null}.asJava
            ConfigValueFactory.fromIterable(listInJava)

            // a property map (ie Map[String, String])
          } else if (e.child.forall {
            case el: Elem => el.label == "property"
            case t: Text => t.text.trim == ""
            case _ => false
          }) {
            val mapInJava = (e \ "property").map { el =>
              ((el \ "name").text, (el \ "value").text) }.toMap.asJava
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
            // a java map
            val mapInScala: Map[String, ConfigValue] =
              e.child.map {
                case ele: Elem =>
                  val newPath =
                    if (path != "") path + "." + ele.label else ele.label
                  (ele.label, appendNode(newPath, parent, ele))
                case t: Text => ("text", null)
              }.filter { case(k, v) => v != null }.toMap
            mapInScala.foldLeft(parent) { case(c, (k, v)) =>
              c.withValue(k, v) }.root()
          }
        case t: Text =>
          if (t.text.trim != "") ConfigValueFactory.fromAnyRef(t.text.trim)
          else null
        case n: Node => {
          assert(false)
          null
        }
      }

    val empty = ConfigFactory.empty();
    val childConfs = 
      elem.child.map { ch => (ch.label, appendNode(ch.label, empty, ch)) }
    childConfs.foldLeft(ConfigFactory.empty()) { case(c, (k, v)) =>
      if (v != null) c.withValue(k, v) else c }
  }

  private def singular(s: String): String = 
    if (s.endsWith("ies")) s.substring(0, s.length - 3) + "y" 
    else s.substring(0, s.length - 1)

  private def removeQuotes(s: String): String =
    if (!s.startsWith("\"") || !s.endsWith("\"")) s
    else s.substring(1, s.length - 1)

  private def isDependencyProperty(key: String): Boolean = key match {
    case "groupId" | "artifactId" | "version" | "scope" | "optional" => true
    case _ => false
  }

  def configToElem(config: Config): Elem = {
    import scala.collection.JavaConverters._

    val children: Seq[java.util.Map.Entry[String, ConfigValue]] =
      config.entrySet().asScala.toSeq

    def makeElem(key: String, value: ConfigValue): Elem = value match {
      case l: ConfigList => 
        val childElems = l.asScala.map { it => makeElem(singular(key), it) }
        new Elem(null, key, Null, TopScope, childElems: _*)
      case m: ConfigObject =>
        val mS = m.asScala
        var elemKey = key
        val childElems =
          if (mS.values.forall { v => 
            v.valueType() == ConfigValueType.STRING }) {
            if (mS.keySet.forall { isDependencyProperty(_) }) {
              // a Dependency object (for configurations)
              elemKey = "dependency"
              mS.map { case(k, v) => new Elem(
                null, k, Null, TopScope, Text(removeQuotes(v.render()))) }.toSeq
            } else {
              // a Properties object
              mS.map { case(k, v) =>
                new Elem(
                  null, "property", Null, TopScope,
                  new Elem(null, "name", Null, TopScope, new Text(k)),
                  new Elem(
                    null, "value", Null, TopScope,
                    new Text(removeQuotes(v.render()))))
              }.toSeq
            }
          } else {
            // a normal Map (ie non-string value type)
            mS.map { case(k, v) => makeElem(k, v) }.toSeq
          }
        new Elem(null, elemKey, Null, TopScope, childElems: _*)
      case v: ConfigValue => 
        new Elem(null, key, Null, TopScope, new Text(removeQuotes(v.render())))
    }

    val childElems: Seq[Elem] = 
      children.map { entry => makeElem(entry.getKey(), entry.getValue()) }
    new Elem(null, "configuration", Null, TopScope, childElems: _*)
  }

  def jsonToConfig(jobject: JObject): Config = {
    import scala.collection.JavaConverters._

    def fromJson(v: JValue): Any =
      v match {
        case s: JString => s.s
        case b: JBool => b.value
        case d: JDecimal => d.num
        case d: JDouble => d.num
        case i: JInt => i.num
        case o: JObject => ConfigValueFactory.fromMap(fromJsonObject(o))
        case a: JArray =>
          val iter = a.arr.map { fromJson(_) }.asJava
          ConfigValueFactory.fromIterable(iter)
        case v: JValue => ???
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
        case _ => ???
      }

    def makeJValue(value: ConfigValue): JValue = value match {
      case l: ConfigList => 
        JArray(l.asScala.map { it => makeJValue(it) }.toList)
      case m: ConfigObject =>
        JObject(m.asScala.map { it => (it._1, makeJValue(it._2)) }.toList)
      case _ => makeJValuePrimitive(value)
    }

    if (conf != null) {
      val children: Seq[java.util.Map.Entry[String, ConfigValue]] =
        conf.entrySet().asScala.toSeq
      val childElems: Seq[JField] =
        children.map { entry => (entry.getKey(), makeJValue(entry.getValue())) }
      new JObject(childElems.toList)
    } else {
      null
      //JNull
    }
  }

  object SL extends Labels
}
