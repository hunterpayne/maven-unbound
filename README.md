# Maven Unbound

Home page: [Maven Unbound](https://hunterpayne.github.io/maven-unbound-site/)

Hocon and Json to Apache Maven pom.xml conversions

## How to install the CLI

To use it, pull this repo and build it with ```mvn clean install```.  This
will build a Deb installed at target/unbound_1.0.0-1_all.deb and a RPM
installer at target/rpm/unbound/RPMS/noarch/unbound-1.0.0-1.noarch.rpm
Install the appropiate one for your distribution of Linux.

For Windows the installer is generated at target/unbound-1.0.0.exe

For OSX use the RPM installer.  You might have to install rpm itself via brew.

## To use the Unbound command line interface

To convert a project from XML pom files to Hocon:
```mvnu --generate-hocon```

To convert a project from XML pom files to Json:
```mvnu --generate-json```

Then use mvnu just like you would use mvn.

The CLI is just a short script called mvnu which takes all the same
arguments as mvn does with two additions.

If instead of the normal maven arguments you provide either of:
* --generate-json then pom.json files will be generated for all the found 
pom.xml files.
* --generate-hocon then pom.conf files will be generated in the Hocon format 
for all the found pom.xml files.

Otherwise, the CLI will look for pom.json or pom.conf files and generate
pom.xml files in the current directory and in any sub-modules of the 
current Maven Project.  Then it will call mvn with the same arguments as Unbound
was given.  This has the effect of making it seem like you are using
Json or Hocon style Maven when you are really just generating the pom.xml files
on the fly and calling normal Maven.


## To use Unbound as a library

Unbound provides a set of Scala case classes mirroring the Maven POM bean 
classes.  These case classes are capable of making the Maven POM bean classes.
This allows us to use conversion utilities like Ficus to automate some of the 
serialization code.  The root case class, Project, can be written and read
to and from Xml and Json and read from Hocon.

#### Read XML POM files from a String into a Project
```
  import org.apache.maven.unbound.Project
  import scala.xml.XML

  val root = XML.loadString(xmlStr) // parse the string into a DOM

  // convert the DOM to a Project case class
  val project: Project = new Project(root) 
```

#### Write a Project to a string as XML
```
  val xmlStr: String = project.toXmlString
```

#### Read a Json POM file from a String into a Project
```
  import org.apache.maven.unbound.Project
  import org.apache.maven.unbound.JsonReader.readPOM
  val project: Project = readPOM(jsonStr)
```

#### Write a Project to a string as Json
```
  import org.apache.maven.unbound.JsonWriter.writePOM
  val jsonStr: String = writePOM(project)
```

#### Convert a Hocon Config to a Project
```
  import org.apache.maven.unbound.Project
  import org.apache.maven.unbound.HoconReader.readPOM

  val conf: Config = ... // load Config using the Typesafe ConfigFactory
  val project: Project = readPOM(conf)
```


## Is it ready to use?

All functionality is finished and works.  This is beta code and ready to 
try.  Comments are only moved from XML to Hocon as that's the use case
that makes sense.  When Hocon is translated back to XML, that's for a computer
to consume.  And Json doesn't support comments so comments are stripped when
using Json POMs.

We have FindBugs, Scoverage, Scalastyle, and CVE reports reporting.  
The Unbound web site now can be generated via ```mvn site```

Things left to do before its 1.0 ready:
* better docs and use cases for site

Watch this space for more news.
