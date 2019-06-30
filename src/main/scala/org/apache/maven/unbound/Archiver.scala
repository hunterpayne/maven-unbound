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

import scala.xml.Elem

import com.typesafe.config.ConfigFactory
import org.json4s._

protected[unbound] object Archiver extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ArchiverSerializer extends CustomSerializer[Archiver](format => (
    {
      case obj @ JObject(fields) =>
        new Archiver(
          readBool(fields, AddMavenDescriptor).getOrElse(true),
          readBool(fields, Compress).getOrElse(true),
          readBool(fields, Forced).getOrElse(true),
          readBool(fields, Index).getOrElse(false),
          readObject[ManifestObj](obj, ManifestStr),
          readProperties(obj, ManifestEntries),
          readStr(fields, ManifestFile).getOrElse(null),
          readObjectSequence[ManifestSection](fields, ManifestSections),
          readStr(fields, PomPropertiesFile).getOrElse(null)
        )
    },
    {
      case a: Archiver =>
        JObject(Seq[Option[JField]](
          writeBool(AddMavenDescriptor, a.addMavenDescriptor, true),
          writeBool(Compress, a.compress, true),
          writeBool(Forced, a.forced, true),
          writeBool(Index, a.index, false),
          writeObject(ManifestStr, a.manifest),
          writeProperties(ManifestEntries, a.manifestEntries),
          writeStr(ManifestFile, a.manifestFile),
          writeObjectSequence(ManifestSections, a.manifestSections),
          writeStr(PomPropertiesFile, a.pomPropertiesFile)
        ).flatten.toList)
    }
  ))
}

/**
  * This class handles the special case of the Maven Archiver
  * which is a piece of shared infrastructure provided by Maven to Plugins
  * to allow compressing of build products into a single file.
  * @see See [[http://maven.apache.org/shared/maven-archiver/index.html]] for
  * more details
  * @author Hunter Payne
  */
case class Archiver(
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
    emptyToDefaultBool((elem \ SL.AddMavenDescriptor).text, true),
    emptyToDefaultBool((elem \ SL.Compress).text, true),
    emptyToDefaultBool((elem \ SL.Forced).text, true),
    emptyToDefaultBool((elem \ SL.Index).text, false),
    (elem \ SL.ManifestStr).map { case e: Elem =>
      new ManifestObj(e) }.headOption.getOrElse(null),
    (elem \ SL.ManifestEntries).headOption.map(
      _.child.filter(_.isInstanceOf[Elem]).map { e =>
        (e.label, e.text.trim) }.toMap).getOrElse(Map[String, String]()),
    emptyToNull((elem \ SL.ManifestFile).text.trim),
    (elem \ SL.ManifestSections \ SL.ManifestSectionStr).map { case e: Elem =>
      new ManifestSection(e) },
    emptyToNull((elem \ SL.PomPropertiesFile).text)
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

protected[unbound] object ManifestObj extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ManifestSerializer extends CustomSerializer[ManifestObj](format => (
    {
      case obj @ JObject(fields) =>
        new ManifestObj(
          readBool(fields, AddClasspath).getOrElse(false),
          readBool(fields, AddDefaultEntries).getOrElse(true),
          readBool(fields, AddDefaultImplementationEntries).getOrElse(false),
          readBool(fields, AddDefaultSpecificationEntries).getOrElse(false),
          readBool(fields, AddBuildEnvironmentEntries).getOrElse(false),
          readBool(fields, AddExtensions).getOrElse(false),
          readStr(fields, ClasspathLayoutType).getOrElse(Simple),
          readStr(fields, ClasspathPrefix).getOrElse(""),
          readStr(fields, CustomClasspathLayout).getOrElse(null),
          readStr(fields, MainClass).getOrElse(null),
          readStr(fields, PackageName).getOrElse(null),
          readBool(fields, UseUniqueVersions).getOrElse(true)
        )
    },
    {
      case m: ManifestObj =>
        JObject(Seq[Option[JField]](
          writeBool(AddClasspath, m.addClasspath, false),
          writeBool(AddDefaultEntries, m.addDefaultEntries, true),
          writeBool(
            AddDefaultImplementationEntries,
            m.addDefaultImplementationEntries, false),
          writeBool(
            AddDefaultSpecificationEntries, m.addDefaultSpecificationEntries,
            false),
          writeBool(
            AddBuildEnvironmentEntries, m.addBuildEnvironmentEntries, false),
          writeBool(AddExtensions, m.addExtensions, false),
          writeStr(ClasspathLayoutType, m.classpathLayoutType, Simple),
          writeStr(ClasspathPrefix, m.classpathPrefix, ""),
          writeStr(CustomClasspathLayout, m.customClasspathLayout),
          writeStr(MainClass, m.mainClass),
          writeStr(PackageName, m.packageName),
          writeBool(UseUniqueVersions, m.useUniqueVersions, true)
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
  classpathLayoutType: String = SL.Simple,
  classpathPrefix: String = "",
  customClasspathLayout: String = null,
  mainClass: String = null,
  packageName: String = null,
  useUniqueVersions: Boolean = true) {

  def this(elem: Elem) = this(
    emptyToDefaultBool((elem \ SL.AddClasspath).text, false),
    emptyToDefaultBool((elem \ SL.AddDefaultEntries).text, true),
    emptyToDefaultBool((elem \ SL.AddDefaultImplementationEntries).text, false),
    emptyToDefaultBool((elem \ SL.AddDefaultSpecificationEntries).text, false),
    emptyToDefaultBool((elem \ SL.AddBuildEnvironmentEntries).text, false),
    emptyToDefaultBool((elem \ SL.AddExtensions).text, false),
    emptyToDefault((elem \ SL.ClasspathLayoutType).text, SL.Simple),
    emptyToDefault((elem \ SL.ClasspathPrefix).text, ""),
    emptyToNull((elem \ SL.CustomClasspathLayout).text),
    emptyToNull((elem \ SL.MainClass).text),
    emptyToNull((elem \ SL.PackageName).text),
    emptyToDefaultBool((elem \ SL.UseUniqueVersions).text, true)
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
      { if (classpathLayoutType != null &&
        classpathLayoutType != SL.Simple.toString)
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

protected[unbound] object ManifestSection extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ManifestSectionSerializer
      extends CustomSerializer[ManifestSection](format => (
        {
          case obj @ JObject(fields) =>
            new ManifestSection(
              readStr(fields, Name).getOrElse(null),
              readProperties(obj, ManifestEntries)
            )
        },
        {
          case s: ManifestSection =>
            JObject(Seq[Option[JField]](
              writeStr(Name, s.name),
              writeProperties(ManifestEntries, s.manifestEntries)
            ).flatten.toList)
        }
      ))
}

case class ManifestSection(
  name: String = null,
  manifestEntries: Map[String, String] = Map[String, String]()) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text),
    (elem \ SL.ManifestEntries).headOption.map(
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
