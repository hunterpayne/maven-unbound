
package org.apache.maven.unbound

import com.typesafe.config.{ Config, ConfigObject }

import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader

/**
  * Cannot generate a config value reader for type 
  * Option[org.apache.maven.unbound.CIManagement], because value readers 
  * cannot be auto-generated for types with type parameters
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
