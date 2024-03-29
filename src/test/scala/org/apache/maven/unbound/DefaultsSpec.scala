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

import java.io.{ File, FileReader }

import scala.xml.XML

import com.typesafe.config.{ 
  Config, ConfigFactory, ConfigResolveOptions, ConfigParseOptions }
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader
import org.json4s._
import org.json4s.native.JsonMethods
import org.scalatest.{ FlatSpec, Matchers }

class DefaultsSpec extends FlatSpec with Matchers {

  behavior of "case class defaults"

  it should "be correct for Project" in {

    val correct = Project(
      true, "4.0.0", null, "group", "artifact", "version", "jar", 
      null, null, null, null,
      null, Seq[License](), Seq[Developer](), Seq[Contributor](), 
      Seq[MailingList](), Seq[String](), null, null, null, null, 
      Map[String, String](), Seq[Dependency](), Seq[Dependency](), 
      Seq[Repository](), Seq[Repository](), null, null, Seq[Profile]())
    Project(
      groupId = "group", artifactId = "artifact", 
      version = "version").toString should be(correct.toString)

    val xmlProject: Project = new Project(XML.loadString(
      """<project><groupId>group</groupId><artifactId>artifact</artifactId><version>version</version></project>"""))
    xmlProject.toString should be(correct.toString)

    val jsonProject: Project = JsonReader.readPOM(
      """{ "project" : { "groupId" : "group", "artifactId" : "artifact", "version" : "version" } }""")
    jsonProject.toString should be(correct.toString)

    val hoconProject = 
      HoconReader.readPOM(ConfigFactory.parseString(
        """{ project : { groupId : "group", artifactId : "artifact", version : "version" } }"""))
    hoconProject.toString should be(correct.toString)
  }

  it should "be correct for Build and Resources" in {

    val res = Resource(
      null, false, ".", Seq[String]("src/main/resources"), Seq[String]())
    val testRes = Resource(
      null, false, ".", Seq[String]("src/test/resources"), Seq[String]())

    val correct = Build(
      "src/main/java", "src/main/scripts", "src/test/java", 
      "target/classes", "target/test-classes",
      Seq[Extension](), null, 
      Seq(res), Seq(testRes),
      "target", null,
      Seq[String](), Seq[Plugin](), Seq[Plugin]())
    Build().toString should be(correct.toString)

    val xmlBuild = new Build(XML.loadString("""<build></build>"""))
    xmlBuild.toString should be(correct.toString)

    val jsonBuild = JsonReader.readObject[Build](
      JsonMethods.parse("""{ "build" : {} }""").asInstanceOf[JObject], 
      "build")
    jsonBuild.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ build : {} }""")
    import HoconReader._
    conf.as[Build]("build").toString should be(correct.toString)
  }

  it should "be correct for Extension" in {

    val correct = Extension(null, null, null)
    Extension().toString should be(correct.toString)

    val xml = new Extension(XML.loadString("""<extension></extension>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Extension](
      JsonMethods.parse("""{ "extension" : {} }""").asInstanceOf[JObject], 
      "extension")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ extension : {} }""")
    import HoconReader._
    conf.as[Extension]("extension").toString should be(correct.toString)
  }

  it should "be correct for CIManagement" in {

    val correct = CIManagement(null, null, Seq[Notifier]())
    CIManagement().toString should be(correct.toString)

    val xml = 
      new CIManagement(XML.loadString("""<cimanagement></cimanagement>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[CIManagement](
      JsonMethods.parse("""{ "ciManagement" : {} }""").asInstanceOf[JObject], 
      "ciManagement")
    json.toString should be(correct.toString)

    import HoconReader._

    val conf = ConfigFactory.parseString("""{ ciManagement : {} }""")
    import HoconReader._
    conf.as[CIManagement]("ciManagement").toString should be(correct.toString)
  }

  it should "be correct for Notifier" in {

    val correct = 
      Notifier("mail", true, true, true, true, Map[String, String]())
    Notifier().toString should be(correct.toString)

    val xml = new Notifier(XML.loadString("""<notifier></notifier>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Notifier](
      JsonMethods.parse("""{ "notifier" : {} }""").asInstanceOf[JObject], 
      "notifier")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ notifier : {} }""")
    import HoconReader._
    conf.as[Notifier]("notifier").toString should be(correct.toString)
  }

  it should "be correct for Contributor" in {

    val correct = Contributor(
      null, null, null, null, null, Seq[String](), null, Map[String, String]())
    Contributor().toString should be(correct.toString)

    val xml = new Contributor(XML.loadString("""<contributor></contributor>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Contributor](
      JsonMethods.parse("""{ "contributor" : {} }""").asInstanceOf[JObject], 
      "contributor")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ contributor : {} }""")
    import HoconReader._
    conf.as[Contributor]("contributor").toString should be(correct.toString)
  }

  it should "be correct for Developer" in {

    val correct = Developer(
      null, null, null, null, null, null, Seq[String](), null, 
      Map[String, String]())
    Developer().toString should be(correct.toString)

    val xml = new Developer(XML.loadString("""<developer></developer>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Developer](
      JsonMethods.parse("""{ "developer" : {} }""").asInstanceOf[JObject], 
      "developer")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ developer : {} }""")
    import HoconReader._
    conf.as[Developer]("developer").toString should be(correct.toString)
  }

  it should "be correct for Dependency" in {

    val correct = Dependency(
      null, null, null, "jar", null, "compile", null, Seq[Exclusion](), false)
    Dependency(null, null).toString should be(correct.toString)

    val xml = new Dependency(XML.loadString("""<dependency></dependency>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Dependency](
      JsonMethods.parse("""{ "dependency" : {} }""").asInstanceOf[JObject], 
      "dependency")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ dependency : {} }""")
    import HoconReader._
    conf.as[Dependency]("dependency").toString should be(correct.toString)
  }

  it should "be correct for Parent" in {

    val correct = Parent(null, null, null, "../pom.xml")
    Parent().toString should be(correct.toString)

    val xml = new Parent(XML.loadString("""<parent></parent>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Parent](
      JsonMethods.parse("""{ "parent" : {} }""").asInstanceOf[JObject], 
      "parent")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ parent : {} }""")
    import HoconReader._
    conf.as[Parent]("parent").toString should be(correct.toString)
  }

  it should "be correct for Organization" in {

    val correct = Organization(null, null)
    Organization().toString should be(correct.toString)

    val xml = 
      new Organization(XML.loadString("""<organization></organization>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Organization](
      JsonMethods.parse("""{ "organization" : {} }""").asInstanceOf[JObject], 
      "organization")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ organization : {} }""")
    import HoconReader._
    conf.as[Organization]("organization").toString should be(correct.toString)
  }

  it should "be correct for License" in {

    val correct = License(null, null, "repo", "")
    License(null, null).toString should be(correct.toString)

    val xml = new License(XML.loadString("""<license></license>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[License](
      JsonMethods.parse("""{ "license" : {} }""").asInstanceOf[JObject], 
      "license")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ license : {} }""")
    import HoconReader._
    conf.as[License]("license").toString should be(correct.toString)
  }

  it should "be correct for Scm" in {

    val correct = Scm(true, true, true, null, null, "HEAD", null)
    Scm().toString should be(correct.toString)

    val xml = new Scm(XML.loadString("""<scm></scm>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Scm](
      JsonMethods.parse("""{ "scm" : {} }""").asInstanceOf[JObject], 
      "scm")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ scm : {} }""")
    import HoconReader._
    conf.as[Scm]("scm").toString should be(correct.toString)
  }

  it should "be correct for IssueManagement" in {

    val correct = IssueManagement(null, null)
    IssueManagement().toString should be(correct.toString)

    val xml = new IssueManagement(
      XML.loadString("""<issueManagement></issueManagement>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[IssueManagement](
      JsonMethods.parse("""{ "issueManagement" : {} }""").asInstanceOf[JObject],
      "issueManagement")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ issueManagement : {} }""")
    import HoconReader._
    conf.as[IssueManagement]("issueManagement").toString should be(
      correct.toString)
  }

  it should "be correct for DistributionManagement" in {

    val correct = DistributionManagement(null, null, null, null, null, "none")
    DistributionManagement().toString should be(correct.toString)

    val xml = new DistributionManagement(
      XML.loadString("""<distributionManagement></distributionManagement>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[DistributionManagement](
      JsonMethods.parse(
        """{ "distributionManagement" : {} }""").asInstanceOf[JObject],
      "distributionManagement")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ distributionManagement : {} }""")
    import HoconReader._
    conf.as[DistributionManagement](
      "distributionManagement").toString should be(correct.toString)
  }

  it should "be correct for Site" in {

    val correct = Site(true, null, null, null)
    Site().toString should be(correct.toString)

    val xml = new Site(XML.loadString("""<site></site>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Site](
      JsonMethods.parse("""{ "site" : {} }""").asInstanceOf[JObject], 
      "site")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ site : {} }""")
    import HoconReader._
    conf.as[Site]("site").toString should be(correct.toString)
  }

  it should "be correct for Relocation" in {

    val correct = Relocation(null, null, null, null)
    Relocation().toString should be(correct.toString)

    val xml = new Relocation(XML.loadString("""<relocation></relocation>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Relocation](
      JsonMethods.parse("""{ "relocation" : {} }""").asInstanceOf[JObject], 
      "relocation")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ relocation : {} }""")
    import HoconReader._
    conf.as[Relocation]("relocation").toString should be(correct.toString)
  }

  it should "be correct for Plugin" in {

    val correct = Plugin(
      "org.apache.maven.plugins", null, null, false, 
      Seq[Execution](), Seq[Dependency](), true, null)
    Plugin().toString should be(correct.toString)

    val xml = new Plugin(XML.loadString("""<plugin></plugin>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Plugin](
      JsonMethods.parse("""{ "plugin" : {} }""").asInstanceOf[JObject], 
      "plugin")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ plugin : {} }""")
    import HoconReader._
    conf.as[Plugin]("plugin").toString should be(correct.toString)
  }

  it should "be correct for Execution" in {

    val correct = Execution("default", null, Seq[String](), true, null)
    Execution().toString should be(correct.toString)

    val xml = new Execution(XML.loadString("""<execution></execution>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Execution](
      JsonMethods.parse("""{ "execution" : {} }""").asInstanceOf[JObject], 
      "execution")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ execution : {} }""")
    import HoconReader._
    conf.as[Execution]("execution").toString should be(correct.toString)
  }

  it should "be correct for Profile" in {

    val correct = Profile(
      "default", null, null, Seq[String](), null, Map[String, String](), 
      Seq[Dependency](), Seq[Dependency](), 
      Seq[Repository](), Seq[Repository](), null)
    Profile().toString should be(correct.toString)

    val xml = new Profile(XML.loadString("""<profile></profile>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Profile](
      JsonMethods.parse("""{ "profile" : {} }""").asInstanceOf[JObject], 
      "profile")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ profile : {} }""")
    import HoconReader._
    conf.as[Profile]("profile").toString should be(correct.toString)
  }

  it should "be correct for Activation" in {

    val correct = Activation(false, null, null, null, null)
    Activation().toString should be(correct.toString)

    val xml = new Activation(XML.loadString("""<activation></activation>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Activation](
      JsonMethods.parse("""{ "activation" : {} }""").asInstanceOf[JObject], 
      "activation")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ activation : {} }""")
    import HoconReader._
    conf.as[Activation]("activation").toString should be(correct.toString)
  }

  it should "be correct for ActivationOS" in {

    val correct = ActivationOS(null, null, null, null)
    ActivationOS().toString should be(correct.toString)

    val xml = new ActivationOS(XML.loadString("""<os></os>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[ActivationOS](
      JsonMethods.parse("""{ "os" : {} }""").asInstanceOf[JObject], "os")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ os : {} }""")
    import HoconReader._
    conf.as[ActivationOS]("os").toString should be(correct.toString)
  }

  it should "be correct for ActivationProperty" in {

    val correct = ActivationProperty(null, null)
    ActivationProperty().toString should be(correct.toString)

    val xml = 
      new ActivationProperty(XML.loadString("""<property></property>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[ActivationProperty](
      JsonMethods.parse("""{ "property" : {} }""").asInstanceOf[JObject], 
      "property")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ property : {} }""")
    import HoconReader._
    conf.as[ActivationProperty]("property").toString should be(correct.toString)
  }

  it should "be correct for ActivationFile" in {

    val correct = ActivationFile(null, null)
    ActivationFile().toString should be(correct.toString)

    val xml = new ActivationFile(XML.loadString("""<file></file>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[ActivationFile](
      JsonMethods.parse("""{ "file" : {} }""").asInstanceOf[JObject], "file")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ file : {} }""")
    import HoconReader._
    conf.as[ActivationFile]("file").toString should be(correct.toString)
  }

  it should "be correct for BuildBase" in {

    val res = Resource(
      null, false, ".", Seq[String]("src/main/resources"), Seq[String]())
    val testRes = Resource(
      null, false, ".", Seq[String]("src/test/resources"), Seq[String]())

    val correct = BuildBase(
      null, Seq(res), Seq(testRes), "target", null, Seq[String](), 
      Seq[Plugin](), Seq[Plugin]())
    BuildBase().toString should be(correct.toString)

    val xml = new BuildBase(XML.loadString("""<buildBase></buildBase>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[BuildBase](
      JsonMethods.parse("""{ "buildBase" : {} }""").asInstanceOf[JObject], 
      "buildBase")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ buildBase : {} }""")
    import HoconReader._
    conf.as[BuildBase]("buildBase").toString should be(correct.toString)
  }

  it should "be correct for Reporting" in {

    val correct = Reporting(false, "target/site", Seq[ReportPlugin]())
    Reporting().toString should be(correct.toString)

    val xml = new Reporting(XML.loadString("""<reporting></reporting>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Reporting](
      JsonMethods.parse("""{ "reporting" : {} }""").asInstanceOf[JObject], 
      "reporting")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ reporting : {} }""")
    import HoconReader._
    conf.as[Reporting]("reporting").toString should be(correct.toString)
  }

  it should "be correct for ReportPlugin" in {

    val correct = ReportPlugin(
      "org.apache.maven.plugins", null, null, Seq[ReportSet](), true, null)
    ReportPlugin().toString should be(correct.toString)

    val xml = new ReportPlugin(
      XML.loadString("""<reportPlugin></reportPlugin>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[ReportPlugin](
      JsonMethods.parse("""{ "reportPlugin" : {} }""").asInstanceOf[JObject], 
      "reportPlugin")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ reportPlugin : {} }""")
    import HoconReader._
    conf.as[ReportPlugin]("reportPlugin").toString should be(correct.toString)
  }

  it should "be correct for ReportSet" in {

    val correct = ReportSet("default", Seq[String](), true, null)
    ReportSet().toString should be(correct.toString)

    val xml = new ReportSet(XML.loadString("""<reportSet></reportSet>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[ReportSet](
      JsonMethods.parse("""{ "reportSet" : {} }""").asInstanceOf[JObject], 
      "reportSet")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ reportSet : {} }""")
    import HoconReader._
    conf.as[ReportSet]("reportSet").toString should be(correct.toString)
  }

  it should "be correct for DeploymentRepository" in {

    val correct = 
      DeploymentRepository(true, null, null, null, null, null, "default")
    DeploymentRepository().toString should be(correct.toString)

    val xml = new DeploymentRepository(
      XML.loadString("""<deploymentRepository></deploymentRepository>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[DeploymentRepository](JsonMethods.parse(
      """{ "deploymentRepository" : {} }""").asInstanceOf[JObject],
      "deploymentRepository")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ deploymentRepository : {} }""")
    import HoconReader._
    conf.as[DeploymentRepository](
      "deploymentRepository").toString should be(correct.toString)
  }

  it should "be correct for RepositoryPolicy" in {

    val correct = RepositoryPolicy(true, "daily", "warn")
    RepositoryPolicy().toString should be(correct.toString)

    val xml = new RepositoryPolicy(
      XML.loadString("""<repositoryPolicy></repositoryPolicy>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[RepositoryPolicy](JsonMethods.parse(
      """{ "repositoryPolicy" : {} }""").asInstanceOf[JObject],
      "repositoryPolicy")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ repositoryPolicy : {} }""")
    import HoconReader._
    conf.as[RepositoryPolicy]("repositoryPolicy").toString should be(
      correct.toString)
  }

  it should "be correct for Repository" in {

    val correct = Repository(null, null, null, null, null, "default")
    Repository().toString should be(correct.toString)

    val xml = new Repository(XML.loadString("""<repository></repository>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Repository](
      JsonMethods.parse("""{ "repository" : {} }""").asInstanceOf[JObject], 
      "repository")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ repository : {} }""")
    import HoconReader._
    conf.as[Repository]("repository").toString should be(correct.toString)
  }

  it should "be correct for Archiver" in {

    val correct = Archiver(
      true, true, true, false, null, Map[String, String](), null, 
      Seq[ManifestSection](), null)
    Archiver().toString should be(correct.toString)

    val xml = new Archiver(XML.loadString("""<archive></archive>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Archiver](
      JsonMethods.parse("""{ "archive" : {} }""").asInstanceOf[JObject], 
      "archive")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ archive : {} }""")
    HoconReader.readArchiver(conf).toString should be(correct.toString)
  }

  it should "be correct for Fileset" in {

    val correct = Fileset(
      null, null, false, null, true, Seq[String](), Seq[String](), 
      "0644", "0755", null)
    Fileset().toString should be(correct.toString)

    val xml = new Fileset(XML.loadString("""<fileset></fileset>"""))
    xml.toString should be(correct.toString)

    val json = JsonReader.readObject[Fileset](
      JsonMethods.parse("""{ "fileset" : {} }""").asInstanceOf[JObject], 
      "fileset")
    json.toString should be(correct.toString)

    val conf = ConfigFactory.parseString("""{ fileset : {} }""")
    HoconReader.readFileset(conf).toString should be(correct.toString)
  }
}
