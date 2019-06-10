
package org.apache.maven.unbound

trait Labels {

  sealed trait Str
  implicit def strToString(s: Str): String = s.toString

  object ParentStr extends Str {
    override def toString(): String = "parent"
  }
  object OrganizationStr extends Str {
    override def toString(): String = "organization"
  }
  object BuildStr extends Str {
    override def toString(): String = "build"
  }
  object Modules extends Str {
    override def toString(): String = "modules"
  }
  object Module extends Str {
    override def toString(): String = "module"
  }
  object Licenses extends Str {
    override def toString(): String = "licenses"
  }
  object LicenseStr extends Str {
    override def toString(): String = "license"
  }
  object PropertiesStr extends Str {
    override def toString(): String = "properties"
  }
  object Developers extends Str {
    override def toString(): String = "developers"
  }
  object DeveloperStr extends Str {
    override def toString(): String = "developer"
  }
  object Contributors extends Str {
    override def toString(): String = "contributors"
  }
  object ContributorStr extends Str {
    override def toString(): String = "contributor"
  }
  object MailingLists extends Str {
    override def toString(): String = "mailingLists"
  }
  object MailingListStr extends Str {
    override def toString(): String = "mailingList"
  }
  object ScmStr extends Str {
    override def toString(): String = "scm"
  }
  object IssueManagementStr extends Str {
    override def toString(): String = "issueManagement"
  }
  object CIManagementStr extends Str {
    override def toString(): String = "ciManagement"
  }
  object DistributionManagementStr extends Str {
    override def toString(): String = "distributionManagement"
  }
  object DependencyManagementStr extends Str {
    override def toString(): String = "dependencyManagement"
  }
  object Dependencies extends Str {
    override def toString(): String = "dependencies"
  }
  object DependencyStr extends Str {
    override def toString(): String = "dependency"
  }
  object Repositories extends Str {
    override def toString(): String = "repositories"
  }
  object RepositoryStr extends Str {
    override def toString(): String = "repository"
  }
  object PluginRepositories extends Str {
    override def toString(): String = "pluginRepositories"
  }
  object PluginRepositoryStr extends Str {
    override def toString(): String = "pluginRepository"
  }
  object ReportingStr extends Str {
    override def toString(): String = "reporting"
  }
  object Profiles extends Str {
    override def toString(): String = "profiles"
  }
  object ProfileStr extends Str {
    override def toString(): String = "profile"
  }
  object ActivationStr extends Str {
    override def toString(): String = "activation"
  }

  object Id extends Str {
    override def toString(): String = "id"
  }
  object DefaultStr extends Str {
    override def toString(): String = "default"
  }
  object ChildInheritUrl extends Str {
    override def toString(): String = "childInheritUrl"
  }
  object ChildInheritProjectFP extends Str {
    override def toString(): String = "child.project.url.inherit.append.path"
  }
  object ModelVersion extends Str {
    override def toString(): String = "modelVersion"
  }
  object DefaultModelVersion extends Str {
    override def toString(): String = "4.0.0"
  }
  object GroupId extends Str {
    override def toString(): String = "groupId"
  }
  object ArtifactId extends Str {
    override def toString(): String = "artifactId"
  }
  object Version extends Str {
    override def toString(): String = "version"
  }
  object Packaging extends Str {
    override def toString(): String = "packaging"
  }
  object JarStr extends Str {
    override def toString(): String = "jar"
  }
  object Name extends Str {
    override def toString(): String = "name"
  }
  object Email extends Str {
    override def toString(): String = "email"
  }
  object Mail extends Str {
    override def toString(): String = "mail"
  }
  object Description extends Str {
    override def toString(): String = "description"
  }
  object UrlStr extends Str {
    override def toString(): String = "url"
  }
  object InceptionYear extends Str {
    override def toString(): String = "inceptionYear"
  }
  object SourceDirectory extends Str {
    override def toString(): String = "sourceDirectory"
  }
  object ScriptSourceDirectory extends Str {
    override def toString(): String = "scriptSourceDirectory"
  }
  object TestSourceDirectory extends Str {
    override def toString(): String = "testSourceDirectory"
  }
  object OutputDirectory extends Str {
    override def toString(): String = "outputDirectory"
  }
  object TestOutputDirectory extends Str {
    override def toString(): String = "testOutputDirectory"
  }
  object Extensions extends Str {
    override def toString(): String = "extensions"
  }
  object ExtensionStr extends Str {
    override def toString(): String = "extension"
  }
  object DefaultGoal extends Str {
    override def toString(): String = "defaultGoal"
  }
  object Resources extends Str {
    override def toString(): String = "resources"
  }
  object ResourceStr extends Str {
    override def toString(): String = "resource"
  }
  object TestResources extends Str {
    override def toString(): String = "testResources"
  }
  object TestResource extends Str {
    override def toString(): String = "testResource"
  }
  object DirectoryStr extends Str {
    override def toString(): String = "directory"
  }
  object FinalName extends Str {
    override def toString(): String = "finalName"
  }
  object Filters extends Str {
    override def toString(): String = "filters"
  }
  object FilterStr extends Str {
    override def toString(): String = "filter"
  }
  object PluginManagement extends Str {
    override def toString(): String = "pluginManagement"
  }
  object Plugins extends Str {
    override def toString(): String = "plugins"
  }
  object PluginStr extends Str {
    override def toString(): String = "plugin"
  }
  object Target extends Str {
    override def toString(): String = "target"
  }
  object TargetPath extends Str {
    override def toString(): String = "targetPath"
  }
  object Filtering extends Str {
    override def toString(): String = "filtering"
  }
  object Includes extends Str {
    override def toString(): String = "includes"
  }
  object IncludeStr extends Str {
    override def toString(): String = "include"
  }
  object Excludes extends Str {
    override def toString(): String = "excludes"
  }
  object ExcludeStr extends Str {
    override def toString(): String = "exclude"
  }
  object Exclusions extends Str {
    override def toString(): String = "exclusions"
  }
  object ExclusionStr extends Str {
    override def toString(): String = "exclusion"
  }
  object Notifiers extends Str {
    override def toString(): String = "notifiers"
  }
  object NotifierStr extends Str {
    override def toString(): String = "notifier"
  }
  object SystemStr extends Str {
    override def toString(): String = "system"
  }
  object TypeStr extends Str {
    override def toString(): String = "type"
  }
  object SendOnError extends Str {
    override def toString(): String = "sendOnError"
  }
  object SendOnFailure extends Str {
    override def toString(): String = "sendOnFailure"
  }
  object SendOnSuccess extends Str {
    override def toString(): String = "sendOnSuccess"
  }
  object SendOnWarning extends Str {
    override def toString(): String = "sendOnWarning"
  }
  object Configuration extends Str {
    override def toString(): String = "configuration"
  }
  object OrganizationUrl extends Str {
    override def toString(): String = "organizationUrl"
  }
  object Roles extends Str {
    override def toString(): String = "roles"
  }
  object RoleStr extends Str {
    override def toString(): String = "role"
  }
  object TimezoneStr extends Str {
    override def toString(): String = "timezone"
  }
  object Classifier extends Str {
    override def toString(): String = "classifier"
  }
  object Scope extends Str {
    override def toString(): String = "scope"
  }
  object Compile extends Str {
    override def toString(): String = "compile"
  }
  object SystemPath extends Str {
    override def toString(): String = "systemPath"
  }
  object OptionalStr extends Str {
    override def toString(): String = "optional"
  }
  object Subscribe extends Str {
    override def toString(): String = "subscribe"
  }
  object Unsubscribe extends Str {
    override def toString(): String = "unsubscribe"
  }
  object Post extends Str {
    override def toString(): String = "post"
  }
  object Archive extends Str {
    override def toString(): String = "archive"
  }
  object OtherArchives extends Str {
    override def toString(): String = "otherArchives"
  }
  object OtherArchive extends Str {
    override def toString(): String = "otherArchive"
  }
  object Distribution extends Str {
    override def toString(): String = "distribution"
  }
  object RelativePath extends Str {
    override def toString(): String = "relativePath"
  }
  object Comments extends Str {
    override def toString(): String = "comments"
  }
  object Connection extends Str {
    override def toString(): String = "connection"
  }
  object DeveloperConnection extends Str {
    override def toString(): String = "developerConnection"
  }
  object Tag extends Str {
    override def toString(): String = "tag"
  }
  object ChildInheritScmUrlFP extends Str {
    override def toString(): String = "child.scm.url.inherit.append.path"
  }
  object ChildInheritConnection extends Str {
    override def toString(): String = "childInheritConnection"
  }
  object ChildInheritDeveloperConnection extends Str {
    override def toString(): String = "childInheritDeveloperConnection"
  }
  object ChildInheritConnectionFP extends Str {
    override def toString(): String = "child.scm.connection.inherit.append.path"
  }
  object ChildInheritDeveloperConnectionFP extends Str {
    override def toString(): String = 
      "child.scm.developerConnection.inherit.append.path"
  }
  object ChildInheritSiteUrlFP extends Str {
    override def toString(): String = "child.site.url.inherit.append.path"
  }
  object Snapshots extends Str {
    override def toString(): String = "snapshots"
  }
  object SnapshotRepository extends Str {
    override def toString(): String = "snapshotRepository"
  }
  object SiteStr extends Str {
    override def toString(): String = "site"
  }
  object DownloadUrl extends Str {
    override def toString(): String = "downloadUrl"
  }
  object RelocationStr extends Str {
    override def toString(): String = "relocation"
  }
  object Status extends Str {
    override def toString(): String = "status"
  }
  object Message extends Str {
    override def toString(): String = "message"
  }
  object Executions extends Str {
    override def toString(): String = "executions"
  }
  object ExecutionStr extends Str {
    override def toString(): String = "execution"
  }
  object Inherited extends Str {
    override def toString(): String = "inherited"
  }
  object Phase extends Str {
    override def toString(): String = "phase"
  }
  object Goals extends Str {
    override def toString(): String = "goals"
  }
  object GoalStr extends Str {
    override def toString(): String = "goal"
  }
  object ExcludeDefaults extends Str {
    override def toString(): String = "excludeDefaults"
  }
  object ReportStr extends Str {
    override def toString(): String = "report"
  }
  object Reports extends Str {
    override def toString(): String = "reports"
  }
  object ReportSets extends Str {
    override def toString(): String = "reportSets"
  }
  object ReportSetStr extends Str {
    override def toString(): String = "reportSet"
  }
  object UniqueVersion extends Str {
    override def toString(): String = "uniqueVersion"
  }
  object Releases extends Str {
    override def toString(): String = "releases"
  }
  object Layout extends Str {
    override def toString(): String = "layout"
  }
  object Enabled extends Str {
    override def toString(): String = "enabled"
  }
  object UpdatePolicy extends Str {
    override def toString(): String = "updatePolicy"
  }
  object ChecksumPolicy extends Str {
    override def toString(): String = "checksumPolicy"
  }
  object TrueStr extends Str {
    override def toString(): String = "true"
  }
  object FalseStr extends Str {
    override def toString(): String = "false"
  }
  object Dot extends Str {
    override def toString(): String = "."
  }
  object DefaultPluginGroup extends Str {
    override def toString(): String = "org.apache.maven.plugins"
  }
}