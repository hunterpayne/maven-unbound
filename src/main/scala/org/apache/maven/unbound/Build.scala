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

import java.io.StringReader
import java.util.Locale

import scala.xml.Elem

import com.typesafe.config.{ Config, ConfigFactory, ConfigObject }
import org.json4s._

case object Build extends CommonJsonReader {

  implicit val formats = JsonReader.formats
  val defSourceDir = "src/main/java"
  val defScriptDir = "src/main/scripts"
  val defTestDir = "src/test/java"
  val defOutputDir = "target/classes"
  val defTestOutputDir = "target/test-classes"
  val defResourcesDir = "src/main/resources"
  val defTestResourcesDir = "src/test/resources"

  class BuildSerializer extends CustomSerializer[Build](format => (
    {
      case JObject(fields) =>
        new Build(
          readStr(fields, SourceDirectory).getOrElse(defSourceDir),
          readStr(
            fields, ScriptSourceDirectory).getOrElse(defScriptDir),
          readStr(fields, TestSourceDirectory).getOrElse(defTestDir),
          readStr(fields, OutputDirectory).getOrElse(defOutputDir),
          readStr(
            fields, TestOutputDirectory).getOrElse(defTestOutputDir),
          readObjectSequence[Extension](fields, Extensions),
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
      case b: Build =>
        JObject(Seq[Option[JField]](
          writeStr(SourceDirectory, b.sourceDirectory, defSourceDir),
          writeStr(
            ScriptSourceDirectory, b.scriptSourceDirectory, defScriptDir),
          writeStr(TestSourceDirectory, b.testSourceDirectory, defTestDir),
          writeStr(OutputDirectory, b.outputDirectory, defSourceDir),
          writeStr(
            TestOutputDirectory, b.testOutputDirectory, defTestOutputDir),
          writeObjectSequence(Extensions, b.extensions),
          writeStr(DefaultGoal, b.defaultGoal),
          if (!isDefaultResources(b.resources))
            writeObjectSequence(Resources, b.resources)
          else None,
          if (!isDefaultResources(b.testResources))
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

  protected[unbound] def isDefaultResources(res: Seq[Resource]): Boolean = {
    res.size == 1 && res(0).targetPath == Build.defResourcesDir &&
    res(0).directory == SL.Dot.toString && res(0).includes.size == 1 &&
    res(0).excludes.size == 0 && res(0).includes(0) == Build.defResourcesDir
  }

  protected[unbound] def isDefaultTestResources(res: Seq[Resource]): Boolean = {
    res.size == 1 && res(0).targetPath == Build.defTestResourcesDir &&
    res(0).directory == SL.Dot.toString && res(0).includes.size == 1 &&
    res(0).excludes.size == 0 && res(0).includes(0) == Build.defTestResourcesDir
  }
}

case class Build(
  sourceDirectory: String = Build.defSourceDir,
  scriptSourceDirectory: String = Build.defScriptDir,
  testSourceDirectory: String = Build.defTestDir,
  outputDirectory: String = Build.defOutputDir,
  testOutputDirectory: String = Build.defTestOutputDir,
  extensions: Seq[Extension] = Seq[Extension](),
  defaultGoal: String = null,
  resources: Seq[Resource] = Seq[Resource](new Resource(Build.defResourcesDir)),
  testResources: Seq[Resource] =
    Seq[Resource](new Resource(Build.defTestResourcesDir)),
  directory: String = SL.Target,
  finalName: String = null, /* ${artifactId}-${version} */
  filters: Seq[String] = Seq[String](),
  pluginManagement: Seq[Plugin] = Seq[Plugin](),
  plugins: Seq[Plugin] = Seq[Plugin]()) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.SourceDirectory).text, Build.defSourceDir),
    emptyToDefault((elem \ SL.ScriptSourceDirectory).text, Build.defScriptDir),
    emptyToDefault((elem \ SL.TestSourceDirectory).text, Build.defTestDir),
    emptyToDefault((elem \ SL.OutputDirectory).text, Build.defOutputDir),
    emptyToDefault(
      (elem \ SL.TestOutputDirectory).text, Build.defTestOutputDir),
    (elem \ SL.Extensions \ SL.ExtensionStr).map { case e: Elem =>
      new Extension(e)},
    emptyToNull((elem \ SL.DefaultGoal).text),
    ensureDefault(
      (elem \ SL.Resources \ SL.ResourceStr).map { case e: Elem =>
        new Resource(e) },
      new Resource(Build.defResourcesDir)),
    ensureDefault(
      (elem \ SL.TestResources \ SL.TestResource).map { case e: Elem =>
        new Resource(e) },
      new Resource(Build.defTestResourcesDir)),
    emptyToDefault((elem \ SL.DirectoryStr).text, SL.Target),
    emptyToNull((elem \ SL.FinalName).text),
    (elem \ SL.Filters \ SL.FilterStr).map { _.text },
    (elem \ SL.PluginManagement \ SL.Plugins \ SL.PluginStr).map {
      case e: Elem => new Plugin(e) },
    (elem \ SL.Plugins \ SL.PluginStr).map { case e: Elem => new Plugin(e) })

  lazy val xml =
    <build>
      { if (sourceDirectory != null && sourceDirectory != Build.defSourceDir)
        <sourceDirectory>{sourceDirectory}</sourceDirectory> }
      { if (scriptSourceDirectory != null &&
        scriptSourceDirectory != Build.defScriptDir)
        <scriptSourceDirectory>{scriptSourceDirectory}</scriptSourceDirectory> }
      { if (testSourceDirectory != null &&
        testSourceDirectory != Build.defTestDir)
        <testSourceDirectory>{testSourceDirectory}</testSourceDirectory> }
      { if (outputDirectory != null && outputDirectory != Build.defOutputDir)
        <outputDirectory>{outputDirectory}</outputDirectory> }
      { if (testOutputDirectory != null &&
        testOutputDirectory != Build.defTestOutputDir)
        <testOutputDirectory>{testOutputDirectory}</testOutputDirectory> }
      { if (!extensions.isEmpty) <extensions>
        { extensions.map { _.xml } }
        </extensions> }
      { if (defaultGoal != null) <defaultGoal>{defaultGoal}</defaultGoal> }
      { if (!resources.isEmpty && !Build.isDefaultResources(resources))
        <resources> { resources.map { _.xml } } </resources> }
      { if (!testResources.isEmpty &&
        !Build.isDefaultTestResources(testResources))
        <testResources> { testResources.map { _.testXml } } </testResources> }
      { if (directory != null && directory != SL.Target.toString)
        <directory>{directory}</directory> }
      { if (finalName != null) <finalName>{finalName}</finalName> }
      { if (!filters.isEmpty) <filters>
        { filters.map { Filter(_).xml } }
        </filters> }
      { if (!pluginManagement.isEmpty) <pluginManagement><plugins>
        { pluginManagement.map { _.xml } }
        </plugins></pluginManagement> }
      { if (!plugins.isEmpty) <plugins>
        { plugins.map { _.xml } }
        </plugins> }
    </build>

  def makeModelObject(): org.apache.maven.model.Build = {
    val build = new org.apache.maven.model.Build()
    build.setSourceDirectory(sourceDirectory)
    build.setScriptSourceDirectory(scriptSourceDirectory)
    build.setTestSourceDirectory(testSourceDirectory)
    build.setOutputDirectory(outputDirectory)
    build.setTestOutputDirectory(testOutputDirectory)
    extensions.foreach { ex => build.addExtension(ex.makeModelObject()) }
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

case class Filter(filter: String) {
  lazy val xml = <filter>{filter}</filter>
}

case class Extension(groupId: String, artifactId: String, version: String) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.GroupId).text),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text))

  lazy val xml = <extension>
                   <groupId>{groupId}</groupId>
                   <artifactId>{artifactId}</artifactId>
                   { if (version != null) <version>{version}</version> }
                 </extension>

  def makeModelObject(): org.apache.maven.model.Extension = {
    val extension = new org.apache.maven.model.Extension()
    extension.setGroupId(groupId)
    extension.setArtifactId(artifactId)
    if (version != null) extension.setVersion(version)
    extension
  }
}

case object Resource extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ResourceSerializer extends CustomSerializer[Resource](format => (
    {
      case JObject(fields) =>
        new Resource(
          readStr(fields, TargetPath).get,
          readBool(fields, Filtering).getOrElse(false),
          readStr(fields, DirectoryStr).getOrElse(SL.Dot),
          readStringSequence(fields, Includes),
          readStringSequence(fields, Excludes)
        )
    },
    {
      case r: Resource =>
        JObject(Seq[Option[JField]](
          writeStr(TargetPath, r.targetPath),
          writeBool(Filtering, r.filtering, false),
          writeStr(DirectoryStr, r.directory, Dot),
          writeStringSequence(Includes, r.includes),
          writeStringSequence(Excludes, r.excludes)
        ).flatten.toList)
    }
  ))
}

case class Resource(
  targetPath: String, filtering: Boolean = false, directory: String = SL.Dot,
  includes: Seq[String], excludes: Seq[String] = Seq[String]()) {

  def this(name: String) = this(name, false, SL.Dot, Seq[String](name))

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.TargetPath).text),
    emptyToDefault(
      (elem \ SL.Filtering).text.toLowerCase(Locale.ROOT), SL.FalseStr) ==
      SL.TrueStr.toString,
    emptyToDefault((elem \ SL.DirectoryStr).text, SL.Dot),
    (elem \ SL.Includes \ SL.IncludeStr).map { _.text },
    (elem \ SL.Excludes \ SL.ExcludeStr).map { _.text })

  lazy val xml = <resource>
                   <targetPath>{targetPath}</targetPath>
                   <filtering>{filtering}</filtering>
                   <directory>{directory}</directory>
                   { if (!includes.isEmpty) <includes>
                     { includes.map { Include(_).xml } }
                   </includes> }
                   { if (!excludes.isEmpty) <excludes>
                     { excludes.map { Exclude(_).xml } }
                   </excludes> }
                 </resource>

  lazy val testXml = <testResource>
                   <targetPath>{targetPath}</targetPath>
                   <filtering>{filtering}</filtering>
                   <directory>{directory}</directory>
                   { if (!includes.isEmpty) <includes>
                     { includes.map { Include(_).xml } }
                   </includes> }
                   { if (!excludes.isEmpty) <excludes>
                     { excludes.map { Exclude(_).xml } }
                   </excludes> }
                 </testResource>

  def makeModelObject(): org.apache.maven.model.Resource = {
    val resource = new org.apache.maven.model.Resource()
    resource.setTargetPath(targetPath)
    resource.setFiltering(filtering)
    resource.setDirectory(directory)
    includes.foreach { inc => resource.addInclude(inc) }
    excludes.foreach { exc => resource.addExclude(exc) }
    resource
  }
}

case class Include(include: String) {
  lazy val xml = <include>{include}</include>
}

case class Exclude(exclude: String) {
  lazy val xml = <exclude>{exclude}</exclude>
}
