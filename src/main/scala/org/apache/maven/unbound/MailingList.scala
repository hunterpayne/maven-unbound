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

import scala.xml.Elem

import com.typesafe.config.ConfigFactory
import org.json4s._

case object MailingList extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class MailingListSerializer extends CustomSerializer[MailingList](format => (
    {
      case JObject(fields) =>
        new MailingList(
          readStr(fields, Name).getOrElse(null),
          readStr(fields, Subscribe).getOrElse(null),
          readStr(fields, Unsubscribe).getOrElse(null),
          readStr(fields, Post).getOrElse(null),
          readStr(fields, Archive).getOrElse(null),
          readStringSequence(fields, OtherArchives)
        )
    },
    {
      case m: MailingList =>
        JObject(Seq[Option[JField]](
          writeStr(Name, m.name),
          writeStr(Subscribe, m.subscribe),
          writeStr(Unsubscribe, m.unsubscribe),
          writeStr(Post, m.post),
          writeStr(Archive, m.archive),
          writeStringSequence(OtherArchives, m.otherArchives)
        ).flatten.toList)
    }
  ))
}

case class MailingList(
  name: String, subscribe: String, unsubscribe: String, post: String,
  archive: String = null, otherArchives: Seq[String] = Seq[String]()) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text.trim),
    emptyToNull((elem \ SL.Subscribe).text.trim),
    emptyToNull((elem \ SL.Unsubscribe).text.trim),
    emptyToNull((elem \ SL.Post).text.trim),
    emptyToNull((elem \ SL.Archive).text.trim),
    (elem \ SL.OtherArchives \ SL.OtherArchive).map { case e: Elem =>
      e.text.trim }
  )

  lazy val xml = <mailingList>
                   <name>{name}</name>
                   <subscribe>{subscribe}</subscribe>
                   <unsubscribe>{unsubscribe}</unsubscribe>
                   { if (post != null) <post>{post}</post> }
                   { if (archive != null) <archive>{archive}</archive> }
                   { if (!otherArchives.isEmpty) <otherArchives>
                     { otherArchives.map { OtherArchive(_).xml } }
                   </otherArchives> }
                 </mailingList>

  def makeModelObject(): org.apache.maven.model.MailingList = {
    val ml = new org.apache.maven.model.MailingList()
    ml.setName(name)
    ml.setSubscribe(subscribe)
    ml.setUnsubscribe(unsubscribe)
    ml.setPost(post)
    ml.setArchive(archive)
    otherArchives.foreach { oa => ml.addOtherArchive(oa) }
    ml
  }
}

case class OtherArchive(archive: String) {
  lazy val xml = <otherArchive>{archive}</otherArchive>
}

