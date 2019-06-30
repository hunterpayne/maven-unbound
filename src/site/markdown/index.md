Maven Unbound

Welcome to Maven Unbound.  Unbound is a project with a simple purpose, to allow
Maven POMs to be expressed in Hocon or Json form.  The project provides a 
command line tool (CLI) that converts pom.xml files to Hocon or Json and back 
again.  

To convert a project from XML pom files to Hocon:
```mvnu --generate-hocon```

To convert a project from XML pom files to Json:
```mvnu --generate-json```

Then use mvnu just like you would use mvn.  When you change your POM file, just
edit your pom.conf or pom.json file just as you normally would edit your pom.xml

Unbound will look for pom.json or pom.conf files and generate
pom.xml files in the current directory and in any sub-modules of the 
current Maven Project.  Then it will call mvn with the same arguments as Unbound
was given.  This has the effect of making it seem like you are using
Json or Hocon style Maven when you are really just generating the pom.xml files
on the fly and calling normal Maven.
