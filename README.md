# Maven Unbound
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

The main functionality is finished and works.  This is beta code and ready to 
try.

We have FindBugs, Scoverage, and Scalastyle reporting in place now.  In addition
to Security CVE reports.  A basic maven site now can be generated via
```mvn site```

Because we don't have code that directly translates Project to Config, we 
can't support transforming XML comments.  This is because to generate HOCON, 
we first generate JSON and load it into a Config which is then rendered 
into HOCON.  JSON has no support for comments.  If in the future we create
a way to directly translate Project into Config, we will add support for
translating XML comments.

Things left to do before its 1.0 ready:
* test Windows installer

Nice to have: translating comments from XML to Hocon

Watch this space for more news.
