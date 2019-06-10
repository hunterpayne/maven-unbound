
package org.apache.maven.unbound

import java.io.StringReader

import scala.xml.Elem

import com.typesafe.config.{ Config, ConfigObject, ConfigFactory }

import org.apache.maven.shared.utils.xml.Xpp3DomBuilder

import org.json4s._

case object Plugin extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class PluginSerializer extends CustomSerializer[Plugin](format => (
    {
      case obj @ JObject(fields) =>
        new Plugin(
          readStr(fields, GroupId).getOrElse(DefaultPluginGroup),
          readStr(fields, ArtifactId).get,
          readStr(fields, Version).getOrElse(null),
          readBool(fields, Extensions).getOrElse(false),
          readObjectSequence[Execution](fields, Executions),
          readObjectSequence[Dependency](fields, Dependencies),
          readBool(fields, Inherited).getOrElse(true),
          (obj \ Configuration) match {
            case o: JObject => jsonToConfig(o)
            case _ => null //ConfigFactory.empty()
          }
        )
    },
    {
      case p: Plugin =>
        JObject(
          JField(GroupId, JString(p.groupId)) ::
          JField(ArtifactId, JString(p.artifactId)) ::
          JField(Version, JString(p.version)) ::
          JField(Extensions, JBool(p.extensions)) :: 
          JField(
            Executions, 
            JArray(p.executions.map { e => Extraction.decompose(e) }.toList)) ::
          JField(
            Dependencies, 
            JArray(p.dependencies.map { d => 
              Extraction.decompose(d) }.toList)) ::
          JField(Inherited, JBool(p.inherited)) :: 
          JField(Configuration, configToJson(p.configuration)) ::
          Nil
        )
    }
  ))
}

case class Plugin(
  groupId: String = SL.DefaultPluginGroup, 
  artifactId: String, version: String = null, 
  extensions: Boolean = false,
  executions: Seq[Execution] = Seq[Execution](), 
  dependencies: Seq[Dependency] = Seq[Dependency](), 
  inherited: Boolean = true,
  configuration: Config = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.GroupId).text, SL.DefaultPluginGroup),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text), 
    emptyToDefault((elem \ SL.Extensions).text.toLowerCase, SL.FalseStr) == 
      SL.TrueStr.toString,
    (elem \ SL.Executions \ SL.ExecutionStr).map { case e: Elem => 
      new Execution(e) },
    (elem \ SL.Dependencies \ SL.DependencyStr).map { case e: Elem => 
      new Dependency(e) },
    emptyToDefault((elem \ SL.Inherited).text.toLowerCase, SL.TrueStr) == 
      SL.TrueStr.toString,
    (elem \ SL.Configuration).headOption.map { case e: Elem =>
      elemToConfig(e) }.getOrElse(null))

  lazy val xml = <plugin>
                   <groupId>{groupId}</groupId>
                   <artifactId>{artifactId}</artifactId>
                   { if (version != null) <version>{version}</version> }
                   { if (extensions) <extensions>true</extensions> }
                   { if (!executions.isEmpty) <executions>
                       { executions.map { _.xml } }
                     </executions> }
                   { if (!dependencies.isEmpty) <dependencies>
                       { dependencies.map { _.xml } }
                     </dependencies> }
                   { if (!inherited) <inherited>false</inherited> }
                   { if (configuration != null) configToElem(configuration) }
                 </plugin>

  def makeModelObject(): org.apache.maven.model.Plugin = {
    val plugin = new org.apache.maven.model.Plugin()
    plugin.setGroupId(groupId)
    plugin.setArtifactId(artifactId)
    plugin.setVersion(version)
    plugin.setExtensions(extensions)
    executions.foreach { ex => plugin.addExecution(ex.makeModelObject()) }
    dependencies.foreach { d => plugin.addDependency(d.makeModelObject()) }
    plugin.setInherited(inherited)
    if (configuration != null) {
      val xmlStr = 
        (new Writeable {
          val xml: Elem = configToElem(configuration)
        }).toXmlString
      val sReader = new StringReader(xmlStr)
      plugin.setConfiguration(Xpp3DomBuilder.build(sReader))
    }
    plugin
  }
}

case object Execution extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ExecutionSerializer extends CustomSerializer[Execution](format => (
    {
      case obj @ JObject(fields) =>
        new Execution(
          readStr(fields, Id).getOrElse(DefaultStr),
          readStr(fields, Phase).getOrElse(null),
          readStringSequence(fields, Goals),
          readBool(fields, Inherited).getOrElse(true),
          (obj \ Configuration) match {
            case o: JObject => jsonToConfig(o)
            case _ => null //ConfigFactory.empty()
          }
        )
    },
    {
      case e: Execution =>
        JObject(
          JField(Id, JString(e.id)) ::
          JField(Phase, JString(e.phase)) ::
          JField(
            Goals,
            JArray(e.goals.map { goal => JString(goal) }.toList)) ::
          JField(Inherited, JBool(e.inherited)) :: 
          JField(Configuration, configToJson(e.configuration)) ::
          Nil
        )
    }
  ))
}

case class Execution(
  id: String = SL.DefaultStr, phase: String = null, 
  goals: Seq[String] = Seq[String](), inherited: Boolean = true,
  configuration: Config = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.Id).text, SL.DefaultStr), 
    emptyToNull((elem \ SL.Phase).text),
    (elem \ SL.Goals \ SL.GoalStr).map { _.text }, 
    emptyToDefault((elem \ SL.Inherited).text.toLowerCase, SL.TrueStr) == 
      SL.TrueStr.toString,
    (elem \ SL.Configuration).headOption.map { case e: Elem => 
      elemToConfig(e) }.getOrElse(null))

  lazy val xml = <execution>
                   <id>{id}</id>
                   { if (phase != null) <phase>{phase}</phase> }
                   { if (!goals.isEmpty) <goals>
                     { goals.map { Goal(_).xml } }
                   </goals> }
                   { if (!inherited) <inherited>false</inherited> }
                   { if (configuration != null) configToElem(configuration) }
                 </execution>

  def makeModelObject(): org.apache.maven.model.PluginExecution = {
    val exec = new org.apache.maven.model.PluginExecution()
    exec.setId(id)
    if (phase != null) exec.setPhase(phase)
    goals.foreach { goal => exec.addGoal(goal) }
    exec.setInherited(inherited)
    if (configuration != null) {
      val xmlStr = 
        (new Writeable {
          val xml: Elem = configToElem(configuration)
        }).toXmlString
      val sReader = new StringReader(xmlStr)
      exec.setConfiguration(Xpp3DomBuilder.build(sReader))
    }
    exec
  }
}

case class Goal(goal: String) {
  lazy val xml = <goal>{goal}</goal>
}

