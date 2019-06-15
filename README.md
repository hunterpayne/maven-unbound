# Maven Unbound
Hocon and Json to Apache Maven pom.xml conversions

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

## To use the Unbound command line interface

The CLI is just a quick script called mvnu which takes all the same
arguments as mvn does with two additions.

If instead of the normal maven arguments you provided either of:
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


## Is it ready to use

More unit tests are necessary but the basic functionality is finished and 
works.  Perhaps there are a few places where defaults are not handled 
correctly but mostly the code is working now.  Exotic configuration elements
could also have issues.  Support for translating comments is coming.
Watch this space for more news.
