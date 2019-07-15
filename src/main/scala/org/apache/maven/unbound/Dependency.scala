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

import java.io.{ ObjectInputStream, ObjectOutputStream }
import java.util.Locale

import scala.xml.Elem

import com.typesafe.config.ConfigFactory
import org.json4s._

case object Dependency extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class DependencySerializer extends CustomSerializer[Dependency](format => (
    {
      case JObject(fields) =>
        new Dependency(
          readStr(fields, GroupId).getOrElse(null),
          readStr(fields, ArtifactId).getOrElse(null),
          readStr(fields, Version).getOrElse(null),
          readStr(fields, TypeStr).getOrElse(JarStr),
          readStr(fields, Classifier).getOrElse(null),
          readStr(fields, Scope).getOrElse(Compile),
          readStr(fields, SystemPath).getOrElse(null),
          readObjectSequence[Exclusion](fields, Exclusions),
          readBool(fields, OptionalStr).getOrElse(false))
    },
    {
      case d: Dependency =>
        JObject(Seq[Option[JField]](
          writeStr(GroupId, d.groupId),
          writeStr(ArtifactId, d.artifactId),
          writeStr(Version, d.version),
          writeStr(TypeStr, d.`type`, JarStr),
          writeStr(Classifier, d.classifier),
          writeStr(Scope, d.scope, Compile),
          writeStr(SystemPath, d.systemPath),
          writeObjectSequence(Exclusions, d.exclusions),
          writeBool(OptionalStr, d.optional, false)
        ).flatten.toList)
    }
  ))
}

case class Dependency(
  groupId: String = null, artifactId: String = null, version: String = null,
  `type`: String = SL.JarStr, classifier: String = null,
  scope: String = SL.Compile, systemPath: String = null,
  exclusions: Seq[Exclusion] = Seq[Exclusion](), optional: Boolean = false) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text),
    emptyToDefault((elem \ SL.TypeStr).text, SL.JarStr),
    emptyToNull((elem \ SL.Classifier).text),
    emptyToDefault((elem \ SL.Scope).text, SL.Compile),
    emptyToNull((elem \ SL.SystemPath).text),
    (elem \ SL.Exclusions \ SL.ExclusionStr).map { case e: Elem =>
      new Exclusion(e) },
    emptyToDefault(
      (elem \ SL.OptionalStr).text.toLowerCase(Locale.ROOT), SL.FalseStr) ==
      SL.TrueStr.toString)

  lazy val xml =
    <dependency>
      { if (groupId != null) <groupId>{groupId}</groupId> }
      { if (artifactId != null) <artifactId>{artifactId}</artifactId> }
      { if (version != null) <version>{version}</version> }
      { if (`type` != null && `type` != SL.JarStr.toString)
        <type>{`type`}</type> }
      { if (classifier != null) <classifier>{classifier}</classifier> }
      { if (scope != null && scope != SL.Compile.toString)
        <scope>{scope}</scope> }
      { if (systemPath != null) <systemPath>{systemPath}</systemPath> }
      { if (!exclusions.isEmpty) <exclusions>
        { exclusions.map { _.xml } }
        </exclusions> }
      { if (optional) <optional>true</optional> }
    </dependency>

  def makeModelObject(): org.apache.maven.model.Dependency = {
    val dependency = new org.apache.maven.model.Dependency()
    if (groupId != null) dependency.setGroupId(groupId)
    if (artifactId != null) dependency.setArtifactId(artifactId)
    if (version != null) dependency.setVersion(version)
    dependency.setType(`type`)
    if (classifier != null) dependency.setClassifier(classifier)
    dependency.setScope(scope)
    if (systemPath != null) dependency.setSystemPath(systemPath)
    exclusions.foreach { ex => dependency.addExclusion(ex.makeModelObject()) }
    dependency.setOptional(optional)
    dependency
  }
}

case class Exclusion(groupId: String = null, artifactId: String = null) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text),
    emptyToNull((elem \ SL.ArtifactId).text))

  lazy val xml =
    <exclusion>
      { if (groupId != null) <groupId>{groupId}</groupId> }
      { if (artifactId != null) <artifactId>{artifactId}</artifactId> }
    </exclusion>

  def makeModelObject(): org.apache.maven.model.Exclusion = {
    val exclusion = new org.apache.maven.model.Exclusion()
    if (groupId != null) exclusion.setGroupId(groupId)
    if (artifactId != null) exclusion.setArtifactId(artifactId)
    exclusion
  }
}

