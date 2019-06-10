
package org.apache.maven.hocon

import java.io.StringReader

import scala.xml.Elem

import com.typesafe.config.{ Config, ConfigObject, ConfigFactory }

import org.apache.maven.shared.utils.xml.Xpp3DomBuilder

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
        JObject(
          JField(SourceDirectory, JString(b.sourceDirectory)) ::
          JField(ScriptSourceDirectory, JString(b.scriptSourceDirectory)) ::
          JField(TestSourceDirectory, JString(b.testSourceDirectory)) ::
          JField(OutputDirectory, JString(b.outputDirectory)) ::
          JField(TestOutputDirectory, JString(b.testOutputDirectory)) ::
          JField(
            Extensions, JArray(
              b.extensions.map { ex => Extraction.decompose(ex) }.toList)) ::
          JField(DefaultGoal, JString(b.defaultGoal)) ::
          JField(
            Resources, 
            JArray(b.resources.map { r => Extraction.decompose(r) }.toList)) ::
          JField(
            TestResources, 
            JArray(b.resources.map { r => Extraction.decompose(r) }.toList)) ::
          JField(DirectoryStr, JString(b.directory)) ::
          JField(FinalName, JString(b.finalName)) ::
          JField(
            Filters,
            JArray(b.filters.map { filter => JString(filter) }.toList)) ::
          JField(
            PluginManagement, 
            JArray(b.pluginManagement.map { p => 
              Extraction.decompose(p) }.toList)) ::
          JField(
            Plugins, 
            JArray(b.pluginManagement.map { p => 
              Extraction.decompose(p) }.toList)) ::
          Nil)
    }
  ))
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
  finalName: String = null, /*${artifactId}-${version} */
  filters: Seq[String] = Seq[String](), 
  pluginManagement: Seq[Plugin] = Seq[Plugin](), 
  plugins: Seq[Plugin] = Seq[Plugin]()) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.SourceDirectory).text, Build.defSourceDir),
    emptyToDefault((elem \ SL.ScriptSourceDirectory).text, Build.defScriptDir),
    emptyToDefault((elem \ SL.TestSourceDirectory).text, Build.defTestDir),
    emptyToDefault((elem \ SL.OutputDirectory).text, Build.defOutputDir),
    emptyToDefault((elem \ SL.TestOutputDirectory).text, Build.defTestOutputDir),
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
    emptyToNull((elem \ SL.DirectoryStr).text), 
    emptyToNull((elem \ SL.FinalName).text),
    (elem \ SL.Filters \ SL.FilterStr).map { _.text },
    (elem \ SL.PluginManagement \ SL.Plugins \ SL.PluginStr).map { 
      case e: Elem => new Plugin(e) },
    (elem \ SL.Plugins \ SL.PluginStr).map { case e: Elem => new Plugin(e) })

  private def isDefaultResources(res: Seq[Resource]): Boolean = {
    res.size == 1 && res(0).targetPath == Build.defResourcesDir &&
    res(0).directory == SL.Dot.toString && res(0).includes.size == 1 &&
    res(0).excludes.size == 0 && res(0).includes(0) == Build.defResourcesDir
  }

  private def isDefaultTestResources(res: Seq[Resource]): Boolean = {
    res.size == 1 && res(0).targetPath == Build.defTestResourcesDir &&
    res(0).directory == SL.Dot.toString && res(0).includes.size == 1 &&
    res(0).excludes.size == 0 && res(0).includes(0) == Build.defTestResourcesDir
  }

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
      { if (!resources.isEmpty && !isDefaultResources(resources)) 
        <resources> { resources.map { _.xml } } </resources> }
      { if (!testResources.isEmpty && !isDefaultTestResources(testResources)) 
        <testResources> { testResources.map { _.xml } } </testResources> }
      { if (directory != null && directory != SL.Target.toString)
        <directory>{directory}</directory> }
      { if (finalName != null) <finalName>{finalName}</finalName> }
      { if (!filters.isEmpty) <filters>
        { filters.map { Filter(_).xml } }
        </filters> }
      { if (!pluginManagement.isEmpty) <pluginManagement>
        { pluginManagement.map { _.xml } }
        </pluginManagement> }
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

case object Extension extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ExtensionSerializer extends CustomSerializer[Extension](format => (
    {
      case JObject(fields) =>
        new Extension(
          readStr(fields, GroupId).get,
          readStr(fields, ArtifactId).get,
          readStr(fields, Version).getOrElse(null)
        )
    },
    {
      case e: Extension =>
        JObject(
          JField(GroupId, JString(e.groupId)) ::
          JField(ArtifactId, JString(e.artifactId)) ::
          JField(Version, JString(e.version)) ::
          Nil)
    }
  ))
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
        JObject(
          JField(TargetPath, JString(r.targetPath)) ::
          JField(Filtering, JBool(r.filtering)) ::
          JField(DirectoryStr, JString(r.directory)) ::
          JField(Includes, JArray(r.includes.map { JString(_) }.toList)) ::
          JField(Excludes, JArray(r.excludes.map { JString(_) }.toList)) ::
          Nil)
    }
  ))
}

case class Resource(
  targetPath: String, filtering: Boolean = false, directory: String = SL.Dot,
  includes: Seq[String], excludes: Seq[String] = Seq[String]()) {

  def this(name: String) = this(name, false, SL.Dot, Seq[String](name))

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.TargetPath).text), 
    emptyToDefault((elem \ SL.Filtering).text.toLowerCase, SL.FalseStr) == 
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
