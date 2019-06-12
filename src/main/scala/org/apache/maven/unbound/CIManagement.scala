
package org.apache.maven.unbound

import scala.xml.Elem

import com.typesafe.config.ConfigFactory

import org.json4s._

case class CIManagement(
  system: String, url: String, notifiers: Seq[Notifier] = Seq[Notifier]()) {

  def this(elem: Elem) = this(
    emptyToNull(
      (elem \ SL.SystemStr).text), emptyToNull((elem \ SL.UrlStr).text),
    (elem \ SL.Notifiers \ SL.NotifierStr).map { case n: Elem => 
      new Notifier(n) })

  lazy val xml = <ciManagement>
                   <system>{system}</system>
                   <url>{url}</url>
                   { if (!notifiers.isEmpty) <notifiers>
                     { notifiers.map { _.xml } }
                   </notifiers> }
                 </ciManagement>

  def makeModelObject(): org.apache.maven.model.CiManagement = {
    val cim = new org.apache.maven.model.CiManagement()
    cim.setSystem(system)
    cim.setUrl(url)
    notifiers.foreach { n => cim.getNotifiers().add(n.makeModelObject()) }
    cim
  }
}

case object Notifier extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class NotifierSerializer 
      extends CustomSerializer[Notifier](format => (
    {
      case obj @ JObject(fields) =>
        new Notifier(
          readStr(fields, TypeStr).getOrElse(Mail),
          readBool(fields, SendOnError).getOrElse(true),
          readBool(fields, SendOnFailure).getOrElse(true),
          readBool(fields, SendOnSuccess).getOrElse(false),
          readBool(fields, SendOnWarning).getOrElse(false),
          readProperties(obj, Configuration)
        )
    },
    {
      case n: Notifier =>
        JObject(Seq[Option[JField]](
          writeStr(TypeStr, n.`type`, Mail),
          writeBool(SendOnError, n.sendOnError, true),
          writeBool(SendOnFailure, n.sendOnFailure, true),
          writeBool(SendOnSuccess, n.sendOnSuccess, false),
          writeBool(SendOnWarning, n.sendOnWarning, false),
          writeProperties(Configuration, n.configuration)
        ).flatten.toList)
    }
  ))
}

case class Notifier(
  `type`: String = SL.Mail, 
  sendOnError: Boolean = true, sendOnFailure: Boolean = true,
  sendOnSuccess: Boolean = false, sendOnWarning: Boolean = false, 
  configuration: Map[String, String] = Map[String, String]()) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.TypeStr).text, SL.Mail),
    emptyToDefault(
      (elem \ SL.SendOnError).text.toLowerCase, SL.TrueStr) == 
      SL.TrueStr.toString,
    emptyToDefault(
      (elem \ SL.SendOnFailure).text.toLowerCase, SL.TrueStr) == 
      SL.TrueStr.toString,
    emptyToDefault(
      (elem \ SL.SendOnSuccess).text.toLowerCase, SL.FalseStr) == 
      SL.TrueStr.toString,
    emptyToDefault(
      (elem \ SL.SendOnWarning).text.toLowerCase, SL.FalseStr) == 
      SL.TrueStr.toString,
    (elem \ SL.Configuration).flatMap(_.map { e => (e.label, e.text) }).toMap)

  lazy val xml = <notifier>
                   <type>{`type`}</type>
                   <sendOnError>{sendOnError}</sendOnError>
                   <sendOnWarning>{sendOnWarning}</sendOnWarning>
                   <sendOnSuccess>{sendOnSuccess}</sendOnSuccess>
                   <sendOnWarning>{sendOnWarning}</sendOnWarning>
                   { if (!configuration.isEmpty) <configuration>
                     { configuration.foreach { case(k, v) =>
                       PropertyValue(k, v).xml } }
                   </configuration> }
                 </notifier>

  def makeModelObject(): org.apache.maven.model.Notifier = {
    val notifier = new org.apache.maven.model.Notifier()
    notifier.setType(`type`)
    notifier.setSendOnError(sendOnError)
    notifier.setSendOnFailure(sendOnFailure)
    notifier.setSendOnSuccess(sendOnSuccess)
    notifier.setSendOnWarning(sendOnWarning)
    configuration.map { case(k, v) => notifier.addConfiguration(k, v) }
    notifier
  }
}

