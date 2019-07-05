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

import com.typesafe.config.{ Config, ConfigObject }
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader

/**
  * This trait provides the ability to read Maven POM projects from Typesafe
  * Config objects.  Since the Typesafe ConfigFactory provides so many ways
  * to read Typesafe Config objects, only the Config -> Maven POM case class
  * is provided here.  Ficus is used to automate much of the conversion.
  */
trait HoconProjectReader {

  import SL._

  // code to convert some of the more complex POM case classes from Config
  // objects
  implicit val developerConfigReader: ValueReader[Developer] =
    ValueReader.relative { config =>
      Developer(
        if (config.hasPath(Id)) config.as[String](Id) else null,
        if (config.hasPath(Name)) config.as[String](Name) else null,
        if (config.hasPath(Email)) config.as[String](Email) else null,
        if (config.hasPath(UrlStr)) config.as[String](UrlStr) else null,
        if (config.hasPath(OrganizationStr)) config.as[String](OrganizationStr)
        else null,
        if (config.hasPath(OrganizationUrl)) config.as[String](OrganizationUrl)
        else null,
        if (config.hasPath(Roles)) config.as[Seq[String]](Roles)
        else Seq[String](),
        if (config.hasPath(TimezoneStr)) config.as[String](TimezoneStr)
        else null,
        if (config.hasPath(PropertiesStr))
          config.as[Map[String, String]](PropertiesStr)
        else Map[String, String]()
      )
    }

  implicit val contributorConfigReader: ValueReader[Contributor] =
    ValueReader.relative { config =>
      Contributor(
        if (config.hasPath(Name)) config.as[String](Name) else null,
        if (config.hasPath(Email)) config.as[String](Email) else null,
        if (config.hasPath(UrlStr)) config.as[String](UrlStr) else null,
        if (config.hasPath(OrganizationStr)) config.as[String](OrganizationStr)
        else null,
        if (config.hasPath(OrganizationUrl)) config.as[String](OrganizationUrl)
        else null,
        if (config.hasPath(Roles)) config.as[Seq[String]](Roles)
        else Seq[String](),
        if (config.hasPath(TimezoneStr)) config.as[String](TimezoneStr)
        else null,
        if (config.hasPath(PropertiesStr))
          config.as[Map[String, String]](PropertiesStr)
        else Map[String, String]()
      )
    }

  implicit val ciManagementConfigReader: ValueReader[CIManagement] =
    ValueReader.relative { config =>
      CIManagement(
        if (config.hasPath(SystemStr)) config.getString(SystemStr) else null,
        if (config.hasPath(UrlStr)) config.getString(UrlStr) else null,
        if (config.hasPath(Notifiers)) config.as[Seq[Notifier]](Notifiers)
        else Seq[Notifier]()
      )
    }

  implicit val notifierConfigReader: ValueReader[Notifier] =
    ValueReader.relative { config =>
      Notifier(
        if (config.hasPath(TypeStr)) config.getString(TypeStr) else Mail,
        if (config.hasPath(SendOnError))
          config.getBoolean(SendOnError)
        else true,
        if (config.hasPath(SendOnFailure))
          config.getBoolean(SendOnFailure)
        else true,
        if (config.hasPath(SendOnSuccess))
          config.getBoolean(SendOnSuccess)
        else true,
        if (config.hasPath(SendOnWarning))
          config.getBoolean(SendOnWarning)
        else true,
        if (config.hasPath(Configuration))
          config.as[Map[String, String]](Configuration)
        else Map[String, String]()
      )
    }

  implicit val profileConfigReader: ValueReader[Profile] =
    ValueReader.relative { config =>
      Profile(
        if (config.hasPath(Id)) config.as[String](Id) else DefaultStr,
        if (config.hasPath(ActivationStr))
          config.as[Activation](ActivationStr)
        else null,
        if (config.hasPath(BuildStr)) config.as[BuildBase](BuildStr) else null,
        if (config.hasPath(Modules)) config.as[Seq[String]](Modules)
        else Seq[String](),
        if (config.hasPath(DistributionManagementStr))
          config.as[DistributionManagement](DistributionManagementStr)
        else null,
        if (config.hasPath(PropertiesStr))
          config.as[Map[String, String]](PropertiesStr)
        else Map[String, String](),
        if (config.hasPath(DependencyManagementStr))
          config.as[Seq[Dependency]](DependencyManagementStr)
        else Seq[Dependency](),
        if (config.hasPath(Dependencies))
          config.as[Seq[Dependency]](Dependencies)
        else Seq[Dependency](),
        if (config.hasPath(Repositories))
          config.as[Seq[Repository]](Repositories)
        else Seq[Repository](),
        if (config.hasPath(PluginRepositories))
          config.as[Seq[Repository]](PluginRepositories)
        else Seq[Repository](),
        if (config.hasPath(ReportingStr)) config.as[Reporting](ReportingStr)
        else null
      )
    }

  implicit val distributionManagementConfigReader:
      ValueReader[DistributionManagement] =
    ValueReader.relative { config =>
      DistributionManagement(
        if (config.hasPath(RepositoryStr))
          config.as[DeploymentRepository](RepositoryStr)
        else null,
        if (config.hasPath(SnapshotRepository))
          config.as[DeploymentRepository](SnapshotRepository)
        else null,
        if (config.hasPath(SiteStr)) config.as[Site](SiteStr) else null,
        if (config.hasPath(DownloadUrl)) config.getString(DownloadUrl)
        else null,
        if (config.hasPath(RelocationStr)) config.as[Relocation](RelocationStr)
        else null,
        if (config.hasPath(Status)) config.getString(Status)
        else DistributionManagement.None
      )
    }

  implicit val archiverConfigReader: ValueReader[Archiver] =
    ValueReader.relative { config =>
      Archiver(
        if (config.hasPath(AddMavenDescriptor))
          config.as[Boolean](AddMavenDescriptor)
        else true,
        if (config.hasPath(Compress)) config.as[Boolean](Compress) else true,
        if (config.hasPath(Forced)) config.as[Boolean](Forced) else true,
        if (config.hasPath(Index)) config.as[Boolean](Index) else false,
        if (config.hasPath(ManifestStr))
          config.as[ManifestObj](ManifestStr)
        else null,
        if (config.hasPath(ManifestEntries))
          config.as[Map[String, String]](ManifestEntries)
        else Map[String, String](),
        if (config.hasPath(ManifestFile))
          config.as[String](ManifestFile)
        else null,
        if (config.hasPath(ManifestSections))
          config.as[Seq[ManifestSection]](ManifestSections)
        else Seq[ManifestSection](),
        if (config.hasPath(PomPropertiesFile))
          config.as[String](PomPropertiesFile)
        else null
      )
    }

  implicit val manifestSectionConfigReader: ValueReader[ManifestSection] =
    ValueReader.relative { config =>
      ManifestSection(
        if (config.hasPath(Name)) config.as[String](Name) else null,
        if (config.hasPath(ManifestEntries))
          config.as[Map[String, String]](ManifestEntries)
        else Map[String, String]()
      )
    }

  /**
    * Converts a Typesafe Config object to a Project case class which can
    * produce Json or XML.
    */
  def readPOM(conf: Config): Project = conf.as[Project](ProjectStr)

  // allows reading of the Archive object from a Typesafe Config via Ficus
  protected[unbound] def readArchiver(conf: Config): Archiver =
    conf.as[Archiver](Archive)

  // allows reading of the Fileset object from a Typesafe Config via Ficus
  protected[unbound] def readFileset(conf: Config): Fileset =
    conf.as[Fileset]("fileset")
}

/** Static accessor to the Config -> Project case class conversion code */
object HoconReader extends HoconProjectReader

