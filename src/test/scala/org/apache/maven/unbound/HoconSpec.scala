
package org.apache.maven.unbound

import java.io.{ File, InputStreamReader }

import org.scalatest.{ FlatSpec, Matchers }

import com.typesafe.config.{ 
  Config, ConfigFactory, ConfigResolveOptions, ConfigParseOptions }

class HoconSpec extends FlatSpec with Matchers {

  behavior of "Hocon"

  import HoconReader._

  it should "load from hocon" in {

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project1 = readPOM(ConfigFactory.load(
      "pom-conf", ConfigParseOptions.defaults(), resolveOpts))

    project1.toXmlString should be ("""<project 
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0">
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
""")

    val project2 = new Project(scala.xml.XML.loadString(project1.toXmlString))
    //println(project2)

    println(project2.toXmlString)
    // doesn't work because Ficus creates Vectors which aren't equal to the
    // Lists created by the XML DOM reading constructors
    //project1 should be(project2)
  }
}

