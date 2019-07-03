# Usage

## Json and Hocon

Hocon is really a version of Json with less syntax.  Commas can be removed
when a value is on a single line.  Colons can be replaced by equals and when
a value is an object, the colon or equals can be dropped altogether.  Since
these forms are similar we will only give examples in Hocon.  Json examples
are the same with the added syntax of colons, quotes for keys and commas.

Another thing to note is that Typesafe Config variable resolving is disabled
in favor of Maven's.  This is obviously necessary as resolving variables in
Maven's style is preferred even if the two styles are nearly identical.


### Hocon Includes (the good stuff)

Hocon has an additional syntax of particular interest to Unbound's use cases.
Hocon can include other hocon files.  This means you can abstract arbitrary
parts of a multi-module build system into any Hocon file.  Then these build
components can be included in sub-modules.  For instance in a file called
scalaCompile.conf could be
```
dependencies = [
  {
    groupId = "org.scala-lang"
    artifactId = "scala-library"
    version = "${versionScalaRelease}"
    scope = "compile"
  }
]
build {
  plugins = [
    {
      groupId = "org.apache.maven.plugins"
      artifactId = "maven-compiler-plugin"
      version = "3.7.0"
      configuration {
        skip = true
        skipMain = true
      }
    }
    {
      groupId = "net.alchim31.maven"
      artifactId = "scala-maven-plugin"
      version = "4.0.2"
      configuration {
        scalaVersion = "${versionScalaRelease}"
      }
      executions = [
        {
          goals = [ "compile", "testCompile" ]
        }
      ]
     }
  ]
}
```

and then in your pom.conf file you have
```
{
  project {
    artifactId = "my-sub-module"
    properties {
      versionScalaRelease = "2.12.8"
    }
    include "scalaCompile.conf"
  }
}
```

This allows for arbitary abstraction of any part of the maven POM without
breaking how reporting aggregration works.  This allows for a nice addition
to Maven's existing abstraction functionality without requiring existing
code to change.


## From Hocon to XML

Translating Hocon to XML is generally a straight forward process.  Most XML
Elements directly translate into ConfigObjects (or JObjects).  Theses two data
structures can be thought of as a Map from String to Any.  So the key of the
map is the label of the child XML node.  So:

```
parent {
  groupId = "bar"
  artifactId = "foo"
}
```
becomes
```
<parent>
  <groupId>bar</groupId>
  <artifactId>foo</artifactId>
</parent>
```

Here we see both map/object and string conversion.  Hocon also supports Numbers,
Doubles, Boolean and Date/Time values.  To convert these to XML, toString
and/or render are used to convert them to strings as POM XML converts everything
to strings.  Maps and primitive types are always converted in this way.  
So ```value = true``` becomes ```<value>true</value>``` and 
```value2 = -14.5``` becomes ```<value2>-14.5</value2>```


## Special Cases

There are a few exceptions for other types however.


### Lists

Lists are handled differently in pom.xml files.  A parent element is labeled 
with a pluralized form of a noun and the child elements are all labeled with 
the same singular form of the noun.  In Hocon this gets compressed to

```
dependencies = [
  { groupId = "foo", artifactId = "bar" }
  { groupId = "foo2", artifactId = "bar2" }
]
```
So this Hocon becomes the following XML
```
<dependencies>
  <dependency>
    <groupId>foo</groupId>
    <artifactId>bar</artifactId>
  </dependency>
  <dependency>
    <groupId>foo2</groupId>
    <artifactId>bar2</artifactId>
  </dependency>
</dependencies>
```

Notice that the word dependency disappears from the Hocon (and Json) form(s).


### Properties

Another difference is how Properties objects are handled in configuration 
elements.  In Plugins and some other POM model components, there are often
a configuration element whose values are bound to fields of the Plugin's Mojo.
When the Mojo binds a Properties object, its XML is expressed in this rather
verbose way:
```
<configuration>
  <myProperties>
    <property>
      <name>propertyName1</name>
      <value>propertyValue1</value>
    <property>
    <property>
      <name>propertyName2</name>
      <value>propertyValue2</value>
    <property>
  </myProperties>
</configuration>
```
In Hocon becomes:
```
configuration {
  myProperties {
    properties = true
    propertyName1 = "propertyValue1"
    propertyName2 = "propertyValue2"
  }
}
```

Note the ```properties = true``` entry that is added.  This way when this
Hocon is converted back to XML, it can be converted into the correct XML for
the Plugin.  Otherwise, this block of Hocon would be converted to
```
<configuration>
  <myProperties>
    <propertyName1>propertyValue1</propertyName1>
    <propertyName2>propertyValue2</propertyName2>
  </myProperties>
</configuration>
```
by following the general rules of translating XML Elements to 
Map of String to Any.  See 
[Configuring Plugins](https://maven.apache.org/guides/mini/guide-configuring-plugins.html)
for more information.


### XML Attributes

Even though most Maven POM XML files don't have configuration elements with 
attributes it does happen.  To handle this case, you must add a special key 
called ```attributeKeys``` to your Hocon or Json object to tell Unbound which 
children to create XML elements for and which ones to create XML attributes
for.  So this Hocon
```
"target" {
  "chmod" { "attributeKeys" = [ "perm" ]
    "perm" = "755"
    "fileset" { "attributeKeys" = [ "dir" ]
      "dir" = "${basedir}/target/deb"
      "include" = { "attributeKeys" = [ "name" ]
        "name" = "usr/bin/mvnu"
      }
    }
  }
}
```
becomes this XML
```
<target>
  <chmod perm="755">
    <fileset dir="${basedir}/target/deb">
      <include name="usr/bin/mvnu"/>
    </fileset>
  </chmod>
</target>
```
which is messy but its such a rare case that it hardly matters.


### Shared Plugin Configuration

There are three other special cases that appear because of shared infrastructure
supplied by Maven and used by Plugins.  Transformer and Archiver functionality
can be used by Plugin authors to provide compression, dependency resolution or
other common functionality provided by Maven itself.  These features are
configured in commons but special ways.


#### Transformers

[Transformers](https://maven.apache.org/plugins-archives/maven-shade-plugin-2.0/examples/resource-transformers.html) 
are used to merge multiple artifacts into a single large artifact.
They are specified in the configuration section of a Plugin.  Here is an example
of a piece of XML that configures a Transformer
```
<transformers>
  <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
    <mainClass>org.apache.maven.unbound.Cli</mainClass>
  </transformer>
</transformers>
```
would become
```                
transformers = [
  {
    implementation = "org.apache.maven.plugins.shade.resource.ManifestResourceTransformer"
    mainClass = "org.apache.maven.unbound.Cli"
  }
]
```
Note the only change in usual behavior is that the implementation entry 
becomes a XML attribute.


#### Archivers

An [Archiver](http://maven.apache.org/shared/maven-archiver/index.html)
is a piece of shared code that compresses a group of files into one artifact
such as a Jar file.  They are configured in the configuration section of
a Plugin and always have the label archiver.  For example the following block
of XML specifies that a manifest file should be created with a certain
main class and the classpath should be added to the manifest.  Also entries
in another file should be added to the manifest of this artifact.  Here is
the XML:
```
<archive>
  <manifest>
    <addClasspath>true</addClasspath>
    <mainClass>com.footballradar.jpademo.App</mainClass>
  </manifest>
  <manifestFile>src/main/resources/Manifest.txt</manifestFile>
</archive>
```
which becomes
```
archive {
  manifestFile = "src/main/resources/Manifest.txt"
  manifest {
    addClasspath = true
    mainClass = "com.footballradar.jpademo.App"
  }
}
```


#### Dependencies

A dependency is a commonly used part of a POM file.  Often Plugins will also
have lists of dependencies in their configuration sections.  When a Hocon object
is composed entirely of keys that match the keys of a dependency object, the
Hocon will be translated into XML in the following way:
```
defineBridge = [
  {
    groupId = "org.scala-sbt",
    artifactId = "compiler-bridge_${version.scala.epoch}",
    version = "${version.scala.zinc}"
  }
]
defineCompiler = [
  {
    groupId = "org.scala-lang",
    artifactId = "scala-compiler",
    version = "${version.scala.release}"
  }
]
```
becomes
```
<defineBridge>
  <dependency>
    <groupId>org.scala-sbt</groupId>
    <artifactId>compiler-bridge_${version.scala.epoch}</artifactId>
    <version>${version.scala.zinc}</version>
  </dependency>
</defineBridge>
<defineCompiler>
  <dependency>
    <groupId>org.scala-lang</groupId>
    <artifactId>scala-compiler</artifactId>
    <version>${version.scala.release}</version>
  </dependency>
</defineCompiler>
```
Notice that the normal pattern is applied except that the XML produced doesn't
try to compute the child label from defineBridge or defineCompiler but instead
uses &quot;dependency&quot; instead.  It does this when all the child labels
(&quot;groupId&quot;, &quot;artifactId&quot; and &quot;version&quot;) are
valid parts of a Maven Dependency object.


## Complete Samples

Here is an example of a simple Hocon POM file that builds a libarary jar.

```
{
  project {
    groupId = "my-group"
    artifactId = "unbound-lib"
    properties {
      versionScalaRelease = "2.12.8"
    }
    # from example in the beginning of this page
    include "scalaCompile.conf"
    dependencies = [
      {
        artifactId = "scala-xml_${version.scala.epoch}"
        groupId = "org.scala-lang.modules"
        version = "1.2.0"
      }
      {
        artifactId = "ficus_${version.scala.epoch}"
        groupId = "com.iheart"
        version = "1.4.6"
      }
      {
        artifactId = "json4s-native_${version.scala.epoch}"
        groupId = "org.json4s"
        version = "3.6.6"
      }
      {
        artifactId = "scalatest_${version.scala.epoch}"
        groupId = "org.scalatest"
        scope = "test"
        version = "${version.scalatest}"
      }
    ]
  }
}
```

Here is an example of a simple Hocon POM file.  That builds scala code and
uses shade to create an executable jar.

```
{
  project {
    artifactId = "unbound"
    properties {
      versionScalaRelease = "2.12.8"
    }
    # from example in the beginning of this page
    include "scalaCompile.conf"
    dependencies = [
      {
        artifactId = "scala-xml_${version.scala.epoch}"
        groupId = "org.scala-lang.modules"
        version = "1.2.0"
      }
      {
        artifactId = "ficus_${version.scala.epoch}"
        groupId = "com.iheart"
        version = "1.4.6"
      }
      {
        artifactId = "json4s-native_${version.scala.epoch}"
        groupId = "org.json4s"
        version = "3.6.6"
      }
      {
        artifactId = "maven-model"
        groupId = "org.apache.maven"
        version = "3.6.1"
      }
      {
        artifactId = "maven-shared-utils"
        groupId = "org.apache.maven.shared"
        version = "3.2.1"
      }
      {
        artifactId = "scalatest_${version.scala.epoch}"
        groupId = "org.scalatest"
        scope = "test"
        version = "${version.scalatest}"
      }
    ]
    build {
      plugins [
        {
          groupId = "org.apache.maven.plugins"
          artifactId = "maven-shade-plugin"
          version = "3.2.1"
          executions = [
            {
              configuration {
                minimizeJar = true
                shadedArtifactAttached = true
                shadedClassifierName = "exec"
                transformers = [
                  {
                    implementation = "org.apache.maven.plugins.shade.resource.ManifestResourceTransformer"
                    mainClass = "org.apache.maven.unbound.Cli"
                  }
                ]
              }
              goals = [ "shade" ]
              phase = "package"
            }
          ]
        }
      ]
    }
  }
}
```
