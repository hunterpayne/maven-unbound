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

import java.io.{ ObjectInputStream, ObjectOutputStream, StringReader }
import java.util.Locale

import scala.xml.Elem

import com.typesafe.config.{ Config, ConfigFactory, ConfigObject }
import org.json4s._

import org.apache.maven.shared.utils.xml.Xpp3DomBuilder

case object Reporting extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ReportingSerializer extends CustomSerializer[Reporting](format => (
    {
      case obj @ JObject(fields) =>
        Reporting(
          readBool(fields, ExcludeDefaults).getOrElse(false),
          readStr(fields, OutputDirectory).getOrElse(Target + "/" + SiteStr),
          readObjectSequence[ReportPlugin](fields, Plugins)
        )
    },
    {
      case r: Reporting =>
        JObject(Seq[Option[JField]](
          writeBool(ExcludeDefaults, r.excludeDefaults, false),
          writeStr(OutputDirectory, r.outputDirectory, Target + "/" + SiteStr),
          writeObjectSequence(Plugins, r.plugins)
        ).flatten.toList)
    }
  ))
}

case class Reporting(
  excludeDefaults: Boolean = false,
  outputDirectory: String = SL.Target + "/" + SL.SiteStr,
  plugins: Seq[ReportPlugin] = Seq[ReportPlugin]()) {

  def this(elem: Elem) = this(
    emptyToDefault(
      (elem \ SL.ExcludeDefaults).text.toLowerCase(Locale.ROOT), SL.FalseStr) ==
      SL.TrueStr.toString,
    emptyToDefault(
      (elem \ SL.OutputDirectory).text, SL.Target + "/" + SL.SiteStr),
    (elem \ SL.Plugins \ SL.PluginStr).map { case e: Elem =>
      new ReportPlugin(e) })

  lazy val xml =
    <reporting>
      { if (excludeDefaults) <excludeDefaults>true</excludeDefaults> }
      { if (outputDirectory != null &&
        outputDirectory != (SL.Target + "/" + SL.SiteStr).toString)
        <outputDirectory>{outputDirectory}</outputDirectory> }
      { if (!plugins.isEmpty) <plugins>
        { plugins.map { _.xml } }
        </plugins> }
    </reporting>

  def makeModelObject(): org.apache.maven.model.Reporting = {
    val reporting = new org.apache.maven.model.Reporting()
    reporting.setExcludeDefaults(
      if (excludeDefaults) SL.TrueStr else SL.FalseStr)
    reporting.setOutputDirectory(outputDirectory)
    plugins.foreach { plugin => reporting.addPlugin(plugin.makeModelObject()) }
    reporting
  }
}

case object ReportPlugin extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ReportPluginSerializer
      extends CustomSerializer[ReportPlugin](format => (
    {
      case obj @ JObject(fields) =>
        ReportPlugin(
          readStr(fields, GroupId).getOrElse(DefaultPluginGroup),
          readStr(fields, ArtifactId).getOrElse(null),
          readStr(fields, Version).getOrElse(null),
          readObjectSequence[ReportSet](fields, ReportSets),
          readBool(fields, Inherited).getOrElse(true),
          (obj \ Configuration) match {
            case o: JObject => jsonToConfig(o)
            case _ => null
          }
        )
    },
    {
      case p: ReportPlugin =>
        JObject(Seq[Option[JField]](
          writeStr(GroupId, p.groupId),
          writeStr(ArtifactId, p.artifactId),
          writeStr(Version, p.version),
          writeObjectSequence(ReportSets, p.reportSets),
          writeBool(Inherited, p.inherited, true),
          writeObject(Configuration, configToJson(p.configuration))
        ).flatten.toList)
    }
  ))
}

case class ReportPlugin(
  groupId: String = SL.DefaultPluginGroup,
  artifactId: String = null, version: String = null,
  reportSets: Seq[ReportSet] = Seq[ReportSet](),
  inherited: Boolean = true,
  configuration: Config = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.GroupId).text, SL.DefaultPluginGroup),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text),
    (elem \ SL.ReportSets \ SL.ReportSetStr).map { case e: Elem =>
      new ReportSet(e)},
    emptyToDefault(
      (elem \ SL.Inherited).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
      SL.TrueStr.toString,
    (elem \ SL.Configuration).headOption.map { case e: Elem =>
      elemToConfig(e) }.getOrElse(null))

  lazy val xml =
    <plugin>
      { if (groupId != null && groupId != SL.DefaultPluginGroup.toString)
        <groupId>{groupId}</groupId> }
      { if (artifactId != null) <artifactId>{artifactId}</artifactId> }
      { if (version != null) <version>{version}</version> }
      { if (!reportSets.isEmpty) <reportSets>
        { reportSets.map { _.xml } }
        </reportSets> }
      { if (!inherited) <inherited>false</inherited> }
      { if (configuration != null) configToElem(configuration) }
    </plugin>

  def makeModelObject(): org.apache.maven.model.ReportPlugin = {
    val plugin = new org.apache.maven.model.ReportPlugin()
    plugin.setGroupId(groupId)
    plugin.setArtifactId(artifactId)
    plugin.setVersion(version)
    reportSets.foreach { rs => plugin.addReportSet(rs.makeModelObject()) }
    if (configuration != null) {
      val xmlStr =
        (new Writable {
          val xml: Elem = configToElem(configuration)
        }).toXmlString
      val sReader = new StringReader(xmlStr)
      plugin.setConfiguration(Xpp3DomBuilder.build(sReader))
    }
    plugin.setInherited(if (inherited) SL.TrueStr else SL.FalseStr)
    plugin
  }
}

case object ReportSet extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ReportSetSerializer
      extends CustomSerializer[ReportSet](format => (
    {
      case obj @ JObject(fields) =>
        ReportSet(
          readStr(fields, Id).getOrElse(DefaultStr),
          readStringSequence(fields, Reports),
          readBool(fields, Inherited).getOrElse(true),
          (obj \ Configuration) match {
            case o: JObject => jsonToConfig(o)
            case _ => null
          }
        )
    },
    {
      case r: ReportSet =>
        JObject(Seq[Option[JField]](
          writeStr(Id, r.id, DefaultStr),
          writeStringSequence(Reports, r.reports),
          writeBool(Inherited, r.inherited, true),
          writeObject(Configuration, configToJson(r.configuration))
        ).flatten.toList)
    }
  ))
}

case class ReportSet(
  id: String = SL.DefaultStr, reports: Seq[String] = Seq[String](),
  inherited: Boolean = true, configuration: Config = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.Id).text, SL.DefaultStr),
    (elem \ SL.Reports \ SL.ReportStr).map { _.text },
    emptyToDefault(
      (elem \ SL.Inherited).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
      SL.TrueStr.toString,
    (elem \ SL.Configuration).headOption.map { case e: Elem =>
      elemToConfig(e) }.getOrElse(null))

  lazy val xml = <reportSet>
                   <id>{id}</id>
                   { if (!inherited) <inherited>false</inherited> }
                   { if (!reports.isEmpty) <reports>
                     { reports.map { Report(_).xml } }
                   </reports> }
                   { if (configuration != null) configToElem(configuration) }
                 </reportSet>

  def makeModelObject(): org.apache.maven.model.ReportSet = {
    val rs = new org.apache.maven.model.ReportSet()
    rs.setId(id)
    rs.setInherited(if (inherited) SL.TrueStr else SL.FalseStr)
    if (configuration != null) {
      val xmlStr =
        (new Writable {
          val xml: Elem = configToElem(configuration)
        }).toXmlString
      val sReader = new StringReader(xmlStr)
      rs.setConfiguration(Xpp3DomBuilder.build(sReader))
    }
    reports.foreach { r => rs.addReport(r) }
    rs
  }
}

case class Report(report: String) {
  lazy val xml = <report>{report}</report>
}

