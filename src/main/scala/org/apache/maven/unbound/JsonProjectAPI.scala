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
  InputStream, InputStreamReader,
  OutputStream, OutputStreamWriter, Reader, Writer }

import scala.reflect.Manifest

import com.typesafe.config.{ Config, ConfigFactory, ConfigObject }
import org.json4s._
import org.json4s.native.JsonMethods
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{ read, write, writePretty }

trait CommonJsonReader extends Labels {

  implicit val formats: Formats
  implicit val boolReader = DefaultReaders.BooleanReader
  implicit val strReader = DefaultReaders.StringReader

  protected def readBool(fields: List[JField], key: String): Option[Boolean] =
    fields.filter { _._1 == key }.headOption.map { case(_, v) => v.as[Boolean] }

  protected def readStr(fields: List[JField], key: String): Option[String] =
    fields.filter { _._1 == key }.headOption.map { case(_, v) => v match {
      case JString(s) => s.replaceAllLiterally("\n", scala.compat.Platform.EOL)
      case v =>
        v.as[String].replaceAllLiterally("\n", scala.compat.Platform.EOL)
    }}

  protected def readObject[T](obj: JObject, key: String, defVa: T = null)(
    implicit m: Manifest[T]): T =
    (obj \ key) match {
      case obj2: JObject => Extraction.extract[T](obj2)
      case _ => defVa
    }

  protected def readProperties(
    obj: JObject, key: String = "properties"): Map[String, String] =
    (obj \ key) match {
      case JObject(fields) =>
        fields.map { case((key, v)) => (key, v.as[String]) }.toMap
      case _ => Map[String, String]()
    }

  protected def readObjectSequence[T](
    fields: List[JField], key: String,
    defVal: Seq[T] = Seq[T]())(implicit m: Manifest[T]): Seq[T] =
    fields.filter { _._1 == key }.headOption.map { e =>
      e._2.children.map { Extraction.extract[T](_) }}.getOrElse(defVal)

  protected def readStringSequence(
    fields: List[JField], key: String): Seq[String] =
    fields.filter { _._1 == key }.headOption.map { goals =>
      goals._2.children.map { goal => Extraction.extract[String](goal) }
    }.getOrElse(Seq[String]())

  protected def writeBool(
    name: String, b: Boolean, defVal: Boolean): Option[JField] =
    if (b != defVal) Some((name, JBool(b)))
    else None

  protected def writeStr(
    name: String, s: String, defVal: String = null): Option[JField] = {
    if (s != null && s != defVal) Some((name, JString(s)))
    else None
  }

  protected def writeObject[T](
    name: String, t: T, defVal: T = null): Option[JField] =
    if (t != null && t != defVal) Some((name, Extraction.decompose(t)))
    else None

  protected def writeProperties(
    name: String, p: Map[String, String]): Option[JField] =
    if (!p.isEmpty)
      Some((name, JObject(p.map { case(k, v) => (k, JString(v)) }.toList)))
    else None

  protected def writeObjectSequence[T](
    name: String, arr: Seq[T], defVal: Seq[T] = Seq[T]()): Option[JField] =
    if (arr != null && arr != defVal)
      Some((name, JArray(arr.map { Extraction.decompose(_) }.toList)))
    else None

  protected def writeStringSequence(
    name: String, v: Seq[String]): Option[JField] =
    if (v != null && !v.isEmpty)
      Some((name, JArray(v.map { JString(_) }.toList)))
    else None
}

trait JsonProjectAPI extends JsonMethods with CommonJsonReader {

  implicit val formats =
    Serialization.formats(NoTypeHints) +
    new Project.ProjectSerializer +
    new Build.BuildSerializer +
    new Plugin.PluginSerializer +
    new Dependency.DependencySerializer +
    new Execution.ExecutionSerializer +
    new Reporting.ReportingSerializer +
    new ReportPlugin.ReportPluginSerializer +
    new Resource.ResourceSerializer +
    new CIManagement.CIManagementSerializer +
    new Notifier.NotifierSerializer +
    new Contributor.ContributorSerializer +
    new Developer.DeveloperSerializer +
    new MailingList.MailingListSerializer +
    new Parent.ParentSerializer +
    new License.LicenseSerializer +
    new Scm.ScmSerializer +
    new IssueManagement.IssueManagementSerializer +
    new Site.SiteSerializer +
    new DistributionManagement.DistributionManagementSerializer +
    new Relocation.RelocationSerializer +
    new DistributionRepository.DistributionRepositorySerializer +
    new Repository.RepositorySerializer +
    new RepositoryPolicy.RepositoryPolicySerializer +
    new ReportSet.ReportSetSerializer +
    new BuildBase.BuildBaseSerializer +
    new Activation.ActivationSerializer +
    new ActivationOS.ActivationOSSerializer +
    new Profile.ProfileSerializer +
    new Archiver.ArchiverSerializer +
    new ManifestObj.ManifestSerializer +
    new ManifestSection.ManifestSectionSerializer
}

object JsonReader extends JsonProjectAPI {

  def readPOM(jsonSrc: String): Project = read[Project](jsonSrc)

  def readPOM(reader: Reader): Project = read[Project](reader)

  def readPOM(in: InputStream, enc: String = "UTF-8"): Project =
    read[Project](new InputStreamReader(in, enc))
}

object JsonWriter extends JsonProjectAPI {

  def writeConcisePOM(project: Project): String = write[Project](project)

  def writePOM(project: Project): String = writePretty(project)

  def writePOM[W <: Writer](project: Project, writer: W): Unit =
    writePretty[Project, W](project, writer)

  def writePOM(
    project: Project, os: OutputStream, enc: String = "UTF-8"): Unit =
    writePretty[Project, OutputStreamWriter](
      project, new OutputStreamWriter(os, enc))

  def writeArchiver(archiver: Archiver): String = writePretty(archiver)
}
