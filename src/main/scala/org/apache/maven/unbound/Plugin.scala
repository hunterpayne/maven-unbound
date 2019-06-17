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

import java.io.StringReader
import java.util.Locale

import scala.xml.Elem

import com.typesafe.config.{ Config, ConfigFactory, ConfigObject }
import org.json4s._

import org.apache.maven.shared.utils.xml.Xpp3DomBuilder

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
            case _ => null
          }
        )
    },
    {
      case p: Plugin =>
        JObject(Seq[Option[JField]](
          writeStr(GroupId, p.groupId),
          writeStr(ArtifactId, p.artifactId),
          writeStr(Version, p.version),
          writeBool(Extensions, p.extensions, false),
          writeObjectSequence(Executions, p.executions),
          writeObjectSequence(Dependencies, p.dependencies),
          writeBool(Inherited, p.inherited, true),
          writeObject(Configuration, configToJson(p.configuration))
        ).flatten.toList)
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
    emptyToDefault(
      (elem \ SL.Extensions).text.toLowerCase(Locale.ROOT), SL.FalseStr) ==
      SL.TrueStr.toString,
    (elem \ SL.Executions \ SL.ExecutionStr).map { case e: Elem =>
      new Execution(e) },
    (elem \ SL.Dependencies \ SL.DependencyStr).map { case e: Elem =>
      new Dependency(e) },
    emptyToDefault(
      (elem \ SL.Inherited).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
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
            case _ => null
          }
        )
    },
    {
      case e: Execution =>
        JObject(Seq[Option[JField]](
          writeStr(Id, e.id, DefaultStr),
          writeStr(Phase, e.phase),
          writeStringSequence(Goals, e.goals),
          writeBool(Inherited, e.inherited, true),
          writeObject(Configuration, configToJson(e.configuration))
        ).flatten.toList)
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
    emptyToDefault(
      (elem \ SL.Inherited).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
      SL.TrueStr.toString,
    (elem \ SL.Configuration).headOption.map { case e: Elem =>
      elemToConfig(e) }.getOrElse(null))

  lazy val xml = <execution>
                   { if (id != null && id != SL.DefaultStr.toString)
                     <id>{id}</id> }
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

