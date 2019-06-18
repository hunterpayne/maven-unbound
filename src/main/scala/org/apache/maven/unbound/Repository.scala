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

case object DistributionRepository extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

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
        JObject(Seq[Option[JField]](
          writeBool(UniqueVersion, d.uniqueVersion, true),
          writeObject(Releases, d.releases),
          writeObject(Snapshots, d.snapshots),
          writeStr(Id, d.id),
          writeStr(Name, d.name),
          writeStr(UrlStr, d.url),
          writeStr(Layout, d.layout, DefaultStr)
        ).flatten.toList)
    }
  ))
}

case class DistributionRepository(
  uniqueVersion: Boolean = true,
  releases: RepositoryPolicy = null, snapshots: RepositoryPolicy = null,
  id: String, name: String, url: String, layout: String = SL.DefaultStr) {

  def this(elem: Elem) = this(
    emptyToDefault(
      (elem \ SL.UniqueVersion).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
      SL.TrueStr.toString(),
    (elem \ SL.Releases).map { case e: Elem =>
      new RepositoryPolicy(e) }.headOption.getOrElse(null),
    (elem \ SL.Snapshots).map { case e: Elem =>
      new RepositoryPolicy(e) }.headOption.getOrElse(null),
    emptyToNull((elem \ SL.Id).text),
    emptyToNull((elem \ SL.Name).text),
    emptyToNull((elem \ SL.UrlStr).text),
    emptyToDefault((elem \ SL.Layout).text, SL.DefaultStr))

  lazy val repositoryXml =
    <repository>
      { if (!uniqueVersion) <uniqueVersion>false</uniqueVersion> }
      { if (releases != null) releases.releasesXml }
      { if (snapshots != null) snapshots.snapshotsXml }
      { if (id != null) <id>{id}</id> }
      { if (name != null) <name>{name}</name> }
      { if (url != null) <url>{url}</url> }
      { if (layout != null && layout != SL.DefaultStr.toString)
        <layout>{layout}</layout> }
    </repository>

  lazy val snapshotXml =
    <snapshotRepository>
      { if (!uniqueVersion) <uniqueVersion>false</uniqueVersion> }
      { if (releases != null) releases.releasesXml }
      { if (snapshots != null) snapshots.snapshotsXml }
      { if (id != null) <id>{id}</id> }
      { if (name != null) <name>{name}</name> }
      { if (url != null) <url>{url}</url> }
      { if (layout != null && layout != SL.DefaultStr.toString)
        <layout>{layout}</layout> }
    </snapshotRepository>

  def makeModelObject(): org.apache.maven.model.DeploymentRepository = {
    val repo = new org.apache.maven.model.DeploymentRepository()
    repo.setUniqueVersion(uniqueVersion)
    if (releases != null) repo.setReleases(releases.makeModelObject())
    if (snapshots != null) repo.setSnapshots(snapshots.makeModelObject())
    // else if (releases != null) repo.setSnapshots(releases.makeModelObject())
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

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

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
        JObject(Seq[Option[JField]](
          writeBool(Enabled, r.enabled, true),
          writeStr(UpdatePolicy, r.updatePolicy, Daily),
          writeStr(ChecksumPolicy, r.checksumPolicy, Warn)
        ).flatten.toList)
    }
  ))
}

case class RepositoryPolicy(
  enabled: Boolean = true, updatePolicy: String = RepositoryPolicy.Daily,
  checksumPolicy: String = RepositoryPolicy.Warn) {

  def this(elem: Elem) = this(
    emptyToDefault(
      (elem \ SL.Enabled).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
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

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class RepositorySerializer
      extends CustomSerializer[Repository](format => (
    {
      case obj @ JObject(fields) =>
        new Repository(
          readObject[RepositoryPolicy](obj, Releases),
          readObject[RepositoryPolicy](obj, Snapshots),
          readStr(fields, Id).getOrElse(null),
          readStr(fields, Name).getOrElse(null),
          readStr(fields, UrlStr).getOrElse(null),
          readStr(fields, Layout).getOrElse(DefaultStr)
        )
    },
    {
      case d: Repository =>
        JObject(Seq[Option[JField]](
          writeObject(Releases, d.releases),
          writeObject(Snapshots, d.snapshots),
          writeStr(Id, d.id),
          writeStr(Name, d.name),
          writeStr(UrlStr, d.url),
          writeStr(Layout, d.layout, DefaultStr)
        ).flatten.toList)
    }
  ))
}

case class Repository(
  releases: RepositoryPolicy = null, snapshots: RepositoryPolicy = null,
  id: String, name: String, url: String, layout: String = SL.DefaultStr) {

  def this(elem: Elem) = this(
    (elem \ SL.Releases).map { case e: Elem =>
      new RepositoryPolicy(e) }.headOption.getOrElse(null),
    (elem \ SL.Snapshots).map { case e: Elem =>
      new RepositoryPolicy(e) }.headOption.getOrElse(null),
    emptyToNull((elem \ SL.Id).text),
    emptyToNull((elem \ SL.Name).text),
    emptyToNull((elem \ SL.UrlStr).text),
    emptyToDefault((elem \ SL.Layout).text, SL.DefaultStr))

  lazy val xml =
    <repository>
      { if (releases != null) releases.releasesXml }
      { if (snapshots != null) snapshots.snapshotsXml }
      { if (id != null) <id>{id}</id> }
      { if (name != null) <name>{name}</name> }
      { if (url != null) <url>{url}</url> }
      { if (layout != null && layout != SL.DefaultStr.toString)
        <layout>{layout}</layout> }
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

