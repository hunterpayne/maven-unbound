
package org.apache.maven.hocon

import scala.reflect.Manifest

import com.typesafe.config.{ Config, ConfigObject, ConfigFactory }

import org.json4s._
import org.json4s.native.JsonMethods
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{ read, write }

trait CommonJsonReader extends Labels {

  implicit val formats: Formats
  implicit val boolReader = DefaultReaders.BooleanReader
  implicit val strReader = DefaultReaders.StringReader

  protected def readBool(fields: List[JField], key: String): Option[Boolean] = 
    fields.filter { _._1 == key }.headOption.map { case(_, v) => v.as[Boolean] }

  protected def readStr(fields: List[JField], key: String): Option[String] = 
    fields.filter { _._1 == key }.headOption.map { case(_, v) => v.as[String] }

  protected def readObject[T](obj: JObject, key: String, defVa: T = null)(
    implicit m: Manifest[T]): T =
    (obj \ key) match {
      case obj2: JObject => Extraction.extract[T](obj2)
      case _ => defVa
    }

  protected def readProperties(
    obj: JObject, key: String = "properties"): Map[String, String] =
    (obj \ key) match {
      case JObject(fields) => 
        fields.map { case((key, v)) => (key, v.as[String]) }.toMap
      case _ => Map[String, String]()
    }

  protected def readObjectSequence[T](
    fields: List[JField], key: String, 
    defVal: Seq[T] = Seq[T]())(implicit m: Manifest[T]): Seq[T] =
    fields.filter { _._1 == key }.headOption.map { e =>
      e._2.children.map { Extraction.extract[T](_) }}.getOrElse(defVal)

  protected def readStringSequence(
    fields: List[JField], key: String): Seq[String] =
    fields.filter { _._1 == key }.headOption.map { goals =>
      goals._2.children.map { goal => Extraction.extract[String](goal) }
    }.getOrElse(Seq[String]())
}

trait JsonProjectAPI extends JsonMethods with CommonJsonReader {

  implicit val formats =
    Serialization.formats(NoTypeHints) + 
    new Project.ProjectSerializer +
    new Build.BuildSerializer + 
    new Plugin.PluginSerializer +
    new Dependency.DependencySerializer + 
    new Execution.ExecutionSerializer +
    new Reporting.ReportingSerializer + 
    new ReportPlugin.ReportPluginSerializer + 
    new Extension.ExtensionSerializer +
    new Resource.ResourceSerializer +
    new Notifier.NotifierSerializer +
    new Contributor.ContributorSerializer +
    new Developer.DeveloperSerializer +
    new MailingList.MailingListSerializer +
    new Parent.ParentSerializer +
    new License.LicenseSerializer +
    new Scm.ScmSerializer +
    new Site.SiteSerializer +
    new DistributionManagement.DistributionManagementSerializer +
    new Relocation.RelocationSerializer +
    new DistributionRepository.DistributionRepositorySerializer +
    new Repository.RepositorySerializer +
    new RepositoryPolicy.RepositoryPolicySerializer +
    new ReportSet.ReportSetSerializer +
    new BuildBase.BuildBaseSerializer +
    new Activation.ActivationSerializer +
    new ActivationOS.ActivationOSSerializer +
    new Profile.ProfileSerializer
}

object JsonReader extends JsonProjectAPI {

  def readPOM(jsonSrc: String): Project = read[Project](jsonSrc)
}

object JsonWriter extends JsonProjectAPI {

  def writePOM(project: Project): String = write[Project](project)

}
