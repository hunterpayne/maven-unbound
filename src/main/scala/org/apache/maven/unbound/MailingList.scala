
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
          readStr(fields, Name).get,
          readStr(fields, Subscribe).get,
          readStr(fields, Unsubscribe).get,
          readStr(fields, Post).getOrElse(null),
          readStr(fields, Archive).getOrElse(null),
          readStringSequence(fields, OtherArchives)
        )
    },
    {
      case m: MailingList =>
        JObject(
          JField(Name, JString(m.name)) ::
          JField(Subscribe, JString(m.subscribe)) ::
          JField(Unsubscribe, JString(m.unsubscribe)) ::
          JField(Post, JString(m.post)) ::
          JField(Archive, JString(m.archive)) ::
          JField(
            OtherArchives, 
            JArray(m.otherArchives.map { JString(_) }.toList)) ::
          Nil)
    }
  ))
}

case class MailingList(
  name: String, subscribe: String, unsubscribe: String, post: String, 
  archive: String = null, otherArchives: Seq[String] = Seq[String]()) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text), emptyToNull((elem \ SL.Subscribe).text),
    emptyToNull((elem \ SL.Unsubscribe).text),
    emptyToNull((elem \ SL.Post).text),
    emptyToNull((elem \ SL.Archive).text),
    (elem \ SL.OtherArchives).map(e => (e \ SL.OtherArchive).text))

  lazy val xml = <mailingList>
                   <name>{name}</name>
                   <subscribe>{subscribe}</subscribe>
                   <unsubscribe>{unsubscribe}</unsubscribe>
                   <post>{post}</post>
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

