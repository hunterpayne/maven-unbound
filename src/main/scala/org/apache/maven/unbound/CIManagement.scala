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

case object CIManagement extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  protected def None = "none"

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class CIManagementSerializer
      extends CustomSerializer[CIManagement](format => (
    {
      case obj @ JObject(fields) =>
        new CIManagement(
          readStr(fields, SystemStr).getOrElse(null),
          readStr(fields, UrlStr).getOrElse(null),
          readObjectSequence[Notifier](fields, Notifiers)
        )
    },
    {
      case c: CIManagement =>
        JObject(Seq[Option[JField]](
          writeStr(SystemStr, c.system),
          writeStr(UrlStr, c.url),
          writeObjectSequence(Notifiers, c.notifiers)
        ).flatten.toList)
    }
  ))
}

case class CIManagement(
  system: String = null, url: String = null,
  notifiers: Seq[Notifier] = Seq[Notifier]()) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.SystemStr).text),
    emptyToNull((elem \ SL.UrlStr).text),
    (elem \ SL.Notifiers \ SL.NotifierStr).map { case n: Elem =>
      new Notifier(n) })

  lazy val xml = <ciManagement>
                   { if (system != null) <system>{system}</system> }
                   { if (url != null) <url>{url}</url> }
                   { if (!notifiers.isEmpty) <notifiers>
                     { notifiers.map { _.xml } }
                   </notifiers> }
                 </ciManagement>

  def makeModelObject(): org.apache.maven.model.CiManagement = {
    val cim = new org.apache.maven.model.CiManagement()
    if (system != null) cim.setSystem(system)
    if (url != null) cim.setUrl(url)
    notifiers.foreach { n => cim.getNotifiers().add(n.makeModelObject()) }
    cim
  }
}

case object Notifier extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class NotifierSerializer
      extends CustomSerializer[Notifier](format => (
    {
      case obj @ JObject(fields) =>
        new Notifier(
          readStr(fields, TypeStr).getOrElse(Mail),
          readBool(fields, SendOnError).getOrElse(true),
          readBool(fields, SendOnFailure).getOrElse(true),
          readBool(fields, SendOnSuccess).getOrElse(true),
          readBool(fields, SendOnWarning).getOrElse(true),
          readProperties(obj, Configuration)
        )
    },
    {
      case n: Notifier =>
        JObject(Seq[Option[JField]](
          writeStr(TypeStr, n.`type`, Mail),
          writeBool(SendOnError, n.sendOnError, true),
          writeBool(SendOnFailure, n.sendOnFailure, true),
          writeBool(SendOnSuccess, n.sendOnSuccess, true),
          writeBool(SendOnWarning, n.sendOnWarning, true),
          writeProperties(Configuration, n.configuration)
        ).flatten.toList)
    }
  ))
}

case class Notifier(
  `type`: String = SL.Mail,
  sendOnError: Boolean = true, sendOnFailure: Boolean = true,
  sendOnSuccess: Boolean = true, sendOnWarning: Boolean = true,
  configuration: Map[String, String] = Map[String, String]()) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.TypeStr).text, SL.Mail),
    emptyToDefault(
      (elem \ SL.SendOnError).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
      SL.TrueStr.toString,
    emptyToDefault(
      (elem \ SL.SendOnFailure).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
      SL.TrueStr.toString,
    emptyToDefault(
      (elem \ SL.SendOnSuccess).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
      SL.TrueStr.toString,
    emptyToDefault(
      (elem \ SL.SendOnWarning).text.toLowerCase(Locale.ROOT), SL.TrueStr) ==
      SL.TrueStr.toString,
    (elem \ SL.Configuration).headOption.map(
      _.child.filter(_.isInstanceOf[Elem]).map { e =>
        (e.label, e.text.trim) }.toMap).getOrElse(Map[String, String]()))

  lazy val xml = <notifier>
                   <type>{`type`}</type>
                   <sendOnError>{sendOnError}</sendOnError>
                   <sendOnFailure>{sendOnFailure}</sendOnFailure>
                   <sendOnSuccess>{sendOnSuccess}</sendOnSuccess>
                   <sendOnWarning>{sendOnWarning}</sendOnWarning>
                   { if (!configuration.isEmpty) <configuration>
                     { configuration.map { case(k, v) =>
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

