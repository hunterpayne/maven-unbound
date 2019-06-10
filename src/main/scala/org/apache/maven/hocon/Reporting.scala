
package org.apache.maven.hocon

import java.io.StringReader

import scala.xml.Elem

import com.typesafe.config.{ Config, ConfigObject, ConfigFactory }

import org.apache.maven.shared.utils.xml.Xpp3DomBuilder

import org.json4s._

case object Reporting extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ReportingSerializer extends CustomSerializer[Reporting](format => (
    {
      case obj @ JObject(fields) =>
        Reporting(
          readBool(fields, ExcludeDefaults).getOrElse(false),
          readStr(fields, OutputDirectory).getOrElse(SiteStr),
          readObjectSequence[ReportPlugin](fields, Plugins)
        )
    },
    {
      case r: Reporting =>
        JObject(
          JField(ExcludeDefaults, JBool(r.excludeDefaults)) ::
          JField(OutputDirectory, JString(r.outputDirectory)) ::
          JField(
            Plugins,
            JArray(r.plugins.map { e => Extraction.decompose(e) }.toList)) ::
          Nil)
    }
  ))
}

case class Reporting(
  excludeDefaults: Boolean = false, outputDirectory: String = SL.SiteStr, 
  plugins: Seq[ReportPlugin] = Seq[ReportPlugin]()) {

  def this(elem: Elem) = this(
    emptyToDefault(
      (elem \ SL.ExcludeDefaults).text.toLowerCase, SL.FalseStr) == 
      SL.TrueStr.toString,
    emptyToDefault((elem \ SL.OutputDirectory).text, SL.SiteStr),
    (elem \ SL.Plugins \ SL.PluginStr).map { case e: Elem => 
      new ReportPlugin(e) })

  lazy val xml = 
    <reporting>
      { if (excludeDefaults) <excludeDefaults>true</excludeDefaults> }
      { if (outputDirectory != null && outputDirectory != SL.SiteStr.toString)
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

  class ReportPluginSerializer 
      extends CustomSerializer[ReportPlugin](format => (
    {
      case obj @ JObject(fields) =>
        ReportPlugin(
          readStr(fields, GroupId).getOrElse(DefaultPluginGroup),
          readStr(fields, ArtifactId).get,
          readStr(fields, Version).get,
          readObjectSequence[ReportSet](fields, ReportSets),
          readBool(fields, Inherited).getOrElse(true),
          (obj \ Configuration) match {
            case o: JObject => jsonToConfig(o)
            case _ => null //ConfigFactory.empty()
          }
        )
    },
    {
      case p: ReportPlugin =>
        JObject(
          JField(GroupId, JString(p.groupId)) ::
          JField(ArtifactId, JString(p.artifactId)) ::
          JField(Version, JString(p.version)) ::
          JField(
            ReportSets, 
            JArray(p.reportSets.map { e => Extraction.decompose(e) }.toList)) ::
          JField(Inherited, JBool(p.inherited)) :: 
          JField(Configuration, configToJson(p.configuration)) ::
          Nil)
    }
  ))
}

case class ReportPlugin(
  groupId: String = SL.DefaultPluginGroup, 
  artifactId: String, version: String,
  reportSets: Seq[ReportSet] = Seq[ReportSet](), 
  inherited: Boolean = true,
  configuration: Config = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.GroupId).text, SL.DefaultPluginGroup),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text),
    (elem \ SL.ReportSets \ SL.ReportSetStr).map { case e: Elem => 
      new ReportSet(e)},
    emptyToDefault((elem \ SL.Inherited).text.toLowerCase, SL.TrueStr) == 
      SL.TrueStr.toString,
    (elem \ SL.Configuration).headOption.map { case e: Elem => 
      elemToConfig(e) }.getOrElse(null))

  lazy val xml = <plugin>
                   <groupId>{groupId}</groupId>
                   <artifactId>{artifactId}</artifactId>
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
        (new Writeable {
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
            case _ => null //ConfigFactory.empty()
          }
        )
    },
    {
      case r: ReportSet =>
        JObject(
          JField(Id, JString(r.id)) ::
          JField(Reports, JArray(r.reports.map { JString(_) }.toList)) ::
          JField(Inherited, JBool(r.inherited)) :: 
          JField(Configuration, configToJson(r.configuration)) ::
          Nil)
    }
  ))
}

case class ReportSet(
  id: String = SL.DefaultStr, reports: Seq[String] = Seq[String](), 
  inherited: Boolean = true, configuration: Config = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.Id).text, SL.DefaultStr),
    (elem \ SL.Reports \ SL.ReportStr).map { _.text },
    emptyToDefault((elem \ SL.Inherited).text.toLowerCase, SL.TrueStr) == 
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
        (new Writeable {
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

