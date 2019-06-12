
package org.apache.maven.unbound

import java.util.Properties

import scala.xml.Elem

import com.typesafe.config.ConfigFactory

import org.json4s._

case object Project extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  class ProjectSerializer extends CustomSerializer[Project](format => (
    {
      case obj @ JObject(fields) =>
        new Project(
          readBool(fields, ChildInheritUrl).getOrElse(true),
          readStr(fields, ModelVersion).getOrElse(DefaultModelVersion),
          readObject[Parent](obj, ParentStr),
          readStr(fields, GroupId).get,
          readStr(fields, ArtifactId).get,
          readStr(fields, Version).getOrElse(null),
          readStr(fields, Packaging).getOrElse(JarStr),
          readStr(fields, Name).getOrElse(null),
          readStr(fields, Description).getOrElse(null),
          readStr(fields, UrlStr).getOrElse(null),
          readStr(fields, InceptionYear).getOrElse(null),
          readObject[Organization](obj, OrganizationStr),
          readObjectSequence[License](fields, Licenses),
          readObjectSequence[Developer](fields, Developers),
          readObjectSequence[Contributor](fields, Contributors),
          readObjectSequence[MailingList](fields, MailingLists),
          readStringSequence(fields, Modules),
          readObject[Scm](obj, ScmStr),
          readObject[IssueManagement](obj, IssueManagementStr),
          readObject[CIManagement](obj, CIManagementStr),
          readObject[DistributionManagement](obj, DistributionManagementStr),
          readProperties(obj),
          readObjectSequence[Dependency](fields, DependencyManagementStr),
          readObjectSequence[Dependency](fields, Dependencies),
          readObjectSequence[Repository](fields, Repositories),
          readObjectSequence[Repository](fields, PluginRepositories),
          readObject[Build](obj, BuildStr),
          readObject[Reporting](obj, ReportingStr),
          readObjectSequence[Profile](fields, Profiles)
        )
    },
    {
      case p: Project =>
        JObject(Seq[Option[JField]](
          writeBool(ChildInheritUrl, p.childInheritUrl, true),
          writeStr(ModelVersion, p.modelVersion, "4.0.0"),
          writeObject(ParentStr, p.parent),
          writeStr(GroupId, p.groupId),
          writeStr(ArtifactId, p.artifactId),
          writeStr(Version, p.version),
          writeStr(Packaging, p.packaging, SL.JarStr),
          writeStr(Name, p.name),
          writeStr(Description, p.description),
          writeStr(UrlStr, p.url),
          writeStr(InceptionYear, p.inceptionYear),
          writeObject(OrganizationStr, p.organization),
          writeObjectSequence(Licenses, p.licenses),
          writeObjectSequence(Developers, p.developers),
          writeObjectSequence(Contributors, p.contributors),
          writeObjectSequence(MailingLists, p.mailingLists),
          writeStringSequence(Modules, p.modules),
          writeObject(ScmStr, p.scm),
          writeObject(IssueManagementStr, p.issueManagement),
          writeObject(CIManagementStr, p.ciManagement),
          writeObject(DistributionManagementStr, p.distributionManagement),
          writeProperties(PropertiesStr, p.properties),
          writeObjectSequence(DependencyManagementStr, p.dependencyManagement),
          writeObjectSequence(Dependencies, p.dependencies),
          writeObjectSequence(Repositories, p.repositories),
          writeObjectSequence(PluginRepositories, p.pluginRepositories),
          writeObject(BuildStr, p.build),
          writeObject(ReportingStr, p.reporting),
          writeObjectSequence(Profiles, p.profiles)
        ).flatten.toList)
    }
  ))
}

case class Project(
  childInheritUrl: Boolean = true,
  modelVersion: String = SL.DefaultModelVersion,
  parent: Parent = null, 
  groupId: String, artifactId: String, version: String, 
  packaging: String = SL.JarStr, name: String = null, 
  description: String = null, url: String = null, inceptionYear: String = null,
  organization: Organization = null,
  licenses: Seq[License] = Seq[License](),
  developers: Seq[Developer] = Seq[Developer](), 
  contributors: Seq[Contributor] = Seq[Contributor](),
  mailingLists: Seq[MailingList] = Seq[MailingList](), 
  modules: Seq[String] = Seq[String](), scm: Scm = null,
  issueManagement: IssueManagement = null, ciManagement: CIManagement = null,
  distributionManagement: DistributionManagement = null, 
  properties: Map[String, String] = Map[String, String](), 
  dependencyManagement: Seq[Dependency] = Seq[Dependency](),
  dependencies: Seq[Dependency] = Seq[Dependency](), 
  repositories: Seq[Repository] = Seq[Repository](),
  pluginRepositories: Seq[Repository] = Seq[Repository](), 
  build: Build = null, reporting: Reporting = null,
  profiles: Seq[Profile] = Seq[Profile]())
    extends Writeable with HoconProjectReader {

  def this(elem: Elem) = this(
    elem.attribute(SL.ChildInheritProjectFP).map { _.text }.
      getOrElse(SL.TrueStr.toString).toLowerCase == SL.TrueStr.toString,
    emptyToNull((elem \ SL.ModelVersion).text),
    (elem \ SL.ParentStr).map { case e: Elem => 
      new Parent(e) }.headOption.getOrElse(null),
    emptyToNull((elem \ SL.GroupId).text),
    emptyToNull((elem \ SL.ArtifactId).text),
    emptyToNull((elem \ SL.Version).text),
    emptyToDefault((elem \ SL.Packaging).text, SL.JarStr),
    emptyToNull((elem \ SL.Name).text),
    emptyToNull((elem \ SL.Description).text),
    emptyToNull((elem \ SL.UrlStr).text),
    emptyToNull((elem \ SL.InceptionYear).text),
    (elem \ SL.OrganizationStr).map { case e: Elem =>
      new Organization(e) }.headOption.getOrElse(null),
    (elem \ SL.Licenses \ SL.LicenseStr).map { case e: Elem => new License(e) },
    (elem \ SL.Developers \ SL.DeveloperStr).map { case e: Elem => 
      new Developer(e)},
    (elem \ SL.Contributors \ SL.ContributorStr).map { case e: Elem => 
      new Contributor(e) },
    (elem \ SL.MailingLists \ SL.MailingListStr).map { case e: Elem => 
      new MailingList(e) },
    (elem \ SL.Modules \ SL.Module).map { _.text },
    (elem \ SL.ScmStr).map { case e: Elem => 
      new Scm(elem) }.headOption.getOrElse(null),
    (elem \ SL.IssueManagementStr).map { case e: Elem => 
      new IssueManagement(elem) }.headOption.getOrElse(null),
    (elem \ SL.CIManagementStr).map { case e: Elem => 
      new CIManagement(elem) }.headOption.getOrElse(null),
    (elem \ SL.DistributionManagementStr).map { case e: Elem => 
      new DistributionManagement(elem) }.headOption.getOrElse(null),
    (elem \ SL.PropertiesStr).headOption.map(
      _.child.filter(_.isInstanceOf[Elem]).map { e => 
        (e.label, e.text.trim) }.toMap).getOrElse(Map[String, String]()),
    (elem \ SL.DependencyManagementStr \ SL.Dependencies \ 
      SL.DependencyStr).map { case e: Elem => new Dependency(e) },
    (elem \ SL.Dependencies \ SL.DependencyStr).map { case e: Elem =>
      new Dependency(e) },
    (elem \ SL.Repositories \ SL.RepositoryStr).map { case e: Elem =>
      new Repository(e) },
    (elem \ SL.PluginRepositories \ SL.PluginRepositoryStr).map {
      case e: Elem => new Repository(e) },
    (elem \ SL.BuildStr).map { case e: Elem => 
      new Build(e) }.headOption.getOrElse(null),
    (elem \ SL.ReportingStr).map { case e: Elem => 
      new Reporting(e) }.headOption.getOrElse(null),
    (elem \ SL.Profiles \ SL.ProfileStr).map { case e: Elem => new Profile(e) })

  lazy val xml = <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd" child.project.url.inherit.append.path={if (childInheritUrl) SL.TrueStr else SL.FalseStr}>
                   <modelVersion>{modelVersion}</modelVersion>
                   { if (parent != null) parent.xml }
                   <groupId>{groupId}</groupId>
                   <artifactId>{artifactId}</artifactId>
                   <version>{version}</version>
                   <packaging>{packaging}</packaging>
                   { if (name != null) <name>{name}</name> }
                   { if (description != null) <description>{description}</description> }
                   { if (url != null) <url>{url}</url> }
                   { if (inceptionYear != null) <inceptionYear>{inceptionYear}</inceptionYear> }
                   { if (organization != null) organization.xml }
                   { if (!licenses.isEmpty) <licenses>
                     { licenses.map { _.xml } }
                   </licenses> }
                   { if (!developers.isEmpty) <developers>
                     { developers.map { _.xml } }
                   </developers> }
                   { if (!contributors.isEmpty) <contributors>
                     { contributors.map { _.xml } }
                   </contributors> }
                   { if (!mailingLists.isEmpty) <mailingLists>
                     { mailingLists.map { _.xml } }
                   </mailingLists> }
                   { if (!modules.isEmpty) <modules>
                     { modules.map { Module(_).xml } }
                   </modules> }
                   { if (scm != null) scm.xml }
                   { if (issueManagement != null) issueManagement.xml }
                   { if (ciManagement != null) ciManagement.xml }
                   { if (distributionManagement != null) distributionManagement.xml }
                   { if (!properties.isEmpty) <properties>
                     { properties.map { case(k, v) => 
                         PropertyValue(k, v).xml } }
                   </properties> }
                   { if (!dependencyManagement.isEmpty) <dependencyManagement>
                     <dependencies>
                       { dependencyManagement.map { _.xml } }
                     </dependencies>
                   </dependencyManagement> }
                   { if (!dependencies.isEmpty) <dependencies>
                     { dependencies.map { _.xml } }
                   </dependencies> }
                   { if (!repositories.isEmpty) <repositories>
                     { repositories.map { _.xml } }
                   </repositories> }
                   { if (!pluginRepositories.isEmpty) <pluginRepositories>
                     { pluginRepositories.map { _.xml } }
                   </pluginRepositories> }
                   { if (build != null) build.xml }
                   { if (reporting != null) reporting.xml }
                   { if (!profiles.isEmpty) <profiles>
                     { profiles.map { _.xml } }
                   </profiles> }
                 </project>

  def makeModelObject(): org.apache.maven.model.Model = {
    val model = new org.apache.maven.model.Model()
    model.setModelVersion(modelVersion)
    if (parent != null) model.setParent(parent.makeModelObject())
    model.setGroupId(groupId)
    model.setArtifactId(artifactId)
    model.setVersion(version)
    model.setPackaging(packaging)
    model.setName(name)
    model.setDescription(description)
    model.setUrl(url)
    model.setInceptionYear(inceptionYear)
    if (organization != null) 
      model.setOrganization(organization.makeModelObject())
    licenses.foreach { lic => model.addLicense(lic.makeModelObject()) }
    developers.foreach { dev => model.addDeveloper(dev.makeModelObject()) }
    contributors.foreach { c => model.addContributor(c.makeModelObject()) }
    mailingLists.foreach { ml => model.addMailingList(ml.makeModelObject()) }
    modules.foreach { module => model.addModule(module) }
    if (issueManagement != null)
      model.setIssueManagement(issueManagement.makeModelObject())
    if (ciManagement != null)
      model.setCiManagement(ciManagement.makeModelObject())
    if (distributionManagement != null) 
      model.setDistributionManagement(distributionManagement.makeModelObject())
    properties.foreach { case(k, v) => model.addProperty(k, v) }
    val mgmt = new org.apache.maven.model.DependencyManagement()
    dependencyManagement.foreach { d =>
      mgmt.addDependency(d.makeModelObject()) }
    model.setDependencyManagement(mgmt)
    dependencies.foreach { d => model.addDependency(d.makeModelObject()) }
    repositories.foreach { r => model.addRepository(r.makeModelObject()) }
    pluginRepositories.foreach { repo => 
      model.addPluginRepository(repo.makeModelObject()) }
    if (build != null) model.setBuild(build.makeModelObject())
    if (reporting != null) model.setReporting(reporting.makeModelObject())
    profiles.foreach { pro => model.addProfile(pro.makeModelObject()) }
    model
  }
}
