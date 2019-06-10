
package org.apache.maven.unbound

import scala.xml.Elem

import com.typesafe.config.ConfigFactory

import org.json4s._

case object DistributionRepository extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class DistributionRepositorySerializer 
      extends CustomSerializer[DistributionRepository](format => (
    {
      case obj @ JObject(fields) =>
        new DistributionRepository(
          readBool(fields, UniqueVersion).getOrElse(true),
          readObject[RepositoryPolicy](obj, Releases),
          readObject[RepositoryPolicy](obj, Snapshots),
          readStr(fields, Id).get,
          readStr(fields, Name).get,
          readStr(fields, UrlStr).getOrElse(null),
          readStr(fields, Layout).getOrElse(DefaultStr)
        )
    },
    {
      case d: DistributionRepository =>
        JObject(
          JField(UniqueVersion, JBool(d.uniqueVersion)) ::
          JField(Releases, Extraction.decompose(d.releases)) ::
          JField(Snapshots, Extraction.decompose(d.snapshots)) ::
          JField(Id, JString(d.id)) ::
          JField(Name, JString(d.name)) ::
          JField(UrlStr, JString(d.url)) ::
          JField(Layout, JString(d.layout)) ::
          Nil)
    }
  ))
}

case class DistributionRepository(
  uniqueVersion: Boolean = true, 
  releases: RepositoryPolicy, snapshots: RepositoryPolicy,
  id: String, name: String, url: String, layout: String = SL.DefaultStr) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.UniqueVersion).text.toLowerCase, SL.TrueStr) == 
      SL.TrueStr.toString(),
    (elem \ SL.Releases).map { case e: Elem =>
      new RepositoryPolicy(e) }.headOption.getOrElse(null),
    (elem \ SL.Snapshots).map { case e: Elem => 
      new RepositoryPolicy(e) }.headOption.getOrElse(null),
    emptyToNull((elem \ SL.Id).text), 
    emptyToNull((elem \ SL.Name).text),
    emptyToNull((elem \ SL.UrlStr).text), 
    emptyToDefault((elem \ SL.Layout).text, SL.DefaultStr))

  lazy val xml = <distributionRepository>
                   <uniqueVersion>{uniqueVersion}</uniqueVersion>
                   { if (releases != null) releases.releasesXml }
                   { if (snapshots != null) snapshots.snapshotsXml }
                   <id>{id}</id>
                   <name>{name}</name>
                   <url>{url}</url>
                   <layout>{layout}</layout>
                 </distributionRepository>

  def makeModelObject(): org.apache.maven.model.DeploymentRepository = {
    val repo = new org.apache.maven.model.DeploymentRepository()
    repo.setUniqueVersion(uniqueVersion)
    if (releases != null) repo.setReleases(releases.makeModelObject())
    if (snapshots != null) repo.setSnapshots(snapshots.makeModelObject())
    repo.setId(id)
    repo.setName(name)
    repo.setUrl(url)
    repo.setLayout(layout)
    repo
  }
}

case object RepositoryPolicy extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val Warn = "warn"
  val Daily = "daily"

  class RepositoryPolicySerializer 
      extends CustomSerializer[RepositoryPolicy](format => (
    {
      case obj @ JObject(fields) =>
        new RepositoryPolicy(
          readBool(fields, Enabled).getOrElse(true),
          readStr(fields, UpdatePolicy).getOrElse(Daily),
          readStr(fields, ChecksumPolicy).getOrElse(Warn)
        )
    },
    {
      case r: RepositoryPolicy =>
        JObject(
          JField(Enabled, JBool(r.enabled)) ::
          JField(UpdatePolicy, Extraction.decompose(r.updatePolicy)) ::
          JField(ChecksumPolicy, Extraction.decompose(r.checksumPolicy)) ::
          Nil)
    }
  ))
}

case class RepositoryPolicy(
  enabled: Boolean = true, updatePolicy: String = RepositoryPolicy.Daily, 
  checksumPolicy: String = RepositoryPolicy.Warn) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.Enabled).text.toLowerCase, SL.TrueStr) == 
      SL.TrueStr.toString,
    emptyToDefault((elem \ SL.UpdatePolicy).text, RepositoryPolicy.Daily),
    emptyToDefault((elem \ SL.ChecksumPolicy).text, RepositoryPolicy.Warn))

  lazy val releasesXml = <releases>
                           <enabled>{enabled}</enabled>
                           <updatePolicy>{updatePolicy}</updatePolicy>
                           <checksumPolicy>{checksumPolicy}</checksumPolicy>
                         </releases>
  lazy val snapshotsXml = <snapshots>
                           <enabled>{enabled}</enabled>
                           <updatePolicy>{updatePolicy}</updatePolicy>
                           <checksumPolicy>{checksumPolicy}</checksumPolicy>
                          </snapshots>

  def makeModelObject(): org.apache.maven.model.RepositoryPolicy = {
    val policy = new org.apache.maven.model.RepositoryPolicy()
    policy.setEnabled(enabled)
    policy.setUpdatePolicy(updatePolicy)
    policy.setChecksumPolicy(checksumPolicy)
    policy
  }
}

case object Repository extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class RepositorySerializer 
      extends CustomSerializer[Repository](format => (
    {
      case obj @ JObject(fields) =>
        new Repository(
          readObject[RepositoryPolicy](obj, Releases),
          readObject[RepositoryPolicy](obj, Snapshots),
          readStr(fields, Id).get,
          readStr(fields, Name).get,
          readStr(fields, UrlStr).getOrElse(null),
          readStr(fields, Layout).getOrElse(DefaultStr)
        )
    },
    {
      case d: Repository =>
        JObject(
          JField(Releases, Extraction.decompose(d.releases)) ::
          JField(Snapshots, Extraction.decompose(d.snapshots)) ::
          JField(Id, JString(d.id)) ::
          JField(Name, JString(d.name)) ::
          JField(UrlStr, JString(d.url)) ::
          JField(Layout, JString(d.layout)) ::
          Nil)
    }
  ))
}

case class Repository(
  releases: RepositoryPolicy, snapshots: RepositoryPolicy,
  id: String, name: String, url: String, layout: String = SL.DefaultStr) {

  def this(elem: Elem) = this(
    (elem \ SL.Releases).map { case e: Elem => 
      new RepositoryPolicy(e) }.headOption.getOrElse(null),
    (elem \ SL.Snapshots).map { case e: Elem => 
      new RepositoryPolicy(e) }.headOption.getOrElse(null),
    emptyToNull((elem \ SL.Id).text), emptyToNull((elem \ SL.Name).text),
    emptyToNull((elem \ SL.UrlStr).text), 
    emptyToDefault((elem \ SL.Layout).text, SL.DefaultStr))

  lazy val xml = <repository>
                   { if (releases != null) releases.releasesXml }
                   { if (snapshots != null) snapshots.snapshotsXml }
                   <id>{id}</id>
                   <name>{name}</name>
                   <url>{url}</url>
                   <layout>{layout}</layout>
                 </repository>

  def makeModelObject(): org.apache.maven.model.Repository = {
    val repo = new org.apache.maven.model.Repository()
    if (releases != null) repo.setReleases(releases.makeModelObject())
    if (snapshots != null) repo.setSnapshots(snapshots.makeModelObject())
    repo.setId(id)
    repo.setName(name)
    repo.setUrl(url)
    repo.setLayout(layout)
    repo
  }
}

