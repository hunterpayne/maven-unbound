
package org.apache.maven.unbound

import java.io.File

import scala.io.Source

import org.scalatest.{ FlatSpec, Matchers }

class JsonSpec extends FlatSpec with Matchers {

  behavior of "Json"

  import JsonReader._

  it should "load from json" in {

    val correct = """<project 
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd" child.project.url.inherit.append.path="true" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>parent-group</groupId>
    <artifactId>parent-pom</artifactId>
  </parent>
  <groupId>org.clifford</groupId>
  <artifactId>clifford</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>
  <name>clifford</name>
  <modules>
    <module>cross/2.11</module>
    <module>cross/2.12</module>
  </modules>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <version.scala.release>2.11.12</version.scala.release>
    <version.scala.plugin.macro>2.1.1</version.scala.plugin.macro>
    <release.stamp>${maven.build.timestamp}</release.stamp>
    <maven.compiler.source>${version.java}</maven.compiler.source>
    <maven.compiler.target>${version.java}</maven.compiler.target>
    <version.scala.zinc>1.3.0-M2</version.scala.zinc>
    <version.scalajs.library>
      sjs${version.scalajs.epoch}_${version.scala.epoch}
    </version.scalajs.library>
    <notavailable.scalajs>true</notavailable.scalajs>
    <version.scalatest>3.0.6</version.scalatest>
    <release.epoch>1.0.0</release.epoch>
    <version.junit4>4.12</version.junit4>
    <version.scalajs.epoch>0.6</version.scalajs.epoch>
    <version.scala.plugin.native>
      ${version.scalanat.release}
    </version.scala.plugin.native>
    <version.maven.testing>2.22.1</version.maven.testing>
    <version.java>1.8</version.java>
    <revision>${release.epoch}</revision>
    <version.scalanat.epoch>0.3</version.scalanat.epoch>
    <version.scalanat.release>0.3.8</version.scalanat.release>
    <maven.build.timestamp.format></maven.build.timestamp.format>
    <version.scala.simple.epoch>2.11</version.scala.simple.epoch>
    <version.scala.epoch>2.11</version.scala.epoch>
    <version.scalajs.release>0.6.26</version.scalajs.release>
    <version.scalanat.library>
      native${version.scalanat.epoch}_${version.scala.epoch}
    </version.scalanat.library>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.scala-lang</groupId>
      <artifactId>scala-library</artifactId>
      <version>${version.scala.release}</version>
    </dependency>
    <dependency>
      <groupId>org.scalanlp</groupId>
      <artifactId>breeze_${version.scala.epoch}</artifactId>
      <version>0.13.2</version>
    </dependency>
    <dependency>
      <groupId>com.chuusai</groupId>
      <artifactId>shapeless_${version.scala.epoch}</artifactId>
      <version>2.3.3</version>
    </dependency>
    <dependency>
      <groupId>org.scalatest</groupId>
      <artifactId>scalatest_${version.scala.epoch}</artifactId>
      <version>${version.scalatest}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.scalacheck</groupId>
      <artifactId>scalacheck_${version.scala.epoch}</artifactId>
      <version>1.14.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.7.0</version>
        <configuration>
          <skip>true</skip>
          <skipMain>true</skipMain>
        </configuration>
      </plugin>
      <plugin>
        <groupId>com.carrotgarden.maven</groupId>
        <artifactId>scalor-maven-plugin_2.12</artifactId>
        <executions>
          <execution>
            <id>scala-build</id>
            <goals>
              <goal>register-main</goal>
              <goal>register-test</goal>
              <goal>compile-main</goal>
              <goal>compile-test</goal>
              <goal>scala-js-link-main</goal>
              <goal>scala-js-link-test</goal>
              <goal>scala-js-env-prov-webjars</goal>
              <goal>scala-js-env-prov-nodejs</goal>
              <goal>scala-js-env-conf-nodejs</goal>
            </goals>
            <inherited>false</inherited>
          </execution>
        </executions>
        <configuration>
          <zincOptionsScala>
            -Xsource:${version.scala.epoch} -withVersionClasspathValidator:false -compileorder:Mixed -language:implicitConversions -usejavacp
          </zincOptionsScala>
          <defineBridge>
            <dependency>
              <version>${version.scala.zinc}</version>
              <artifactId>compiler-bridge_${version.scala.epoch}</artifactId>
              <groupId>org.scala-sbt</groupId>
            </dependency>
          </defineBridge>
          <defineCompiler>
            <dependency>
              <groupId>org.scala-lang</groupId>
              <artifactId>scala-compiler</artifactId>
              <version>${version.scala.release}</version>
            </dependency>
          </defineCompiler>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.scalatest</groupId>
        <artifactId>scalatest-maven-plugin</artifactId>
        <version>1.0</version>
        <executions>
          <execution>
            <id>test</id>
            <goals>
              <goal>test</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <parallel>false</parallel>
          <reportsDirectory>
            ${project.build.directory}/jvm-surefire-reports
          </reportsDirectory>
          <filereports>Clifford-TestSuite.txt</filereports>
          <junitxml>.</junitxml>
        </configuration>
      </plugin>
    </plugins>
  </build>
  <reporting>
    <plugins>
      <plugin>
        <groupId>net.alchim31.maven</groupId>
        <artifactId>scala-maven-plugin</artifactId>
        <version>3.4.4</version>
        <configuration>
          <jvmArgs>
            <jvmArg>-Xms64m</jvmArg>
            <jvmArg>-Xmx1024m</jvmArg>
          </jvmArgs>
        </configuration>
      </plugin>
    </plugins>
  </reporting>
</project>
"""
    val is = getClass().getClassLoader.getResourceAsStream("pom-json.json")
    try {
      val jsonStr = Source.fromInputStream(is, "UTF-8").getLines.mkString
      val project1 = readPOM(jsonStr)
      val xmlStr = project1.toXmlString

      xmlStr should be (correct)

      val project2 = new Project(scala.xml.XML.loadString(xmlStr))
      println(project2.toXmlString)
      // TODO need to make the order of the properties and configurations
      // deterministic to make this test work
      //project2.toXmlString should be (correct)

    } finally {
      is.close()
    }
  }

  /*
  it should "load another pom from json" in {
    val correct = """<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>jpademo</artifactId>
  <version>1.0</version>
  <packaging>jar</packaging>

  <scm>
      <connection>scm:git:ssh://my.git.server.internal/home/git/jpademo</connection>
      <developerConnection>scm:git:ssh://my.git.server.internal/home/git/jpademo</developerConnection>
  </scm>
  <ciManagement>
      <system>jenkins</system>
      <url>https://my.jenkins.internal/jenkins</url>
  </ciManagement>
  
  
  <name>jpademo</name>
  <url>http://maven.apache.org</url>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>2.3.2</version>
                <configuration>
                    <source>1.6</source>
                    <target>1.6</target>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>2.2</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                        <id>jar</id>
                    </execution>
                </executions>
                <configuration>
                      <archive>
                        <manifestFile>src/main/resources/Manifest.txt</manifestFile>
                        <manifest>
                          <addClasspath>true</addClasspath>
                         
                          <mainClass>com.footballradar.jpademo.App</mainClass>
                          
                        </manifest>
                      </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>1.4</version>
                    <executions>
                        <execution>
                                <phase>package</phase>
                                <goals>
                                        <goal>shade</goal>
                                </goals>
                        </execution>
                    </executions>
                    <configuration>
                            <finalName>${project.artifactId}-${project.version}</finalName>
                    </configuration>
            </plugin>
            
        </plugins>

    </build>

    <repositories>
    <repository>
      <url>http://download.java.net/maven/2/</url>
      <id>hibernate-support</id>
      <layout>default</layout>
      <name>Repository for library Library[hibernate-support]</name>
    </repository>
  </repositories>
  

  
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.hibernate</groupId>
      <artifactId>hibernate-core</artifactId>
      <version>3.6.3.Final</version>
    </dependency>
    <dependency>
      <groupId>org.hibernate</groupId>
      <artifactId>hibernate</artifactId>
      <version>3.2.5.ga</version>
    </dependency>
    <dependency>
      <groupId>org.hibernate</groupId>
      <artifactId>hibernate-entitymanager</artifactId>
      <version>3.3.2.GA</version>
    </dependency>
    <dependency>
      <groupId>javax.sql</groupId>
      <artifactId>jdbc-stdext</artifactId>
      <version>2.0</version>
    </dependency>
    <dependency>
      <groupId>javax.transaction</groupId>
      <artifactId>jta</artifactId>
      <version>1.0.1B</version>
    </dependency>
      <dependency>
          <groupId>org.hibernate</groupId>
          <artifactId>ejb3-persistence</artifactId>
          <version>1.0.1.GA</version>
      </dependency>
      <dependency>
          <groupId>mysql</groupId>
          <artifactId>mysql-connector-java</artifactId>
          <version>5.1.14</version>
      </dependency>
      <dependency>
          <artifactId>slf4j-api</artifactId>
          <groupId>org.slf4j</groupId>
          <type>jar</type>
          <version>1.6.1</version>
      </dependency>
      <dependency>
          <groupId>org.slf4j</groupId>
          <artifactId>log4j-over-slf4j</artifactId>
          <version>1.6.1</version>
      </dependency>
      <dependency>
          <groupId>org.slf4j</groupId>
          <artifactId>slf4j-simple</artifactId>
          <version>1.6.1</version>
      </dependency>
  </dependencies>
  
 <distributionManagement>
    <repository>
        <id>My_Artifactory_Releases</id>
        <name>My_Artifactory-releases</name>
        <url>http://my.maven.repository.internal/artifactory/release</url>
    </repository>
    
    <snapshotRepository>
        <id>My_Artifactory_Snapshots</id>
        <name>My_Artifactory-snapshots</name>
        <url>http://my.maven.repository.internal/artifactory/snapshot</url>
    </snapshotRepository>

</distributionManagement>

</project>"""
   }
   */

}
