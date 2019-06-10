
package org.apache.maven.unbound

import scala.xml.Elem

import com.typesafe.config.ConfigFactory

import org.json4s._

case object Contributor extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ContributorSerializer 
      extends CustomSerializer[Contributor](format => (
    {
      case obj @ JObject(fields) =>
        new Contributor(
          readStr(fields, Name).get,
          readStr(fields, Email).getOrElse(null),
          readStr(fields, UrlStr).getOrElse(null),
          readStr(fields, OrganizationStr).getOrElse(null),
          readStr(fields, OrganizationUrl).getOrElse(null),
          readStringSequence(fields, Roles),
          readStr(fields, TimezoneStr).getOrElse(null),
          readProperties(obj)
        )
    },
    {
      case c: Contributor =>
        JObject(
          JField(Name, JString(c.name)) ::
          JField(Email, JString(c.email)) ::
          JField(UrlStr, JString(c.url)) ::
          JField(OrganizationStr, JString(c.organization)) ::
          JField(OrganizationUrl, JString(c.organizationUrl)) ::
          JField(Roles, JArray(c.roles.map { JString(_) }.toList)) ::
          JField(TimezoneStr, JString(c.timezone)) ::
          JField(
            PropertiesStr, 
            JObject(
              c.properties.map { case(k, v) => (k, JString(v)) }.toList)) ::
          Nil)
    }
  ))
}

case class Contributor(
  name: String, email: String = null, url: String = null,
  organization: String = null, organizationUrl: String = null, 
  roles: Seq[String] = Seq[String](), timezone: String = null,
  properties: Map[String, String] = Map[String, String]()) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text),
    emptyToNull((elem \ SL.Email).text),
    emptyToNull((elem \ SL.UrlStr).text),
    emptyToNull((elem \ SL.OrganizationStr).text),
    emptyToNull((elem \ SL.OrganizationUrl).text),
    (elem \ SL.Roles \ SL.RoleStr).map { _.text },
    emptyToNull((elem \ SL.TimezoneStr).text),
    (elem \ SL.PropertiesStr).flatMap(_.map { e => (e.label, e.text) }).toMap)

  lazy val xml = <contributor>
                   <name>{name}</name>
                   <email>{email}</email>
                   { if (url != null) <url>{url}</url> }
                   { if (organization != null) <organization>{organization}</organization> }
                   { if (organizationUrl != null) <organizationUrl>{organizationUrl}</organizationUrl> }
                   { if (!roles.isEmpty) <roles>
                     { roles.map { Role(_).xml } }
                   </roles> }
                   { if (timezone != null) <timezone>{timezone}</timezone> }
                   <properties>
                     { properties.map { case(k, v) => 
                       PropertyValue(k, v).xml } }
                   </properties>
                 </contributor>

  def makeModelObject(): org.apache.maven.model.Contributor = {
    val contributor = new org.apache.maven.model.Contributor()
    contributor.setName(name)
    contributor.setEmail(email)
    if (url != null) contributor.setUrl(url)
    if (organization != null) contributor.setOrganization(organization)
    if (organizationUrl != null) contributor.setOrganizationUrl(organizationUrl)
    roles.foreach { role => contributor.addRole(role) }
    if (timezone != null) contributor.setTimezone(timezone)
    properties.foreach { case(k, v) => contributor.addProperty(k, v) }
    contributor
  }
}

case object Developer extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class DeveloperSerializer 
      extends CustomSerializer[Developer](format => (
    {
      case obj @ JObject(fields) =>
        new Developer(
          readStr(fields, Id).get,
          readStr(fields, Name).getOrElse(null),
          readStr(fields, Email).getOrElse(null),
          readStr(fields, UrlStr).getOrElse(null),
          readStr(fields, OrganizationStr).getOrElse(null),
          readStr(fields, OrganizationUrl).getOrElse(null),
          readStringSequence(fields, Roles),
          readStr(fields, TimezoneStr).getOrElse(null),
          readProperties(obj)
        )
    },
    {
      case d: Developer =>
        JObject(
          JField(Id, JString(d.id)) ::
          JField(Name, JString(d.name)) ::
          JField(Email, JString(d.email)) ::
          JField(UrlStr, JString(d.url)) ::
          JField(OrganizationStr, JString(d.organization)) ::
          JField(OrganizationUrl, JString(d.organizationUrl)) ::
          JField(Roles, JArray(d.roles.map { JString(_) }.toList)) ::
          JField(TimezoneStr, JString(d.timezone)) ::
          JField(
            PropertiesStr, 
            JObject(
              d.properties.map { case(k, v) => (k, JString(v)) }.toList)) ::
          Nil)
    }
  ))
}

case class Developer(
  id: String, name: String = null, email: String = null, url: String = null, 
  organization: String = null, organizationUrl: String = null, 
  roles: Seq[String] = Seq[String](), timezone: String = null,
  properties: Map[String, String] = Map[String, String]()) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Id).text),
    emptyToNull((elem \ SL.Name).text),
    emptyToNull((elem \ SL.Email).text), 
    emptyToNull((elem \ SL.UrlStr).text),
    emptyToNull((elem \ SL.OrganizationStr).text), 
    emptyToNull((elem \ SL.OrganizationUrl).text),
    (elem \ SL.Roles \ SL.RoleStr).map { _.text },
    emptyToNull((elem \ SL.TimezoneStr).text),
    (elem \ SL.PropertiesStr).flatMap(_.map { e => (e.label, e.text) }).toMap)

  lazy val xml = <developer>
                   <id>{id}</id>
                   <name>{name}</name>
                   <email>{email}</email>
                   { if (url != null) <url>{url}</url> }
                   { if (organization != null) <organization>{organization}</organization> }
                   { if (organizationUrl != null) <organizationUrl>{organizationUrl}</organizationUrl> }
                   <roles>
                     { roles.map { Role(_).xml } }
                   </roles>
                   { if (timezone != null) <timezone>{timezone}</timezone> }
                   <properties>
                     { properties.map { case(k, v) => 
                       PropertyValue(k, v).xml } }
                   </properties>
                 </developer>

  def makeModelObject(): org.apache.maven.model.Developer = {
    val developer = new org.apache.maven.model.Developer()
    developer.setId(id)
    developer.setName(name)
    developer.setEmail(email)
    if (url != null) developer.setUrl(url)
    if (organization != null) developer.setOrganization(organization)
    if (organizationUrl != null) developer.setOrganizationUrl(organizationUrl)
    roles.foreach { role => developer.addRole(role) }
    if (timezone != null) developer.setTimezone(timezone)
    properties.foreach { case(k, v) => developer.addProperty(k, v) }
    developer
  }
}

case class Role(role: String) {
  lazy val xml = <role>{role}</role>
}

