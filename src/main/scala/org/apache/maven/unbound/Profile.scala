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

case object Profile extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ProfileSerializer extends CustomSerializer[Profile](format => (
    {
      case obj @ JObject(fields) =>
        new Profile(
          readStr(fields, Id).getOrElse(DefaultStr),
          readObject[Activation](obj, ActivationStr),
          readObject[BuildBase](obj, BuildStr),
          readStringSequence(fields, Modules),
          readObject[DistributionManagement](obj, DistributionManagementStr),
          readProperties(obj),
          readObjectSequence[Dependency](fields, DependencyManagementStr),
          readObjectSequence[Dependency](fields, Dependencies),
          readObjectSequence[Repository](fields, Repositories),
          readObjectSequence[Repository](fields, PluginRepositories),
          readObject[Reporting](obj, ReportingStr)
        )
    },
    {
      case p: Profile =>
        JObject(Seq[Option[JField]](
          writeStr(Id, p.id, DefaultStr),
          writeObject(ActivationStr, p.activation),
          writeObject(BuildStr, p.build),
          writeStringSequence(Modules, p.modules),
          writeObject(DistributionManagementStr, p.distributionManagement),
          writeProperties(PropertiesStr, p.properties),
          writeObjectSequence(DependencyManagementStr, p.dependencyManagement),
          writeObjectSequence(Dependencies, p.dependencies),
          writeObjectSequence(Repositories, p.repositories),
          writeObjectSequence(PluginRepositories, p.pluginRepositories),
          writeObject(ReportingStr, p.reporting)
        ).flatten.toList)
    }
  ))
}

case class Profile(
  id: String = SL.DefaultStr, activation: Activation = null,
  build: BuildBase = null,
  modules: Seq[String] = Seq[String](),
  distributionManagement: DistributionManagement = null,
  properties: Map[String, String] = Map[String, String](),
  dependencyManagement: Seq[Dependency] = Seq[Dependency](),
  dependencies: Seq[Dependency] = Seq[Dependency](),
  repositories: Seq[Repository] = Seq[Repository](),
  pluginRepositories: Seq[Repository] = Seq[Repository](),
  reporting: Reporting = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.Id).text, SL.DefaultStr),
    (elem \ SL.ActivationStr).map { case e: Elem =>
      new Activation(e) }.headOption.getOrElse(null),
    (elem \ SL.BuildStr).map { case e: Elem =>
      new BuildBase(e) }.headOption.getOrElse(null),
    (elem \ SL.Modules \ SL.Module).map { _.text },
    (elem \ SL.DistributionManagementStr).map { case e: Elem =>
      new DistributionManagement(e) }.headOption.getOrElse(null),
    (elem \ SL.PropertiesStr).flatMap(_.map { e => (e.label, e.text) }).toMap,
    (elem \ SL.DependencyManagementStr \ SL.Dependencies \
      SL.DependencyStr).map { case e: Elem => new Dependency(e) },
    (elem \ SL.Dependencies \ SL.DependencyStr).map { case e: Elem =>
      new Dependency(e) },
    (elem \ SL.Repositories \ SL.RepositoryStr).map { case e: Elem =>
      new Repository(e) },
    (elem \ SL.PluginRepositories \
      SL.PluginRepositoryStr).map { case e: Elem => new Repository(e) },
    (elem \ SL.ReportingStr).map { case e: Elem =>
      new Reporting(e) }.headOption.getOrElse(null))

  lazy val xml = <profile>
                   <id>{id}</id>
                   { if (activation != null) activation.xml }
                   { if (build != null) build.xml }
                   { if (!modules.isEmpty) <modules>
                     { modules.map { Module(_).xml } }
                   </modules> }
                   { if (distributionManagement != null)
                     distributionManagement.xml }
                   { if (properties != null && !properties.isEmpty) <properties>
                     { properties.map { case(k, v) =>
                         PropertyValue(k, v).xml } }
                   </properties> }
                   { if (dependencyManagement != null &&
                     !dependencyManagement.isEmpty)
                     <dependencyManagement>
                       <dependencies>
                       { dependencyManagement.map { _.xml } }
                       </dependencies>
                     </dependencyManagement> }
                   { if (dependencies != null && !dependencies.isEmpty)
                     <dependencies>
                     { dependencies.map { _.xml } }
                     </dependencies> }
                   { if (repositories != null && !repositories.isEmpty)
                     <repositories>
                     { repositories.map { _.xml } }
                     </repositories> }
                   { if (pluginRepositories != null &&
                     !pluginRepositories.isEmpty)
                     <pluginRepositories>
                     { pluginRepositories.map { _.xml } }
                     </pluginRepositories> }
                   { if (reporting != null) reporting.xml }
                 </profile>

  def makeModelObject(): org.apache.maven.model.Profile = {
    val profile = new org.apache.maven.model.Profile()
    profile.setId(id)
    if (activation != null) profile.setActivation(activation.makeModelObject())
    if (build != null) profile.setBuild(build.makeModelObject())
    modules.foreach { module => profile.addModule(module) }
    if (distributionManagement != null)
      profile.setDistributionManagement(
        distributionManagement.makeModelObject())
    if (properties != null)
      properties.foreach { case(k, v) => profile.addProperty(k, v) }
    if (dependencyManagement != null) {
      val mgmt = new org.apache.maven.model.DependencyManagement()
      dependencyManagement.foreach { d =>
        mgmt.addDependency(d.makeModelObject()) }
      profile.setDependencyManagement(mgmt)
    }
    if (dependencies != null)
      dependencies.foreach { d => profile.addDependency(d.makeModelObject()) }
    if (repositories != null)
      repositories.foreach { r => profile.addRepository(r.makeModelObject()) }
    if (pluginRepositories != null)
      pluginRepositories.foreach { repo =>
        profile.addPluginRepository(repo.makeModelObject()) }
    if (reporting != null) profile.setReporting(reporting.makeModelObject())
    profile
  }
}

case object Activation extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ActivationSerializer extends CustomSerializer[Activation](format => (
    {
      case obj @ JObject(fields) =>
        new Activation(
          readBool(fields, ActiveByDefault).getOrElse(false),
          readStr(fields, JDK).getOrElse(null),
          readObject[ActivationOS](obj, OS),
          readObject[ActivationProperty](obj, PropertyStr),
          readObject[ActivationFile](obj, FileStr)
        )
    },
    {
      case a: Activation =>
        JObject(Seq[Option[JField]](
          writeBool(ActiveByDefault, a.activeByDefault, false),
          writeStr(JDK, a.jdk),
          writeObject(OS, a.os),
          writeObject(PropertyStr, a.property),
          writeObject(FileStr, a.file)
        ).flatten.toList)
    }
  ))
}

case class Activation(
  activeByDefault: Boolean = false, jdk: String = null,
  os: ActivationOS = null, property: ActivationProperty = null,
  file: ActivationFile = null) {

  def this(elem: Elem) = this(
    emptyToDefault(
      (elem \ SL.ActiveByDefault).text.toLowerCase(Locale.ROOT), SL.FalseStr) ==
      SL.TrueStr.toString,
    emptyToNull((elem \ SL.JDK).text),
    (elem \ SL.OS).map { case e: Elem =>
      new ActivationOS(e) }.headOption.getOrElse(null),
    (elem \ SL.PropertyStr).map { case e: Elem =>
      new ActivationProperty(e) }.headOption.getOrElse(null),
    (elem \ SL.FileStr).map { case e: Elem =>
      new ActivationFile(e) }.headOption.getOrElse(null))

  lazy val xml =
    <activation>
      { if (activeByDefault) <activeByDefault>true</activeByDefault> }
      { if (jdk != null) <jdk>{jdk}</jdk> }
      { if (os != null) os.xml }
      { if (property != null) property.xml }
      { if (file != null) file.xml }
    </activation>

  def makeModelObject(): org.apache.maven.model.Activation = {
    val activation = new org.apache.maven.model.Activation()
    activation.setActiveByDefault(activeByDefault)
    if (jdk != null) activation.setJdk(jdk)
    if (os != null) activation.setOs(os.makeModelObject())
    if (property != null) activation.setProperty(property.makeModelObject())
    if (file != null) activation.setFile(file.makeModelObject())
    activation
  }
}

case object ActivationOS extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ActivationOSSerializer
      extends CustomSerializer[ActivationOS](format => (
    {
      case obj @ JObject(fields) =>
        new ActivationOS(
          readStr(fields, Name).getOrElse(null),
          readStr(fields, Family).getOrElse(null),
          readStr(fields, Arch).getOrElse(null),
          readStr(fields, Version).getOrElse(null)
        )
    },
    {
      case a: ActivationOS =>
        JObject(Seq[Option[JField]](
          writeStr(Name, a.name),
          writeStr(Family, a.family),
          writeStr(Arch, a.arch),
          writeStr(Version, a.version)
        ).flatten.toList)
    }
  ))
}

case class ActivationOS(
  name: String = null,
  family: String = null, arch: String = null, version: String = null) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text), emptyToNull((elem \ SL.Family).text),
    emptyToNull((elem \ SL.Arch).text), emptyToNull((elem \ SL.Version).text))

  lazy val xml = <os>
                   { if (name != null) <name>{name}</name> }
                   { if (family != null) <family>{family}</family> }
                   { if (arch != null) <arch>{arch}</arch> }
                   { if (version != null) <version>{version}</version> }
                 </os>

  def makeModelObject(): org.apache.maven.model.ActivationOS = {
    val os = new org.apache.maven.model.ActivationOS()
    if (name != null) os.setName(name)
    if (family != null) os.setFamily(family)
    if (arch != null) os.setArch(arch)
    if (version != null) os.setVersion(version)
    os
  }
}

case object ActivationProperty extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ActivationPropertySerializer
      extends CustomSerializer[ActivationProperty](format => (
    {
      case obj @ JObject(fields) =>
        new ActivationProperty(
          readStr(fields, Name).getOrElse(null),
          readStr(fields, ValueStr).getOrElse(null)
        )
    },
    {
      case p: ActivationProperty =>
        JObject(Seq[Option[JField]](
          writeStr(Name, p.name),
          writeStr(ValueStr, p.value)
        ).flatten.toList)
    }
  ))
}

case class ActivationProperty(name: String = null, value: String = null) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Name).text), emptyToNull((elem \ SL.ValueStr).text))

  lazy val xml = <property>
                   { if (name != null) <name>{name}</name> }
                   { if (value != null) <value>{value}</value> }
                 </property>

  def makeModelObject(): org.apache.maven.model.ActivationProperty = {
    val prop = new org.apache.maven.model.ActivationProperty()
    if (name != null) prop.setName(name)
    if (value != null) prop.setValue(value)
    prop
  }
}

case object ActivationFile extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class ActivationFileSerializer
      extends CustomSerializer[ActivationFile](format => (
    {
      case obj @ JObject(fields) =>
        new ActivationFile(
          readStr(fields, Missing).getOrElse(null),
          readStr(fields, Exists).getOrElse(null)
        )
    },
    {
      case f: ActivationFile =>
        JObject(Seq[Option[JField]](
          writeStr(Missing, f.missing),
          writeStr(Exists, f.exists)
        ).flatten.toList)
    }
  ))
}

case class ActivationFile(missing: String = null, exists: String = null) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.Missing).text),
    emptyToNull((elem \ SL.Exists).text))

  lazy val xml = <file>
                   { if (missing != null) <missing>{missing}</missing> }
                   { if (exists != null) <exists>{exists}</exists> }
                 </file>


  def makeModelObject(): org.apache.maven.model.ActivationFile = {
    val file = new org.apache.maven.model.ActivationFile()
    if (missing != null) file.setMissing(missing)
    if (exists != null) file.setExists(exists)
    file
  }
}

case object BuildBase extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val defResourcesDir = "src/main/resources"
  val defTestResourcesDir = "src/test/resources"

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class BuildBaseSerializer extends CustomSerializer[BuildBase](format => (
    {
      case JObject(fields) =>
        new BuildBase(
          readStr(fields, DefaultGoal).getOrElse(null),
          readObjectSequence[Resource](
            fields, Resources,
            Seq[Resource](new Resource(defResourcesDir))),
          readObjectSequence[Resource](
            fields, TestResources,
            Seq[Resource](new Resource(defTestResourcesDir))),
          readStr(fields, DirectoryStr).getOrElse(Target),
          readStr(fields, FinalName).getOrElse(null),
          readStringSequence(fields, Filters),
          readObjectSequence[Plugin](fields, PluginManagement),
          readObjectSequence[Plugin](fields, Plugins)
        )
    },
    {
      case b: BuildBase =>
        JObject(Seq[Option[JField]](
          writeStr(DefaultGoal, b.defaultGoal),
          if (!Build.isDefaultResources(b.resources))
            writeObjectSequence(Resources, b.resources)
          else None,
          if (!Build.isDefaultResources(b.testResources))
            writeObjectSequence(TestResources, b.testResources)
          else None,
          writeStr(DirectoryStr, b.directory, Target),
          writeStr(FinalName, b.finalName),
          writeStringSequence(Filters, b.filters),
          writeObjectSequence(PluginManagement, b.pluginManagement),
          writeObjectSequence(Plugins, b.plugins)
        ).flatten.toList)
    }
  ))
}

case class BuildBase(
  defaultGoal: String = null,
  resources: Seq[Resource] =
    Seq[Resource](new Resource(BuildBase.defResourcesDir)),
  testResources: Seq[Resource] =
    Seq[Resource](new Resource(BuildBase.defTestResourcesDir)),
  directory: String = SL.Target, finalName: String = null,
  filters: Seq[String] = Seq[String](),
  pluginManagement: Seq[Plugin] = Seq[Plugin](),
  plugins: Seq[Plugin] = Seq[Plugin]()) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.DefaultGoal).text),
    ensureDefault(
      (elem \ SL.Resources \ SL.ResourceStr).map { case e: Elem =>
        new Resource(e) },
      new Resource(BuildBase.defResourcesDir)),
    ensureDefault(
      (elem \ SL.TestResources \ SL.TestResource).map { case e: Elem =>
        new Resource(e) },
      new Resource(BuildBase.defTestResourcesDir)),
    emptyToDefault((elem \ SL.DirectoryStr).text, SL.Target),
    emptyToNull((elem \ SL.FinalName).text),
    (elem \ SL.Filters \ SL.FilterStr).map { _.text },
    (elem \ SL.PluginManagement \ SL.Plugins \ SL.PluginStr).map {
      case e: Elem => new Plugin(e) },
    (elem \ SL.Plugins \ SL.PluginStr).map { case e: Elem => new Plugin(e) })

  lazy val xml =
    <build>
      { if (defaultGoal != null) <defaultGoal>{defaultGoal}</defaultGoal> }
      { if (!Build.isDefaultResources(resources))
        <resources> { resources.map { _.xml } } </resources> }
      { if (!Build.isDefaultTestResources(testResources))
        <testResources> { testResources.map { _.testXml } } </testResources> }
      { if (directory != null && directory != SL.Target.toString)
        <directory>{directory}</directory> }
      { if (finalName != null) <finalName>{finalName}</finalName> }
      { if (!filters.isEmpty)
        <filters> { filters.map { Filter(_).xml } } </filters> }
      { if (!pluginManagement.isEmpty) <pluginManagement>
        { pluginManagement.map { _.xml } }
        </pluginManagement> }
      { if (!plugins.isEmpty) <plugins> { plugins.map { _.xml } } </plugins> }
    </build>

  def makeModelObject(): org.apache.maven.model.BuildBase = {
    val build = new org.apache.maven.model.BuildBase()
    if (defaultGoal != null) build.setDefaultGoal(defaultGoal)
    resources.foreach { r => build.addResource(r.makeModelObject()) }
    testResources.foreach { r => build.addTestResource(r.makeModelObject()) }
    if (directory != null) build.setDirectory(directory)
    if (finalName != null) build.setFinalName(finalName)
    filters.foreach { f => build.addFilter(f) }
    val pm = new org.apache.maven.model.PluginManagement()
    pluginManagement.foreach { p => pm.addPlugin(p.makeModelObject()) }
    build.setPluginManagement(pm)
    plugins.foreach { p => build.addPlugin(p.makeModelObject()) }
    build
  }
}

