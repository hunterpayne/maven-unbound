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
    */
trait HoconProjectReader {

  import SL._

  implicit val developerConfigReader: ValueReader[Developer] =
    ValueReader.relative { config =>
      Developer(
        config.as[String](Id),
        config.as[String](Name),
        config.as[String](Email),
        config.as[String](UrlStr),
        config.as[String](OrganizationStr),
        config.as[String](OrganizationUrl),
        config.as[Seq[String]](Roles),
        config.as[String](TimezoneStr),
        config.as[Map[String, String]](PropertiesStr))
    }

  implicit val contributorConfigReader: ValueReader[Contributor] =
    ValueReader.relative { config =>
      Contributor(
        config.as[String](Name),
        config.as[String](Email),
        config.as[String](UrlStr),
        config.as[String](OrganizationStr),
        config.as[String](OrganizationUrl),
        config.as[Seq[String]](Roles),
        config.as[String](TimezoneStr),
        config.as[Map[String, String]](SL.PropertiesStr))
    }

  implicit val ciManagementConfigReader: ValueReader[CIManagement] =
    ValueReader.relative { config =>
      CIManagement(
        if (config.hasPath(SystemStr)) config.getString(SystemStr) else null,
        if (config.hasPath(UrlStr)) config.getString(UrlStr) else null,
        if (config.hasPath(Notifiers)) config.as[Seq[Notifier]](Notifiers)
        else Seq[Notifier]())
    }

  implicit val profileConfigReader: ValueReader[Profile] =
    ValueReader.relative { config =>
      Profile(
        config.as[String](Id),
        config.as[Activation](ActivationStr),
        config.as[BuildBase](BuildStr),
        config.as[Seq[String]](Modules),
        config.as[DistributionManagement](DistributionManagementStr),
        config.as[Map[String, String]](PropertiesStr),
        config.as[Seq[Dependency]](DependencyManagementStr),
        config.as[Seq[Dependency]](Dependencies),
        config.as[Seq[Repository]](Repositories),
        config.as[Seq[Repository]](PluginRepositories),
        config.as[Reporting](ReportingStr))
    }

  implicit val manifestSectionConfigReader: ValueReader[ManifestSection] =
    ValueReader.relative { config =>
      ManifestSection(
        config.as[String](Name),
        config.as[Map[String, String]](ManifestEntries))
    }

  def readPOM(conf: Config): Project = conf.as[Project](ProjectStr)

  def readArchiver(conf: Config): Archiver = conf.as[Archiver](Archive)
}

object HoconReader extends HoconProjectReader
