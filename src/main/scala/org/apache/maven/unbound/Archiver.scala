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

protected[unbound] case object Archiver extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ArchiverSerializer extends CustomSerializer[Archiver](format => (
    {
      case obj @ JObject(fields) =>
        new Archiver(
          readBool(fields, "addMavenDescriptor").getOrElse(true),
          readBool(fields, "compress").getOrElse(true),
          readBool(fields, "forced").getOrElse(true),
          readBool(fields, "index").getOrElse(false),
          readObject[ManifestObj](obj, "manifest"),
          readProperties(obj, "manifestEntries"),
          readStr(fields, "manifestFile").getOrElse(null),
          readObjectSequence[ManifestSection](fields, "manifestSections"),
          readStr(fields, "pomPropertiesFile").getOrElse(null)
        )
    },
    {
      case a: Archiver =>
        JObject(Seq[Option[JField]](
          writeBool("addMavenDescriptor", a.addMavenDescriptor, true),
          writeBool("compress", a.compress, true),
          writeBool("forced", a.forced, true),
          writeBool("index", a.index, false),
          writeObject("manifest", a.manifest),
          writeProperties("manifestEntries", a.manifestEntries),
          writeStr("manifestFile", a.manifestFile),
          writeObjectSequence("manifestSections", a.manifestSections),
          writeStr("pomPropertiesFile", a.pomPropertiesFile)
        ).flatten.toList)
    }
  ))
}

protected[unbound] case class Archiver(
  addMavenDescriptor: Boolean = true,
  compress: Boolean = true,
  forced: Boolean = true,
  index: Boolean = false,
  manifest: ManifestObj = null,
  manifestEntries: Map[String, String] = Map[String, String](),
  manifestFile: String = null,
  manifestSections: Seq[ManifestSection] = Seq[ManifestSection](),
  pomPropertiesFile: String = null
) {

  def this(elem: Elem) = this(
    emptyToDefaultBool((elem \ "addMavenDescriptor").text, true),
    emptyToDefaultBool((elem \ "compress").text, true),
    emptyToDefaultBool((elem \ "forced").text, true),
    emptyToDefaultBool((elem \ "index").text, false),
    (elem \ "manifest").map { case e: Elem =>
      new ManifestObj(e) }.headOption.getOrElse(null),
    (elem \ "manifestEntries").headOption.map(
      _.child.filter(_.isInstanceOf[Elem]).map { e =>
        (e.label, e.text.trim) }.toMap).getOrElse(Map[String, String]()),
    emptyToNull((elem \ "manifestFile").text.trim),
    (elem \ "manifestSections" \ "manifestSection").map { case e: Elem =>
      new ManifestSection(e) },
    emptyToNull((elem \ "pomPropertiesFile").text)
  )

  lazy val xml =
    <archive>
      { if (!addMavenDescriptor) <addMavenDescriptor>false</addMavenDescriptor>}
      { if (!compress) <compress>false</compress> }
      { if (!forced) <forced>false</forced> }
      { if (index) <index>true</index> }
      { if (manifest != null) manifest.xml }
      { if (!manifestEntries.isEmpty) <manifestEntries>
        { manifestEntries.map { case(k, v) => PropertyValue(k, v).xml } }
        </manifestEntries> }
      { if (manifestFile != null) <manifestFile>{manifestFile}</manifestFile> }
      { if (!manifestSections.isEmpty) <manifestSections>
        { manifestSections.map { _.xml } } </manifestSections> }
      { if (pomPropertiesFile != null)
        <pomPropertiesFile>{pomPropertiesFile}</pomPropertiesFile> }
    </archive>
}

case object ManifestObj extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ManifestSerializer extends CustomSerializer[ManifestObj](format => (
    {
      case obj @ JObject(fields) =>
        new ManifestObj(
          readBool(fields, "addClasspath").getOrElse(false),
          readBool(fields, "addDefaultEntries").getOrElse(true),
          readBool(fields, "addDefaultImplementationEntries").getOrElse(false),
          readBool(fields, "addDefaultSpecificationEntries").getOrElse(false),
          readBool(fields, "addBuildEnvironmentEntries").getOrElse(false),
          readBool(fields, "addExtensions").getOrElse(false),
          readStr(fields, "classpathLayoutType").getOrElse("simple"),
          readStr(fields, "classpathPrefix").getOrElse(""),
          readStr(fields, "customClasspathLayout").getOrElse(null),
          readStr(fields, "mainClass").getOrElse(null),
          readStr(fields, "packageName").getOrElse(null),
          readBool(fields, "useUniqueVersions").getOrElse(true)
        )
    },
    {
      case m: ManifestObj =>
        JObject(Seq[Option[JField]](
          writeBool("addClasspath", m.addClasspath, false),
          writeBool("addDefaultEntries", m.addDefaultEntries, true),
          writeBool(
            "addDefaultImplementationEntries",
            m.addDefaultImplementationEntries, false),
          writeBool(
            "addDefaultSpecificationEntries", m.addDefaultSpecificationEntries,
            false),
          writeBool(
            "addBuildEnvironmentEntries", m.addBuildEnvironmentEntries, false),
          writeBool("addExtensions", m.addExtensions, false),
          writeStr("classpathLayoutType", m.classpathLayoutType, "simple"),
          writeStr("classpathPrefix", m.classpathPrefix, ""),
          writeStr("customClasspathLayout", m.customClasspathLayout),
          writeStr("mainClass", m.mainClass),
          writeStr("packageName", m.packageName),
          writeBool("useUniqueVersions", m.useUniqueVersions, true)
        ).flatten.toList)
    }
  ))
}

case class ManifestObj(
  addClasspath: Boolean = false,
  addDefaultEntries: Boolean = true,
  addDefaultImplementationEntries: Boolean = false,
  addDefaultSpecificationEntries: Boolean = false,
  addBuildEnvironmentEntries: Boolean = false,
  addExtensions: Boolean = false,
  classpathLayoutType: String = "simple",
  classpathPrefix: String = "",
  customClasspathLayout: String = null,
  mainClass: String = null,
  packageName: String = null,
  useUniqueVersions: Boolean = true) {

  def this(elem: Elem) = this(
    emptyToDefaultBool((elem \ "addClasspath").text, false),
    emptyToDefaultBool((elem \ "addDefaultEntries").text, true),
    emptyToDefaultBool((elem \ "addDefaultImplementationEntries").text, false),
    emptyToDefaultBool((elem \ "addDefaultSpecificationEntries").text, false),
    emptyToDefaultBool((elem \ "addBuildEnvironmentEntries").text, false),
    emptyToDefaultBool((elem \ "addExtensions").text, false),
    emptyToDefault((elem \ "classpathLayoutType").text, "simple"),
    emptyToDefault((elem \ "classpathPrefix").text, ""),
    emptyToNull((elem \ "customClasspathLayout").text),
    emptyToNull((elem \ "mainClass").text),
    emptyToNull((elem \ "packageName").text),
    emptyToDefaultBool((elem \ "useUniqueVersions").text, true)
  )

  lazy val xml =
    <manifest>
      { if (addClasspath) <addClasspath>true</addClasspath> }
      { if (!addDefaultEntries) <addDefaultEntries>false</addDefaultEntries> }
      { if (addDefaultImplementationEntries)
        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>}
      { if (addDefaultSpecificationEntries)
        <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries> }
      { if (addBuildEnvironmentEntries)
        <addBuildEnvironmentEntries>true</addBuildEnvironmentEntries> }
      { if (addExtensions) <addExtensions>true</addExtensions> }
      { if (classpathLayoutType != null && classpathLayoutType != "simple")
        <classpathLayoutType>{classpathLayoutType}</classpathLayoutType> }
      { if (classpathPrefix != null && classpathPrefix != "")
        <classpathPrefix>{classpathPrefix}</classpathPrefix> }
      { if (customClasspathLayout != null)
        <customClasspathLayout>{customClasspathLayout}</customClasspathLayout> }
      { if (mainClass != null) <mainClass>{mainClass}</mainClass> }
      { if (packageName != null) <packageName>{packageName}</packageName> }
      { if (!useUniqueVersions) <useUniqueVersions>false</useUniqueVersions> }
    </manifest>
}

case object ManifestSection extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ManifestSectionSerializer
      extends CustomSerializer[ManifestSection](format => (
        {
          case obj @ JObject(fields) =>
            new ManifestSection(
              readStr(fields, Name).getOrElse(null),
              readProperties(obj, "manifestEntries")
            )
        },
        {
          case s: ManifestSection =>
            JObject(Seq[Option[JField]](
              writeStr(Name, s.name),
              writeProperties("manifestEntries", s.manifestEntries)
            ).flatten.toList)
        }
      ))
}

case class ManifestSection(
  name: String = null,
  manifestEntries: Map[String, String] = Map[String, String]()) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text),
    (elem \ "manifestEntries").headOption.map(
      _.child.filter(_.isInstanceOf[Elem]).map { e =>
        (e.label, e.text.trim) }.toMap).getOrElse(Map[String, String]())
  )

  lazy val xml =
    <manifestSection>
      { if (name != null) <name>{name}</name> }
      { if (!manifestEntries.isEmpty) <manifestEntries>
        { manifestEntries.map { case(k, v) => PropertyValue(k, v).xml } }
        </manifestEntries> }
    </manifestSection>
}
