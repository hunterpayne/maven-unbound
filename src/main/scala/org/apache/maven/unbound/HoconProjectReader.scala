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

  implicit val developerConfigReader: ValueReader[Developer] =
    ValueReader.relative { config =>
      Developer(
        config.as[String]("id"),
        config.as[String]("name"),
        config.as[String]("email"),
        config.as[String]("url"),
        config.as[String]("organization"),
        config.as[String]("organizationUrl"),
        config.as[Seq[String]]("roles"),
        config.as[String]("timezone"),
        config.as[Map[String, String]]("properties"))
    }

  implicit val contributorConfigReader: ValueReader[Contributor] =
    ValueReader.relative { config =>
      Contributor(
        config.as[String]("name"),
        config.as[String]("email"),
        config.as[String]("url"),
        config.as[String]("organization"),
        config.as[String]("organizationUrl"),
        config.as[Seq[String]]("roles"),
        config.as[String]("timezone"),
        config.as[Map[String, String]]("properties"))
    }

  implicit val ciManagementConfigReader: ValueReader[CIManagement] =
    ValueReader.relative { config =>
      CIManagement(
        if (config.hasPath("system")) config.getString("system") else null,
        if (config.hasPath("url")) config.getString("url") else null,
        if (config.hasPath("notifiers")) config.as[Seq[Notifier]]("notifiers")
        else Seq[Notifier]())
    }

  implicit val profileConfigReader: ValueReader[Profile] =
    ValueReader.relative { config =>
      Profile(
        config.as[String]("id"),
        config.as[Activation]("activation"),
        config.as[BuildBase]("build"),
        config.as[Seq[String]]("modules"),
        config.as[DistributionManagement]("distributionManagement"),
        config.as[Map[String, String]]("properties"),
        config.as[Seq[Dependency]]("dependencyManagement"),
        config.as[Seq[Dependency]]("dependencies"),
        config.as[Seq[Repository]]("repositories"),
        config.as[Seq[Repository]]("pluginRepositories"),
        config.as[Reporting]("reporting"))
    }

  implicit val manifestSectionConfigReader: ValueReader[ManifestSection] =
    ValueReader.relative { config =>
      ManifestSection(
        config.as[String]("name"),
        config.as[Map[String, String]]("manifestEntries"))
    }

  def readPOM(conf: Config): Project = conf.as[Project]("project")

  def readArchiver(conf: Config): Archiver = conf.as[Archiver]("archive")
}

object HoconReader extends HoconProjectReader
