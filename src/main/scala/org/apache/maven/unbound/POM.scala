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

import scala.xml.{ Elem, Null, Text, TopScope, UnprefixedAttribute, XML }

import com.typesafe.config.ConfigFactory
import org.json4s._

case class PropertyValue(key: String, value: String) {
  lazy val xml = new Elem(null, key, Null, TopScope, new Text(value))
}

case object Parent extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val DefRelativePath = "../pom.xml"

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ParentSerializer extends CustomSerializer[Parent](format => (
    {
      case obj @ JObject(fields) =>
        new Parent(
          readStr(fields, GroupId).getOrElse(null),
          readStr(fields, ArtifactId).getOrElse(null),
          readStr(fields, Version).getOrElse(null),
          readStr(fields, RelativePath).getOrElse(DefRelativePath)
        )
    },
    {
      case p: Parent =>
        JObject(Seq[Option[JField]](
          writeStr(GroupId, p.groupId),
          writeStr(ArtifactId, p.artifactId),
          writeStr(Version, p.version),
          writeStr(RelativePath, p.relativePath)
        ).flatten.toList)
    }
  ))
}

case class Parent(
  groupId: String = null, artifactId: String = null, version: String = null,
  relativePath: String = Parent.DefRelativePath) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text),
    emptyToDefault((elem \ SL.RelativePath).text, Parent.DefRelativePath))

  lazy val xml =
    <parent>
      { if (groupId != null) <groupId>{groupId}</groupId> }
      { if (artifactId != null) <artifactId>{artifactId}</artifactId> }
      { if (version != null) <version>{version}</version> }
      { if (relativePath != null &&
        !Parent.DefRelativePath.toString.equals(relativePath))
        <relativePath>{relativePath}</relativePath>
      }
    </parent>

  def makeModelObject(): org.apache.maven.model.Parent = {
    val parent = new org.apache.maven.model.Parent()
    if (groupId != null) parent.setGroupId(groupId)
    if (artifactId != null) parent.setArtifactId(artifactId)
    if (version != null) parent.setVersion(version)
    parent.setRelativePath(relativePath)
    parent
  }
}

case object Organization extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class OrganizationSerializer extends CustomSerializer[Organization](format => (
    {
      case JObject(fields) =>
        new Organization(
          readStr(fields, Name).getOrElse(null),
          readStr(fields, UrlStr).getOrElse(null)
        )
    },
    {
      case o: Organization =>
        JObject(Seq[Option[JField]](
          writeStr(Name, o.name),
          writeStr(UrlStr, o.url)
        ).flatten.toList)
    }
  ))
}

case class Organization(name: String = null, url: String = null) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text), emptyToNull((elem \ SL.UrlStr).text))

  lazy val xml = <organization>
                   { if (name != null) <name>{name}</name> }
                   { if (url != null) <url>{url}</url> }
                 </organization>

  def makeModelObject(): org.apache.maven.model.Organization = {
    val organization = new org.apache.maven.model.Organization()
    if (name != null) organization.setName(name)
    if (url != null) organization.setUrl(url)
    organization
  }
}

case object License extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val Repo = "repo"

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class LicenseSerializer extends CustomSerializer[License](format => (
    {
      case obj @ JObject(fields) =>
        new License(
          readStr(fields, Name).getOrElse(null),
          readStr(fields, UrlStr).getOrElse(null),
          readStr(fields, Distribution).getOrElse(Repo),
          readStr(fields, Comments).getOrElse("")
        )
    },
    {
      case l: License =>
        JObject(Seq[Option[JField]](
          writeStr(Name, l.name),
          writeStr(UrlStr, l.url),
          writeStr(Distribution, l.distribution),
          writeStr(Comments, l.comments)
        ).flatten.toList)
    }
  ))
}

case class License(
  name: String = null, url: String = null,
  distribution: String = License.Repo, comments: String = "") {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text.trim),
    emptyToNull((elem \ SL.UrlStr).text.trim),
    emptyToDefault((elem \ SL.Distribution).text.trim, License.Repo),
    (elem \ SL.Comments).text.trim)

  lazy val xml =
    <license>
      { if (name != null) <name>{name}</name> }
      { if (url != null) <url>{url}</url> }
      { if (distribution != null && distribution != License.Repo.toString)
        <distribution>{distribution}</distribution> }
      { if (comments != null && comments != "")
        <comments>{comments}</comments> }
    </license>

  def makeModelObject(): org.apache.maven.model.License = {
    val license = new org.apache.maven.model.License()
    if (name != null) license.setName(name)
    if (url != null) license.setUrl(url)
    if (distribution != null) license.setDistribution(distribution)
    if (comments != null) license.setComments(comments)
    license
  }
}

case object Scm extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val Head = "HEAD"

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ScmSerializer extends CustomSerializer[Scm](format => (
    {
      case obj @ JObject(fields) =>
        new Scm(
          readBool(fields, ChildInheritConnection).getOrElse(true),
          readBool(fields, ChildInheritDeveloperConnection).getOrElse(true),
          readBool(fields, ChildInheritUrl).getOrElse(true),
          readStr(fields, Connection).getOrElse(null),
          readStr(fields, DeveloperConnection).getOrElse(null),
          readStr(fields, Tag).getOrElse(Head),
          readStr(fields, UrlStr).getOrElse(null)
        )
    },
    {
      case s: Scm =>
        JObject(Seq[Option[JField]](
          writeBool(ChildInheritConnection, s.childInheritConnection, true),
          writeBool(
            ChildInheritDeveloperConnection,
            s.childInheritDeveloperConnection,
            true),
          writeBool(ChildInheritUrl, s.childInheritUrl, true),
          writeStr(Connection, s.connection),
          writeStr(DeveloperConnection, s.developerConnection),
          writeStr(Tag, s.tag, Head),
          writeStr(UrlStr, s.url)
        ).flatten.toList)
    }
  ))
}

case class Scm(
  childInheritConnection: Boolean = true,
  childInheritDeveloperConnection: Boolean = true,
  childInheritUrl: Boolean = true,
  connection: String = null, developerConnection: String = null,
  tag: String = Scm.Head, url: String = null) {

  def this(elem: Elem) = this(
    emptyToDefaultBool((elem \@ SL.ChildInheritConnectionFP).trim, true),
    emptyToDefaultBool(
      (elem \@ SL.ChildInheritDeveloperConnectionFP).trim, true),
    emptyToDefaultBool((elem \@ SL.ChildInheritScmUrlFP).trim, true),
    emptyToNull((elem \ SL.Connection).text.trim),
    emptyToNull((elem \ SL.DeveloperConnection).text.trim),
    emptyToDefault((elem \ SL.Tag).text.trim, Scm.Head),
    emptyToNull((elem \ SL.UrlStr).text.trim))

  lazy val xml = toXml
  private def toXml = {
    var elem =
      <scm>
        { if (connection != null) <connection>{connection}</connection> }
        { if (developerConnection != null)
          <developerConnection>{developerConnection}</developerConnection> }
        { if (tag != null && tag != Scm.Head.toString) <tag>{tag}</tag> }
        { if (url != null) <url>{url}</url> }
      </scm>
    elem = if (childInheritConnection) elem else new Elem(
      elem.prefix, elem.label,
      new UnprefixedAttribute(
        SL.ChildInheritConnectionFP, "false", elem.attributes),
      elem.scope, false, elem.child: _*)
    elem = if (childInheritDeveloperConnection) elem else new Elem(
      elem.prefix, elem.label,
      new UnprefixedAttribute(
        SL.ChildInheritDeveloperConnectionFP, "false", elem.attributes),
      elem.scope, false, elem.child: _*)
    if (childInheritUrl) elem
    else new Elem(
      elem.prefix, elem.label,
      new UnprefixedAttribute(
        SL.ChildInheritScmUrlFP, "false", elem.attributes),
      elem.scope, false, elem.child: _*)
  }

  def makeModelObject(): org.apache.maven.model.Scm = {
    val scm = new org.apache.maven.model.Scm()
    if (connection != null) scm.setConnection(connection)
    if (developerConnection != null)
      scm.setDeveloperConnection(developerConnection)
    scm.setTag(tag)
    if (url != null) scm.setUrl(url)
    scm
  }
}

case object IssueManagement extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class IssueManagementSerializer
      extends CustomSerializer[IssueManagement](format => (
        {
          case obj @ JObject(fields) =>
            new IssueManagement(
              readStr(fields, SystemStr).getOrElse(null),
              readStr(fields, UrlStr).getOrElse(null)
            )
        },
        {
          case i: IssueManagement =>
            JObject(Seq[Option[JField]](
              writeStr(SystemStr, i.system),
              writeStr(UrlStr, i.url)
            ).flatten.toList)
        }
      ))
}

case class IssueManagement(system: String = null, url: String = null) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.SystemStr).text),
    emptyToNull((elem \ SL.UrlStr).text))

  lazy val xml = <issueManagement>
                   { if (system != null) <system>{system}</system> }
                   { if (url != null) <url>{url}</url> }
                 </issueManagement>

  def makeModelObject(): org.apache.maven.model.IssueManagement = {
    val im = new org.apache.maven.model.IssueManagement()
    if (system != null) im.setSystem(system)
    if (url != null) im.setUrl(url)
    im
  }
}

case object DistributionManagement extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  protected[unbound] def None = "none"

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class DistributionManagementSerializer
      extends CustomSerializer[DistributionManagement](format => (
    {
      case obj @ JObject(fields) =>
        new DistributionManagement(
          readObject[DeploymentRepository](obj, RepositoryStr),
          readObject[DeploymentRepository](obj, SnapshotRepository),
          readObject[Site](obj, SiteStr),
          readStr(fields, DownloadUrl).getOrElse(null),
          readObject[Relocation](obj, RelocationStr),
          readStr(fields, Status).getOrElse(None)
        )
    },
    {
      case d: DistributionManagement =>
        JObject(Seq[Option[JField]](
          writeObject(RepositoryStr, d.repository),
          writeObject(SnapshotRepository, d.snapshotRepository),
          writeObject(SiteStr, d.site),
          writeStr(DownloadUrl, d.downloadUrl),
          writeObject(RelocationStr, d.relocation),
          writeStr(Status, d.status, None)
        ).flatten.toList)
    }
  ))
}

case class DistributionManagement(
  repository: DeploymentRepository = null,
  snapshotRepository: DeploymentRepository = null,
  site: Site = null, downloadUrl: String = null,
  relocation: Relocation = null, status: String = DistributionManagement.None) {

  def this(elem: Elem) = this(
    (elem \ SL.RepositoryStr).map { case e: Elem =>
      new DeploymentRepository(e) }.headOption.getOrElse(null),
    (elem \ SL.SnapshotRepository.toString).map { case e: Elem =>
      new DeploymentRepository(e) }.headOption.getOrElse(null),
    (elem \ SL.SiteStr.toString).map { case e: Elem =>
      new Site(e) }.headOption.getOrElse(null),
    emptyToNull((elem \ SL.DownloadUrl).text),
    (elem \ SL.RelocationStr.toString).map { case e: Elem =>
      new Relocation(e) }.headOption.getOrElse(null),
    emptyToDefault((elem \ SL.Status).text, DistributionManagement.None))

  lazy val xml = <distributionManagement>
                   { if (repository != null) repository.repositoryXml }
                   { if (snapshotRepository != null)
                     snapshotRepository.snapshotXml }
                   { if (site != null) site.xml }
                   { if (relocation != null) relocation.xml }
                   { if (status != null &&
                     status != DistributionManagement.None)
                     <status>{status}</status> }
                 </distributionManagement>

  def makeModelObject(): org.apache.maven.model.DistributionManagement = {
    val mgmt = new org.apache.maven.model.DistributionManagement()
    if (repository != null) mgmt.setRepository(repository.makeModelObject())
    if (snapshotRepository != null)
      mgmt.setSnapshotRepository(snapshotRepository.makeModelObject())
    else if (repository != null)
      mgmt.setSnapshotRepository(repository.makeModelObject())
    if (site != null) mgmt.setSite(site.makeModelObject())
    if (downloadUrl != null) mgmt.setDownloadUrl(downloadUrl)
    if (relocation != null) mgmt.setRelocation(relocation.makeModelObject())
    mgmt.setStatus(status)
    mgmt
  }
}

case object Site extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class SiteSerializer extends CustomSerializer[Site](format => (
    {
      case obj @ JObject(fields) =>
        new Site(
          readBool(fields, ChildInheritUrl).getOrElse(true),
          readStr(fields, Id).getOrElse(null),
          readStr(fields, Name).getOrElse(null),
          readStr(fields, UrlStr).getOrElse(null)
        )
    },
    {
      case s: Site =>
        JObject(Seq[Option[JField]](
          writeBool(ChildInheritUrl, s.childInheritUrl, true),
          writeStr(Id, s.id),
          writeStr(Name, s.name),
          writeStr(UrlStr, s.url)
        ).flatten.toList)
    }
  ))
}

case class Site(
  childInheritUrl: Boolean = true, id: String = null,
  name: String = null, url: String = null) {

  def this(elem: Elem) = this(
    emptyToDefaultBool((elem \@ SL.ChildInheritSiteUrlFP).trim, true),
    emptyToNull((elem \ SL.Id).text.trim),
    emptyToNull((elem \ SL.Name).text.trim),
    emptyToNull((elem \ SL.UrlStr).text.trim))

  lazy val xml = toXml
  private def toXml = {
    val elem =
      <site>
        { if (id != null) <id>{id}</id> }
        { if (name != null) <name>{name}</name> }
        { if (url != null) <url>{url}</url> }
      </site>
    if (childInheritUrl) elem
    else new Elem(
      elem.prefix, elem.label,
      new UnprefixedAttribute(
        SL.ChildInheritSiteUrlFP, "false", elem.attributes),
      elem.scope, false, elem.child: _*)
  }

  def makeModelObject(): org.apache.maven.model.Site = {
    val site = new org.apache.maven.model.Site()
    if (id != null) site.setId(id)
    if (name != null) site.setName(name)
    if (url != null) site.setUrl(url)
    site
  }
}

case object Relocation extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class RelocationSerializer extends CustomSerializer[Relocation](format => (
    {
      case JObject(fields) =>
        new Relocation(
          readStr(fields, GroupId).getOrElse(null),
          readStr(fields, ArtifactId).getOrElse(null),
          readStr(fields, Version).getOrElse(null),
          readStr(fields, Message).getOrElse(null))
    },
    {
      case d: Relocation =>
        JObject(Seq[Option[JField]](
          writeStr(GroupId, d.groupId),
          writeStr(ArtifactId, d.artifactId),
          writeStr(Version, d.version),
          writeStr(Message, d.message)
        ).flatten.toList)
    }
  ))
}

case class Relocation(
  groupId: String = null, artifactId: String = null, version: String = null,
  message: String = null) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text),
    emptyToNull((elem \ SL.Message).text))

  lazy val xml =
    <relocation>
      { if (groupId != null) <groupId>{groupId}</groupId> }
      { if (artifactId != null) <artifactId>{artifactId}</artifactId> }
      { if (version != null) <version>{version}</version> }
      { if (message != null) <message>{message}</message> }
    </relocation>

  def makeModelObject(): org.apache.maven.model.Relocation = {
    val relocation = new org.apache.maven.model.Relocation()
    if (groupId != null) relocation.setGroupId(groupId)
    if (artifactId != null) relocation.setArtifactId(artifactId)
    if (version != null) relocation.setVersion(version)
    if (message != null) relocation.setMessage(message)
    relocation
  }
}

case class Module(module: String) {
  lazy val xml = <module>{module}</module>
}
