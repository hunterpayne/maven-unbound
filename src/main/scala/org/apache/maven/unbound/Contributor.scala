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
        JObject(Seq[Option[JField]](
          writeStr(Name, c.name),
          writeStr(Email, c.email),
          writeStr(UrlStr, c.url),
          writeStr(OrganizationStr, c.organization),
          writeStr(OrganizationUrl, c.organizationUrl),
          writeStringSequence(Roles, c.roles),
          writeStr(TimezoneStr, c.timezone),
          writeProperties(PropertiesStr, c.properties)
        ).flatten.toList)
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

  lazy val xml =
    <contributor>
      <name>{name}</name>
      <email>{email}</email>
      { if (url != null) <url>{url}</url> }
      { if (organization != null) <organization>{organization}</organization> }
      { if (organizationUrl != null)
        <organizationUrl>{organizationUrl}</organizationUrl> }
      { if (!roles.isEmpty) <roles> { roles.map { Role(_).xml } } </roles> }
      { if (timezone != null) <timezone>{timezone}</timezone> }
      { if (!properties.isEmpty) <properties>
        { properties.map { case(k, v) => PropertyValue(k, v).xml } }
        </properties> }
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
        JObject(Seq[Option[JField]](
          writeStr(Id, d.id),
          writeStr(Name, d.name),
          writeStr(Email, d.email),
          writeStr(UrlStr, d.url),
          writeStr(OrganizationStr, d.organization),
          writeStr(OrganizationUrl, d.organizationUrl),
          writeStringSequence(Roles, d.roles),
          writeStr(TimezoneStr, d.timezone),
          writeProperties(PropertiesStr, d.properties)
        ).flatten.toList)
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
                   { if (organization != null)
                     <organization>{organization}</organization> }
                   { if (organizationUrl != null)
                     <organizationUrl>{organizationUrl}</organizationUrl> }
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

