
package org.apache.maven.unbound

import scala.xml.{ Elem, Text, XML, Null, TopScope }

import com.typesafe.config.ConfigFactory

import org.json4s._

case class PropertyValue(key: String, value: String) {
  lazy val xml = new Elem(null, key, Null, TopScope, new Text(value))
}

case object Parent extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val DefRelativePath = "../pom.xml"

  class ParentSerializer extends CustomSerializer[Parent](format => (
    {
      case obj @ JObject(fields) =>
        new Parent(
          readStr(fields, GroupId).get,
          readStr(fields, ArtifactId).get,
          readStr(fields, Version).getOrElse(null),
          readStr(fields, RelativePath).getOrElse(DefRelativePath)
        )
    },
    {
      case p: Parent =>
        JObject(
          JField(GroupId, JString(p.groupId)) ::
          JField(ArtifactId, JString(p.artifactId)) ::
          JField(Version, JString(p.version)) ::
          JField(RelativePath, JString(p.relativePath)) ::
          Nil)
    }
  ))
}     

case class Parent(
  groupId: String, artifactId: String, version: String = null, 
  relativePath: String = Parent.DefRelativePath) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text), 
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text), 
    emptyToDefault((elem \ SL.RelativePath).text, Parent.DefRelativePath))

  lazy val xml = <parent>
                   <groupId>{groupId}</groupId>
                   <artifactId>{artifactId}</artifactId>
                   { if (version != null) <version>{version}</version> }
                   { if (relativePath != null && 
                         !Parent.DefRelativePath.toString.equals(relativePath))
                     <relativePath>{relativePath}</relativePath>
                   }
                 </parent>

  def makeModelObject(): org.apache.maven.model.Parent = {
    val parent = new org.apache.maven.model.Parent()
    parent.setGroupId(groupId)
    parent.setArtifactId(artifactId)
    if (version != null) parent.setVersion(version)
    parent.setRelativePath(relativePath)
    parent
  }
}

case class Organization(name: String, url: String) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text), emptyToNull((elem \ SL.UrlStr).text))

  lazy val xml = <organization>
                   <name>{name}</name>
                   <url>{url}</url>
                 </organization>

  def makeModelObject(): org.apache.maven.model.Organization = {
    val organization = new org.apache.maven.model.Organization()
    organization.setName(name)
    organization.setUrl(url)
    organization
  }
}

case object License extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val Repo = "repo"

  class LicenseSerializer extends CustomSerializer[License](format => (
    {
      case obj @ JObject(fields) =>
        new License(
          readStr(fields, Name).get,
          readStr(fields, UrlStr).get,
          readStr(fields, Distribution).getOrElse(Repo),
          readStr(fields, Comments).getOrElse("")
        )
    },
    {
      case l: License =>
        JObject(
          JField(Name, JString(l.name)) ::
          JField(UrlStr, JString(l.url)) ::
          JField(Distribution, JString(l.distribution)) ::
          JField(Comments, JString(l.comments)) ::
          Nil)
    }
  ))
}     

case class License(
  name: String, url: String, 
  distribution: String = License.Repo, comments: String = "") {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text), emptyToNull((elem \ SL.UrlStr).text),
    emptyToDefault((elem \ SL.Distribution).text, License.Repo), 
    (elem \ SL.Comments).text)

  lazy val xml = <license>
                   <name>{name}</name>
                   <url>{url}</url>
                   <distribution>{distribution}</distribution>
                   <comments>{comments}</comments>
                 </license>

  def makeModelObject(): org.apache.maven.model.License = {
    val license = new org.apache.maven.model.License()
    license.setName(name)
    license.setUrl(url)
    license.setDistribution(distribution)
    license.setComments(comments)
    license
  }
}

case object Scm extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val Head = "HEAD"

  class ScmSerializer extends CustomSerializer[Scm](format => (
    {
      case obj @ JObject(fields) =>
        new Scm(
          readBool(fields, ChildInheritConnection).getOrElse(true),
          readBool(fields, ChildInheritDeveloperConnection).getOrElse(true),
          readBool(fields, ChildInheritUrl).getOrElse(true),
          readStr(fields, Connection).get,
          readStr(fields, DeveloperConnection).get,
          readStr(fields, Tag).getOrElse(Head),
          readStr(fields, UrlStr).getOrElse("")
        )
    },
    {
      case s: Scm =>
        JObject(
          JField(ChildInheritConnection, JBool(s.childInheritConnection)) ::
          JField(
            ChildInheritDeveloperConnection, 
            JBool(s.childInheritDeveloperConnection)) ::
          JField(ChildInheritUrl, JBool(s.childInheritUrl)) ::
          JField(Connection, JString(s.connection)) ::
          JField(DeveloperConnection, JString(s.developerConnection)) ::
          JField(Tag, JString(s.tag)) ::
          JField(UrlStr, JString(s.url)) ::
          Nil)
    }
  ))
}     

case class Scm(
  childInheritConnection: Boolean = true,
  childInheritDeveloperConnection: Boolean = true,
  childInheritUrl: Boolean = true,
  connection: String, developerConnection: String, tag: String = Scm.Head, 
  url: String) {

  def this(elem: Elem) = this(
    elem.attribute(SL.ChildInheritConnectionFP).map { _.text }.
      getOrElse(SL.TrueStr.toString).toLowerCase == SL.TrueStr.toString,
    elem.attribute(SL.ChildInheritDeveloperConnectionFP).
      map { _.text }.getOrElse(SL.TrueStr.toString).toLowerCase == 
      SL.TrueStr.toString,
    elem.attribute(SL.ChildInheritScmUrlFP).map { _.text }.
      getOrElse(SL.TrueStr.toString).toLowerCase == SL.TrueStr.toString,
    emptyToNull((elem \ SL.Connection).text), 
    emptyToNull((elem \ SL.DeveloperConnection).text),
    emptyToDefault((elem \ SL.Tag).text, Scm.Head), 
    emptyToNull((elem \ SL.UrlStr).text))

  lazy val xml = 
    <scm child.scm.connection.inherit.append.path={if (childInheritConnection) SL.TrueStr else SL.FalseStr} 
         child.scm.developerConnection.inherit.append.path={if (childInheritConnection) SL.TrueStr else SL.FalseStr} 
         child.scm.url.inherit.append.path={if (childInheritConnection) SL.TrueStr else SL.FalseStr}>
      <connection>{connection}</connection>
      <developerConnection>{developerConnection}</developerConnection>
      <tag>{tag}</tag>
      <url>{url}</url>
    </scm>

  def makeModelObject(): org.apache.maven.model.Scm = {
    val scm = new org.apache.maven.model.Scm()
    scm.setConnection(connection)
    scm.setDeveloperConnection(developerConnection)
    scm.setTag(tag)
    scm.setUrl(url)
    scm
  }
}

case class IssueManagement(system: String, url: String) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.SystemStr).text), 
    emptyToNull((elem \ SL.UrlStr).text))

  lazy val xml = <issueManagement>
                   <system>{system}</system>
                   <url>{url}</url>
                 </issueManagement>

  def makeModelObject(): org.apache.maven.model.IssueManagement = {
    val im = new org.apache.maven.model.IssueManagement()
    im.setSystem(system)
    im.setUrl(url)
    im
  }
}

case object DistributionManagement extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  def None = "none"

  class DistributionManagementSerializer 
      extends CustomSerializer[DistributionManagement](format => (
    {
      case obj @ JObject(fields) =>
        new DistributionManagement(
          readObject[DistributionRepository](obj, RepositoryStr),
          readObject[DistributionRepository](obj, SnapshotRepository),
          readObject[Site](obj, SiteStr),
          readStr(fields, DownloadUrl).getOrElse(null),
          readObject[Relocation](obj, RelocationStr),
          readStr(fields, Status).getOrElse(None)
        )
    },
    {
      case d: DistributionManagement =>
        JObject(
          JField(RepositoryStr, Extraction.decompose(d.repository)) ::
          JField(
            SnapshotRepository, Extraction.decompose(d.snapshotRepository)) ::
          JField(SiteStr, Extraction.decompose(d.site)) ::
          JField(DownloadUrl, JString(d.downloadUrl)) ::
          JField(RelocationStr, Extraction.decompose(d.relocation)) ::
          JField(Status, JString(d.status)) ::
          Nil)
    }
  ))
}     

case class DistributionManagement(
  repository: DistributionRepository, 
  snapshotRepository: DistributionRepository = null,
  site: Site = null, downloadUrl: String = null, 
  relocation: Relocation = null, status: String = DistributionManagement.None) {

  def this(elem: Elem) = this(
    (elem \ SL.RepositoryStr).map { case e: Elem => 
      new DistributionRepository(e) }.headOption.getOrElse(null),
    (elem \ SL.SnapshotRepository).map { case e: Elem => 
      new DistributionRepository(e) }.headOption.getOrElse(null),
    (elem \ SL.SiteStr).map { case e: Elem => 
      new Site(e) }.headOption.getOrElse(null),
    emptyToNull((elem \ SL.DownloadUrl).text),
    (elem \ SL.RelocationStr).map { case e: Elem => 
      new Relocation(e) }.headOption.getOrElse(null),
    emptyToDefault((elem \ SL.Status).text, DistributionManagement.None))

  lazy val xml = <distributionManagement>
                   { repository.xml }
                   { if (snapshotRepository != null) snapshotRepository.xml }
                   { if (site != null) site.xml }
                 </distributionManagement>

  def makeModelObject(): org.apache.maven.model.DistributionManagement = {
    val mgmt = new org.apache.maven.model.DistributionManagement()
    mgmt.setRepository(repository.makeModelObject())
    if (snapshotRepository != null) 
      mgmt.setSnapshotRepository(snapshotRepository.makeModelObject())
    else mgmt.setSnapshotRepository(repository.makeModelObject())
    if (site != null) mgmt.setSite(site.makeModelObject())
    if (downloadUrl != null) mgmt.setDownloadUrl(downloadUrl)
    if (relocation != null) mgmt.setRelocation(relocation.makeModelObject())
    mgmt.setStatus(status)
    mgmt
  }
}

case object Site extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class SiteSerializer extends CustomSerializer[Site](format => (
    {
      case obj @ JObject(fields) =>
        new Site(
          readBool(fields, ChildInheritUrl).getOrElse(true),
          readStr(fields, Id).get,
          readStr(fields, Name).get,
          readStr(fields, UrlStr).getOrElse(null)
        )
    },
    {
      case s: Site =>
        JObject(
          JField(ChildInheritUrl, JBool(s.childInheritUrl)) ::
          JField(Id, JString(s.id)) ::
          JField(Name, JString(s.name)) ::
          JField(UrlStr, JString(s.url)) ::
          Nil)
    }
  ))
}

case class Site(
  childInheritUrl: Boolean = true, id: String, name: String, url: String) {

  def this(elem: Elem) = this(
    elem.attribute(SL.ChildInheritSiteUrlFP).map { _.text }.
      getOrElse(SL.TrueStr.toString).toLowerCase == SL.TrueStr.toString,
    emptyToNull((elem \ SL.Id).text), 
    emptyToNull((elem \ SL.Name).text), 
    emptyToNull((elem \ SL.UrlStr).text))

  lazy val xml = 
    <site child.site.url.inherit.append.path={if (childInheritUrl) SL.TrueStr else SL.FalseStr}>
      <id>{id}</id>
      <name>{name}</name>
      { if (url != null) <url>{url}</url> }
    </site>

  def makeModelObject(): org.apache.maven.model.Site = {
    val site = new org.apache.maven.model.Site()
    site.setId(id)
    site.setName(name)
    if (url != null) site.setUrl(url)
    site
  }
}

case object Relocation extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class RelocationSerializer extends CustomSerializer[Relocation](format => (
    {
      case JObject(fields) =>
        new Relocation(
          readStr(fields, GroupId).get,
          readStr(fields, ArtifactId).get,
          readStr(fields, Version).get,
          readStr(fields, Message).getOrElse(null))
    },
    {
      case d: Relocation =>
        JObject(
          JField(GroupId, JString(d.groupId)) ::
          JField(ArtifactId, JString(d.artifactId)) ::
          JField(Version, JString(d.version)) ::
          JField(Message, JString(d.message)) ::
          Nil)
    }
  ))
}

case class Relocation(
  groupId: String, artifactId: String, version: String, 
  message: String = null) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text), 
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text), 
    emptyToNull((elem \ SL.Message).text))

  lazy val xml = <relocation>
                   <groupId>{groupId}</groupId>
                   <artifactId>{artifactId}</artifactId>
                   <version>{version}</version>
                   { if (message != null) <message>{message}</message> }
                 </relocation>

  def makeModelObject(): org.apache.maven.model.Relocation = {
    val relocation = new org.apache.maven.model.Relocation()
    relocation.setGroupId(groupId)
    relocation.setArtifactId(artifactId)
    relocation.setVersion(version)
    if (message != null) relocation.setMessage(message)
    relocation
  }
}

case class Module(module: String) {
  lazy val xml = <module>{module}</module>
}
