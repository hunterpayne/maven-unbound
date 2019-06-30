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
