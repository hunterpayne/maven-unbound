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

import java.io.{
  File, FileOutputStream, OutputStream, OutputStreamWriter, Writer }
import java.util.{ Locale, Properties }

import scala.util.Try
import scala.xml.{ Elem, PrettyPrinter, XML }

import com.typesafe.config.{ Config, ConfigRenderOptions }

/**
  * A trait case classes can extend to get the ability to write themselves
  * as XML provided they have a field named xml of type Elem
  */
trait Writable {

  val xml: Elem

  private val nl = System.lineSeparator

  // max width: 80 chars
  // indent:     2 spaces
  /**
    * returns a string containing a pretty printed XML document represented
    * by the field xml
    */
  def toXmlString: String = {
    val printer = new PrettyPrinter(80, 2)
    val sb = new StringBuilder()
    printer.format(xml, sb)
    sb.toString()
  }

  /** writes this object into the provided Writer */
  def writePOM(writer: Writer): Unit = writer.write(toXmlString)

  /** writes this object to the specified file using UTF-8 character encoding */
  def writePOM(filename: String): Unit = {
    val writer =
      new OutputStreamWriter(new FileOutputStream(new File(filename)), "UTF-8")
    try {
      writePOM(writer)
      writer.flush()
    } finally { writer.close() }
  }

  /** writes this object to the specified stream using the specified encoding */
  def writePOM(os: OutputStream, enc: String): Unit = {
    val writer = new OutputStreamWriter(os, enc)
    try { writePOM(writer) } finally { writer.close() }
  }

  def encodeJson(s: String): String =
    s.flatMap { c => c match {
      case '\b' => Seq('\\', 'b')
      case '\f' => Seq('\\', 'f')
      case '\n' => Seq('\\', 'n')
      case '\r' => Seq('\\', 'r')
      case '\t' => Seq('\\', 't')
      case '"' => Seq('\\', '"')
      case '\\' => Seq('\\', '\\')
      case c => Seq(c)
    }}.toString

  // what represents a tab in the serialized Hocon format produced by this class
  val spacer = "  "

  def keyName: String = {
    val fullName = getClass.getName
    fullName.substring(fullName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT)
  }

  import scala.reflect.{ classTag, ClassTag }
  import scala.reflect.runtime.universe._
  import scala.reflect.api.{ StandardDefinitions, Symbols, StandardNames }

  /**
    * Serializes this class to a String in the Hocon format
    */
  def instanceToString[T: TypeTag](
    instance: T, cmts: DocComments = DocComments(Seq()))(
    implicit ct: ClassTag[T]): String = {
    val sb = new StringBuilder("{ ")
    sb.append(keyName)
    sb.append(" ")
    subToString[T](instance, sb, cmts, List(ElementLabel(keyName)), 1, false)
    sb.append("}")
    sb.append(nl)
    sb.toString
  }

  private def subToString[T: TypeTag](
    instance: T, sb: StringBuilder,
    comments: DocComments, path: List[PathElement],
    depth: Int = 0, tab: Boolean = true)(
    implicit ct: ClassTag[T]): Unit = {

    def writeElem(
      innerClassname: String, v: Any, path: List[PathElement],
      key: Option[String] = None,
      dp: Int = depth + 2, tab: Boolean = true): Unit =
      innerClassname match {
        case "org.apache.maven.unbound.Exclusion" =>
          subToString[Exclusion](
            v.asInstanceOf[Exclusion], sb, comments,
            path ::: List(ElementLabel(SL.ExclusionStr)), dp, tab)
        case "org.apache.maven.unbound.Build" =>
          subToString[Build](
            v.asInstanceOf[Build], sb, comments,
            path ::: List(ElementLabel(SL.BuildStr)), dp, tab)
        case "org.apache.maven.unbound.Resource" =>
          subToString[Resource](
            v.asInstanceOf[Resource], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.Parent" =>
          subToString[Parent](
            v.asInstanceOf[Parent], sb, comments,
            path ::: List(ElementLabel(SL.ParentStr)), dp, tab)
        case "org.apache.maven.unbound.Organization" =>
          subToString[Organization](
            v.asInstanceOf[Organization], sb, comments,
            path ::: List(ElementLabel(SL.OrganizationStr)), dp, tab)
        case "org.apache.maven.unbound.Scm" =>
          subToString[Scm](v.asInstanceOf[Scm], sb, comments,
            path ::: List(ElementLabel(SL.ScmStr)), dp, tab)
        case "org.apache.maven.unbound.IssueManagement" =>
          subToString[IssueManagement](
            v.asInstanceOf[IssueManagement], sb, comments,
            path ::: List(ElementLabel(SL.IssueManagementStr)), dp, tab)
        case "org.apache.maven.unbound.CIManagement" =>
          subToString[CIManagement](
            v.asInstanceOf[CIManagement], sb, comments,
            path ::: List(ElementLabel(SL.CIManagementStr)), dp, tab)
        case "org.apache.maven.unbound.Notifier" =>
          subToString[Notifier](
            v.asInstanceOf[Notifier], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.Plugin" =>
          subToString[Plugin](
            v.asInstanceOf[Plugin], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.Execution" =>
          subToString[Execution](
            v.asInstanceOf[Execution], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.Reporting" =>
          subToString[Reporting](
            v.asInstanceOf[Reporting], sb, comments,
            path ::: List(ElementLabel(SL.ReportingStr)), dp, tab)
        case "org.apache.maven.unbound.ReportPlugin" =>
          subToString[ReportPlugin](
            v.asInstanceOf[ReportPlugin], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.ReportSet" =>
          subToString[ReportSet](
            v.asInstanceOf[ReportSet], sb, comments,
            path ::: List(ElementLabel(SL.ReportSetStr)), dp, tab)
        case "org.apache.maven.unbound.License" =>
          subToString[License](
            v.asInstanceOf[License], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.Developer" =>
          subToString[Developer](
            v.asInstanceOf[Developer], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.Contributor" =>
          subToString[Contributor](
            v.asInstanceOf[Contributor], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.MailingList" =>
          subToString[MailingList](
            v.asInstanceOf[MailingList], sb, comments,
            path ::: List(ElementLabel(SL.MailingListStr)), dp, tab)
        case "org.apache.maven.unbound.Dependency" =>
          subToString[Dependency](
            v.asInstanceOf[Dependency], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.Repository" =>
          subToString[Repository](
            v.asInstanceOf[Repository], sb, comments,
            path ::: List(ElementLabel(SL.RepositoryStr)), dp, tab)
        case "org.apache.maven.unbound.Profile" =>
          subToString[Profile](
            v.asInstanceOf[Profile], sb, comments, path, dp, tab)
        case "org.apache.maven.unbound.Site" =>
          subToString[Site](
            v.asInstanceOf[Site], sb, comments,
            path ::: List(ElementLabel(SL.SiteStr)), dp, tab)
        case "org.apache.maven.unbound.DistributionManagement" =>
          subToString[DistributionManagement](
            v.asInstanceOf[DistributionManagement], sb, comments,
            path ::: List(ElementLabel(SL.DistributionManagementStr)), dp, tab)
        case "org.apache.maven.unbound.Relocation" =>
          subToString[Relocation](
            v.asInstanceOf[Relocation], sb, comments,
            path ::: List(ElementLabel(SL.RelocationStr)), dp, tab)
        case "org.apache.maven.unbound.DeploymentRepository" =>
          subToString[DeploymentRepository](
            v.asInstanceOf[DeploymentRepository], sb, comments,
            path ::: List(ElementLabel(key.get)), dp, tab)
        case "org.apache.maven.unbound.RepositoryPolicy" =>
          subToString[RepositoryPolicy](
            v.asInstanceOf[RepositoryPolicy], sb, comments,
            path ::: List(ElementLabel(key.get)), dp, tab)
        case "org.apache.maven.unbound.BuildBase" =>
          subToString[BuildBase](
            v.asInstanceOf[BuildBase], sb, comments,
            path ::: List(ElementLabel(SL.BuildStr)), dp, tab)
        case "org.apache.maven.unbound.Activation" =>
          subToString[Activation](
            v.asInstanceOf[Activation], sb, comments,
            path ::: List(ElementLabel(SL.ActivationStr)), dp, tab)
        case "org.apache.maven.unbound.ActivationOS" =>
          subToString[ActivationOS](
            v.asInstanceOf[ActivationOS], sb, comments,
            path ::: List(ElementLabel(SL.OS)), dp, tab)
        case "org.apache.maven.unbound.ActivationProperty" =>
          subToString[ActivationProperty](
            v.asInstanceOf[ActivationProperty], sb, comments,
            path ::: List(ElementLabel(SL.PropertyStr)), dp, tab)
        case "org.apache.maven.unbound.ActivationFile" =>
          subToString[ActivationFile](
            v.asInstanceOf[ActivationFile], sb, comments,
            path ::: List(ElementLabel(SL.FileStr)), dp, tab)
        case "org.apache.maven.unbound.Archiver" =>
          subToString[Archiver](
            v.asInstanceOf[Archiver], sb, comments,
            path ::: List(ElementLabel(SL.Archive)), dp, tab)
        case "org.apache.maven.unbound.ManifestObj" =>
          subToString[ManifestObj](
            v.asInstanceOf[ManifestObj], sb, comments,
            path ::: List(ElementLabel(SL.ManifestStr)), dp, tab)
        case "org.apache.maven.unbound.ManifestSection" =>
          subToString[ManifestSection](
            v.asInstanceOf[ManifestSection], sb, comments,
            path ::: List(ElementLabel(SL.ManifestSectionStr)), dp, tab)
        case "org.apache.maven.unbound.Fileset" =>
          subToString[Fileset](
            v.asInstanceOf[Fileset], sb, comments,
            path ::: List(ElementLabel(SL.FilesetStr)), dp, tab)
        case c => println("unknown class " + c)
      }

    def writeKey(key: String): Unit = {
      sb.append(spacer * (depth + 1))
      sb.append(key)
    }

    def insertComments(
      currPath: List[PathElement], dp: Int = depth + 1): Unit =
      comments.comments.filter { _.path.elems.toList == currPath }.foreach {
        case comment: Comments =>
          comment.s.foreach { str =>
            sb.append(spacer * dp)
            sb.append("# ")
            sb.append(str)
            sb.append(nl)
          }
      }

    def outputValue(
      method: MethodSymbol, key: String, value: Any): Unit = {

      // inserts comments from parsed from XML here
      val currPath = path ::: List(ElementLabel(key))
      insertComments(currPath)

      method.returnType.toString match {
        case "Boolean" =>
          writeKey(key)
          sb.append(" = ")
          if (value.asInstanceOf[Boolean]) sb.append("true")
          else sb.append("false")
          sb.append(nl)

        case "Int" | "Long" | "Float" | "Double" =>
          writeKey(key)
          sb.append(" = ")
          sb.append(value.toString)
          sb.append(nl)

        case "String" =>
          writeKey(key)
          sb.append(" = ")
          sb.append("\"")
          sb.append(encodeJson(value.toString))
          sb.append("\"")
          sb.append(nl)

        case "Seq[String]" =>
          val lst = value.asInstanceOf[Seq[String]]
          // get comments that are interspearsed in the list of strings in XML
          comments.comments.filter { comment =>
            comment.path.elems.toList.startsWith(currPath) &&
            currPath.size < comment.path.elems.size }.foreach {
            case comment: Comments =>
              comment.s.foreach { str =>
                sb.append(spacer * (depth + 1))
                sb.append("# ")
                sb.append(str)
                sb.append(nl)
              }
          }
          if (!lst.isEmpty) {
            writeKey(key)
            sb.append(" = [ ")

            val last = lst.last
            value.asInstanceOf[Seq[String]].foreach { c =>
              sb.append("\"")
              sb.append(encodeJson(c))
              sb.append("\"")
              if (c != last) sb.append(", ")
            }
            sb.append(" ]")
            sb.append(nl)
          }

        case lstClz if (lstClz.startsWith("Seq[")) =>
          val lst = value.asInstanceOf[Seq[_]]
          if (!lst.isEmpty) {
            val innerClassname = lstClz.substring(4, lstClz.length - 1)
            writeKey(key)
            sb.append(" = [")
            sb.append(nl)

            val last = lst.last
            lst.zipWithIndex.foreach { case(v, idx) =>
              val idxPath = currPath ::: List(ListIndex(idx))
              insertComments(idxPath, depth + 2)
              writeElem(innerClassname, v, idxPath)
            }
            sb.append(spacer * (depth + 1))
            sb.append("]")
            sb.append(nl)
          }

        case "Properties" =>
          val props = value.asInstanceOf[Properties]
          if (!props.isEmpty) {
            writeKey(key)
            sb.append(" {")
            sb.append(nl)
            var idx = 0
            props.stringPropertyNames().iterator().forEachRemaining { k =>
              insertComments(currPath ::: List(ListIndex(idx)), depth + 2)

              val v = props.getProperty(k)
              sb.append(spacer * (depth + 1))
              sb.append(k)
              sb.append(" = \"")
              sb.append(encodeJson(v))
              sb.append("\"")
              sb.append(nl)
              idx = idx + 1
            }
            sb.append(spacer * (depth + 1))
            sb.append("}")
            sb.append(nl)
          }

        case "Map[String,String]" =>
          val strMap = value.asInstanceOf[Map[String, String]]
          if (!strMap.isEmpty) {
            writeKey(key)
            sb.append(" {")
            sb.append(nl)
            var idx = 0
            strMap.foreach { case(k, v) =>
              insertComments(currPath ::: List(ElementLabel(k)), depth + 2)
              insertComments(currPath ::: List(ListIndex(idx)), depth + 2)
              sb.append(spacer * (depth + 2))
              sb.append("\"")
              sb.append(k)
              sb.append("\" = \"")
              sb.append(encodeJson(v))
              sb.append("\"")
              sb.append(nl)
              idx = idx + 1
            }
            sb.append(spacer * (depth + 1))
            sb.append("}")
            sb.append(nl)
          }

        case mapClz if (mapClz.startsWith("Map[String,")) =>
          val objMap = value.asInstanceOf[Map[String, _]]
          if (!objMap.isEmpty) {
            writeKey(key)
            val innerClassname = mapClz.substring(11, mapClz.length - 1)
            sb.append(" {")
            sb.append(nl)
            objMap.foreach { case(k, v) =>
              val keyPath = currPath ::: List(ElementLabel(k))
              insertComments(keyPath, depth + 2)
              sb.append(k)
              sb.append(" = ")
              writeElem(innerClassname, v, keyPath, Some(k))
            }
            sb.append(spacer * (depth + 1))
            sb.append("}")
            sb.append(nl)
          }

        case "com.typesafe.config.Config" =>
          val conf = value.asInstanceOf[Config]
          if (!conf.isEmpty) {
            writeKey(key)
            sb.append(" ")
            val options =
              ConfigRenderOptions.defaults().setOriginComments(false)
            // val rendered = conf.root().render(options)
            // need to take off the first currPath.size PathElements off the
            // paths b/c otherwise the comment insertion doesn't work
            val filteredComments =
              DocComments(comments.comments.flatMap { case comment: Comments =>
                if (comment.path.elems.toList.startsWith(currPath)) {
                  val cPath = comment.path.elems.toSeq
                  val suffixPath = cPath.slice(currPath.size, cPath.size)
                  Some(Comments(comment.s, CommentPath(suffixPath: _*)))
                } else None
              })
            val rendered =
              filteredComments.insertConf(conf).root().render(options)

            sb.append(rendered.split(Array('\n', '\r')).
              mkString(nl + (spacer * (1 + depth))))
            sb.append(nl)
          }

        case clz =>
          writeKey(key)
          sb.append(" ")
          writeElem(clz, value, path, Some(key), depth + 1, false)
      }
    }

    def defaut(im: InstanceMirror, name: String): Seq[Any] = {
      val at = newTermName(name)
      val ts = im.symbol.typeSignature
      val method = (ts.member(at)).asMethod

      // get default value for argument at index i with name p
      def valueFor(p: Symbol, i: Int): Any = {
        val defarg = ts.member(newTermName(s"$name$$default$$${i + 1}"))
        require(
          defarg != NoSymbol,
          p.toString + " of " + instance + " has no default value")
        (im.reflectMethod(defarg.asMethod))()
      }

      (for (ps <- method.paramss; p <- ps) yield p).zipWithIndex.map { p =>
        valueFor(p._1, p._2) }
    }

    val mirror = runtimeMirror(instance.getClass.getClassLoader())
    val tpe = typeOf[T]
    val classSymbol = tpe.typeSymbol.asClass

    val companion =
      mirror.reflectModule(classSymbol.companion.asModule).instance
    val companionMirror = mirror.reflect(companion)
    val defValues = defaut(companionMirror, "apply").iterator

    val instMirror = mirror.reflect(instance)

    val methods = tpe.members.flatMap {
      case m: MethodSymbol if m.isCaseAccessor => Some(m)
      case _ => None
    }.toSeq

    if (tab && depth > 0) sb.append(spacer * depth)
    sb.append("{")
    sb.append(nl)

    methods.reverse.foreach { method =>
      val value = instMirror.reflectMethod(method.asMethod)()
      require(defValues.hasNext)
      val defValue = defValues.next
      if (value != null && value != defValue)
        outputValue(method, method.name.toString, value)
    }

    if (depth > 0) sb.append(spacer * depth)
    sb.append("}")
    sb.append(nl)
  }
}
