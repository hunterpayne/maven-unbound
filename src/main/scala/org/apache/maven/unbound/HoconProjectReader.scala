
package org.apache.maven.unbound

import com.typesafe.config.{ Config, ConfigObject }

import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader

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
        config.as[String]("system"),
        config.as[String]("url"),
        config.as[Seq[Notifier]]("notifiers"))
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

  def readPOM(conf: Config): Project = conf.as[Project]("project")
}

object HoconReader extends HoconProjectReader
