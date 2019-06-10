
package org.apache.maven.hocon

import scala.xml.Elem

import com.typesafe.config.ConfigFactory

import org.json4s._

case object Dependency extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class DependencySerializer extends CustomSerializer[Dependency](format => (
    {
      case JObject(fields) =>
        new Dependency(
          readStr(fields, GroupId).get,
          readStr(fields, ArtifactId).get,
          readStr(fields, Version).getOrElse(null),
          readStr(fields, TypeStr).getOrElse(JarStr),
          readStr(fields, Classifier).getOrElse(null),
          readStr(fields, Scope).getOrElse(Compile),
          readStr(fields, SystemPath).getOrElse(null),
          fields.filter { _._1 == Exclusions }.headOption.map { excls =>
            excls._2.children.map { excl => excl match {
              case JObject(exFields) =>
                new Exclusion(
                  readStr(exFields, GroupId).get, 
                  readStr(exFields, ArtifactId).get)
              case _ => ???
            }}.toSeq}.getOrElse(Seq[Exclusion]()),
          readBool(fields, OptionalStr).getOrElse(false))
    },
    {
      case d: Dependency =>
        JObject(
          JField(GroupId, JString(d.groupId)) ::
          JField(ArtifactId, JString(d.artifactId)) ::
          JField(Version, JString(d.version)) ::
          JField(TypeStr, JString(d.`type`)) ::
          JField(Classifier, JString(d.artifactId)) ::
          JField(Scope, JString(d.artifactId)) ::
          JField(SystemPath, JString(d.artifactId)) ::
          JField(
            Exclusions, JArray(
              d.exclusions.map { ex =>
                JObject(
                  JField(GroupId, JString(ex.groupId)) ::
                  JField(ArtifactId, JString(ex.artifactId)) ::
                  Nil) }.toList)) ::
          JField(OptionalStr, JBool(d.optional)) :: 
          Nil)
    }
  ))
}

case class Dependency(
  groupId: String, artifactId: String, version: String,
  `type`: String = SL.JarStr, classifier: String = null, 
  scope: String = SL.Compile.toString, systemPath: String = null,
  exclusions: Seq[Exclusion] = Seq[Exclusion](), optional: Boolean = false) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text),
    emptyToDefault((elem \ SL.TypeStr).text, SL.JarStr),
    emptyToNull((elem \ SL.Classifier).text), 
    emptyToNull((elem \ SL.Scope).text),
    emptyToNull((elem \ SL.SystemPath).text),
    (elem \ SL.Exclusions \ SL.ExclusionStr).map { case e: Elem => 
      new Exclusion(e) },
    emptyToDefault((elem \ SL.OptionalStr).text.toLowerCase, SL.FalseStr) == 
      SL.TrueStr.toString)

  lazy val xml = 
    <dependency>
      <groupId>{groupId}</groupId>
      <artifactId>{artifactId}</artifactId>
      <version>{version}</version>
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
    dependency.setGroupId(groupId)
    dependency.setArtifactId(artifactId)
    dependency.setVersion(version)
    dependency.setType(`type`)
    if (classifier != null) dependency.setClassifier(classifier)
    dependency.setScope(scope)
    if (systemPath != null) dependency.setSystemPath(systemPath)
    exclusions.foreach { ex => dependency.addExclusion(ex.makeModelObject()) }
    dependency.setOptional(optional)
    dependency
  }
}

case class Exclusion(groupId: String, artifactId: String) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text), 
    emptyToNull((elem \ SL.ArtifactId).text))

  lazy val xml = <exclusion>
                   <groupId>{groupId}</groupId>
                   <artifactId>{artifactId}</artifactId>
                 </exclusion>

  def makeModelObject(): org.apache.maven.model.Exclusion = {
    val exclusion = new org.apache.maven.model.Exclusion()
    exclusion.setGroupId(groupId)
    exclusion.setArtifactId(artifactId)
    exclusion
  }
}

