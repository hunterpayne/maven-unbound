/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.unbound

import java.io.{ File, InputStreamReader }

import org.scalatest.{ FlatSpec, Matchers }

import com.typesafe.config.{ 
  Config, ConfigFactory, ConfigResolveOptions, ConfigParseOptions }

class HoconSpec extends FlatSpec with Matchers {

  behavior of "Hocon"

  import HoconReader._

  it should "load from hocon" in {

    val correct = """<project 
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
"""

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project1 = readPOM(ConfigFactory.load(
      "pom-conf", ConfigParseOptions.defaults(), resolveOpts))
    project1.toXmlString should be (correct)

    val project2 = new Project(scala.xml.XML.loadString(project1.toXmlString))
    project2.makeModelObject() // just to make sure it doesn't NPE
    // required because Ficus creates Vectors which aren't equal to the
    // Lists created by the XML DOM reading constructors
    val removedVectors = 
      project1.toString.replaceAllLiterally("Vector(", "List(")
    project2.toString should be (removedVectors)
  }

  it should "load another pom from hocon" in {
    val correct = """<project 
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.example</groupId>
  <artifactId>jpademo</artifactId>
  <version>1.0</version>
  <packaging>jar</packaging>
  <name>jpademo</name>
  <url>http://maven.apache.org</url>
  <scm>
    <connection>scm:git:ssh://my.git.server.internal/home/git/jpademo</connection>
    <developerConnection>
      scm:git:ssh://my.git.server.internal/home/git/jpademo
    </developerConnection>
  </scm>
  <ciManagement>
    <system>jenkins</system>
    <url>https://my.jenkins.internal/jenkins</url>
  </ciManagement>
  <distributionManagement>
    <repository>
      <releases>
        <enabled>false</enabled>
        <updatePolicy>weekly</updatePolicy>
        <checksumPolicy>strict</checksumPolicy>
      </releases>
      <snapshots>
        <enabled>false</enabled>
        <updatePolicy>hourly</updatePolicy>
        <checksumPolicy>loose</checksumPolicy>
      </snapshots>
      <id>My_Artifactory_Releases</id>
      <name>My_Artifactory-releases</name>
      <url>http://my.maven.repository.internal/artifactory/release</url>
      <layout>ivy</layout>
    </repository>
    <snapshotRepository>
      <id>My_Artifactory_Snapshots</id>
      <name>My_Artifactory-snapshots</name>
      <url>http://my.maven.repository.internal/artifactory/snapshot</url>
    </snapshotRepository>
  </distributionManagement>
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
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
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
  <repositories>
    <repository>
      <id>hibernate-support</id>
      <name>Repository for library Library[hibernate-support]</name>
      <url>http://download.java.net/maven/2/</url>
    </repository>
  </repositories>
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
            <id>jar</id>
            <goals>
              <goal>jar</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <archive>
            <manifest>
              <addClasspath>true</addClasspath>
              <mainClass>com.footballradar.jpademo.App</mainClass>
            </manifest>
            <manifestFile>src/main/resources/Manifest.txt</manifestFile>
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
</project>
"""

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project1 = readPOM(ConfigFactory.load(
      "pom2-conf", ConfigParseOptions.defaults(), resolveOpts))
    project1.toXmlString should be (correct)

    val project2 = new Project(scala.xml.XML.loadString(project1.toXmlString))
    project2.makeModelObject() // just to make sure it doesn't NPE
    // required because Ficus creates Vectors which aren't equal to the
    // Lists created by the XML DOM reading constructors
    val removedVectors = 
      project1.toString.replaceAllLiterally("Vector(", "List(")
    project2.toString should be (removedVectors)

    val is = getClass().getClassLoader.getResourceAsStream("pom2-conf.xml")
    try {
      val project3 = new Project(scala.xml.XML.load(is))
      project3.toString should be (removedVectors)

    } finally {
      is.close()
    }
  }

  it should "load a 3rd pom from hocon" in {
    val correct = """<project 
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.apache.maven.lifecycle.test</groupId>
  <artifactId>mojo-configuration</artifactId>
  <version>1.0</version>
  <packaging>jar</packaging>
  <name>project-with-additional-lifecycle-elements</name>
  <url>http://maven.apache.org</url>
  <developers>
    <developer>
      <id>foo</id>
      <name>Foo Bar</name>
      <email>any@no-spam</email>
      <organization>Apache Foundation</organization>
      <organizationUrl>https://www.apache.org/</organizationUrl>
      <roles>
        <role>developer</role>
      </roles>
      <timezone>America/Los_Angeles</timezone>
    </developer>
  </developers>
  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-clean-plugin</artifactId>
          <version>0.1</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>0.1</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-deploy-plugin</artifactId>
          <version>0.1</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-install-plugin</artifactId>
          <version>0.1</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-jar-plugin</artifactId>
          <version>0.1</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-plugin-plugin</artifactId>
          <version>0.1</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>0.1</version>
        </plugin>
      </plugins>
    </pluginManagement>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.its.plugins</groupId>
        <artifactId>maven-it-plugin</artifactId>
        <version>0.1</version>
        <executions>
          <execution>
            <phase>generate-sources</phase>
            <goals>
              <goal>xpp3-reader</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
"""

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project1 = readPOM(ConfigFactory.load(
      "pom3-conf", ConfigParseOptions.defaults(), resolveOpts))
    project1.toXmlString should be (correct)

    val project2 = new Project(scala.xml.XML.loadString(project1.toXmlString))
    project2.makeModelObject() // just to make sure it doesn't NPE
    // required because Ficus creates Vectors which aren't equal to the
    // Lists created by the XML DOM reading constructors
    val removedVectors = 
      project1.toString.replaceAllLiterally("Vector(", "List(")
    project2.toString should be (removedVectors)
  }

  it should "load a 4th pom from hocon" in {
    val correct = """<project 
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.apache.maven</groupId>
    <artifactId>maven-parent</artifactId>
    <version>11</version>
    <relativePath>../pom/maven/pom.xml</relativePath>
  </parent>
  <groupId>org.apache.maven</groupId>
  <artifactId>maven</artifactId>
  <version>3.0-SNAPSHOT</version>
  <packaging>pom</packaging>
  <name>Apache Maven</name>
  <description>
    Maven is a project development management and
    comprehension tool. Based on the concept of a project object model:
    builds, dependency management, documentation creation, site
    publication, and distribution publication are all controlled from
    the declarative file. Maven can be extended by plugins to utilise a
    number of other development tools for reporting or the build
    process.
  </description>
  <url>http://maven.apache.org/</url>
  <inceptionYear>2001</inceptionYear>
  <organization>
    <name>Apache Foundation</name>
    <url>https://www.apache.org/</url>
  </organization>
  <mailingLists>
    <mailingList>
      <name>Maven Developer List</name>
      <subscribe>dev-subscribe@maven.apache.org</subscribe>
      <unsubscribe>dev-unsubscribe@maven.apache.org</unsubscribe>
      <post>dev@maven.apache.org</post>
      <archive>http://mail-archives.apache.org/mod_mbox/maven-dev</archive>
      <otherArchives>
        <otherArchive>http://www.mail-archive.com/dev@maven.apache.org/</otherArchive>
        <otherArchive>http://www.nabble.com/Maven-Developers-f179.html</otherArchive>
        <otherArchive>http://maven.dev.markmail.org/</otherArchive>
      </otherArchives>
    </mailingList>
    <mailingList>
      <name>Maven User List</name>
      <subscribe>users-subscribe@maven.apache.org</subscribe>
      <unsubscribe>users-unsubscribe@maven.apache.org</unsubscribe>
      <post>users@maven.apache.org</post>
      <archive>http://mail-archives.apache.org/mod_mbox/maven-users</archive>
      <otherArchives>
        <otherArchive>
          http://www.mail-archive.com/users@maven.apache.org/
        </otherArchive>
        <otherArchive>http://www.nabble.com/Maven---Users-f178.html</otherArchive>
        <otherArchive>http://maven.users.markmail.org/</otherArchive>
      </otherArchives>
    </mailingList>
    <mailingList>
      <name>Maven Issues List</name>
      <subscribe>issues-subscribe@maven.apache.org</subscribe>
      <unsubscribe>issues-unsubscribe@maven.apache.org</unsubscribe>
      <archive>http://mail-archives.apache.org/mod_mbox/maven-issues/</archive>
      <otherArchives>
        <otherArchive>
          http://www.mail-archive.com/issues@maven.apache.org
        </otherArchive>
        <otherArchive>http://www.nabble.com/Maven---Issues-f15573.html</otherArchive>
        <otherArchive>http://maven.issues.markmail.org/</otherArchive>
      </otherArchives>
    </mailingList>
    <mailingList>
      <name>Maven Commits List</name>
      <subscribe>commits-subscribe@maven.apache.org</subscribe>
      <unsubscribe>commits-unsubscribe@maven.apache.org</unsubscribe>
      <archive>http://mail-archives.apache.org/mod_mbox/maven-commits</archive>
      <otherArchives>
        <otherArchive>
          http://www.mail-archive.com/commits@maven.apache.org
        </otherArchive>
        <otherArchive>http://www.nabble.com/Maven---Commits-f15575.html</otherArchive>
        <otherArchive>http://maven.commits.markmail.org/</otherArchive>
      </otherArchives>
    </mailingList>
    <mailingList>
      <name>Maven Announcements List</name>
      <subscribe>announce-subscribe@maven.apache.org</subscribe>
      <unsubscribe>announce-unsubscribe@maven.apache.org</unsubscribe>
      <post>announce@maven.apache.org</post>
      <archive>http://mail-archives.apache.org/mod_mbox/maven-announce/</archive>
      <otherArchives>
        <otherArchive>
          http://www.mail-archive.com/announce@maven.apache.org
        </otherArchive>
        <otherArchive>
          http://www.nabble.com/Maven-Announcements-f15617.html
        </otherArchive>
        <otherArchive>http://maven.announce.markmail.org/</otherArchive>
      </otherArchives>
    </mailingList>
    <mailingList>
      <name>Maven Notifications List</name>
      <subscribe>notifications-subscribe@maven.apache.org</subscribe>
      <unsubscribe>notifications-unsubscribe@maven.apache.org</unsubscribe>
      <archive>
        http://mail-archives.apache.org/mod_mbox/maven-notifications/
      </archive>
      <otherArchives>
        <otherArchive>
          http://www.mail-archive.com/notifications@maven.apache.org
        </otherArchive>
        <otherArchive>
          http://www.nabble.com/Maven---Notifications-f15574.html
        </otherArchive>
        <otherArchive>http://maven.notifications.markmail.org/</otherArchive>
      </otherArchives>
    </mailingList>
  </mailingLists>
  <modules>
    <module>maven-core</module>
    <module>apache-maven</module>
    <module>maven-model</module>
    <module>maven-plugin-api</module>
    <module>maven-project</module>
    <module>maven-reporting-api</module>
    <module>maven-project-builder</module>
    <module>maven-mercury</module>
    <module>maven-embedder</module>
    <module>maven-toolchain</module>
    <module>maven-compat</module>
    <module>maven-repository</module>
    <module>maven-repository-mercury</module>
  </modules>
  <scm>
    <connection>
      scm:svn:http://svn.apache.org/repos/asf/maven/components/trunk
    </connection>
    <developerConnection>
      scm:svn:https://svn.apache.org/repos/asf/maven/components/trunk
    </developerConnection>
    <url>http://svn.apache.org/viewcvs.cgi/maven/components/trunk</url>
  </scm>
  <issueManagement>
    <system>jira</system>
    <url>http://jira.codehaus.org/browse/MNG</url>
  </issueManagement>
  <distributionManagement>
    <site>
      <id>apache.website</id>
      <url>scp://people.apache.org/www/maven.apache.org/ref/${project.version}/</url>
    </site>
  </distributionManagement>
  <properties>
    <securityDispatcherVersion>1.2</securityDispatcherVersion>
    <commonsCliVersion>1.0</commonsCliVersion>
    <plexusInteractivityVersion>1.0-alpha-6</plexusInteractivityVersion>
    <plexusUtilsVersion>1.5.8</plexusUtilsVersion>
    <modelloVersion>1.0.1-SNAPSHOT</modelloVersion>
    <easyMockVersion>1.2_Java1.3</easyMockVersion>
    <modelBuilderVersion>1.7-SNAPSHOT</modelBuilderVersion>
    <plexusJetty6Version>1.6</plexusJetty6Version>
    <plexusWebdavVersion>1.0</plexusWebdavVersion>
    <jxpathVersion>1.3</jxpathVersion>
    <junitVersion>3.8.1</junitVersion>
    <plexusPluginManagerVersion>1.0-alpha-1</plexusPluginManagerVersion>
    <plexusInterpolationVersion>1.1</plexusInterpolationVersion>
    <mercuryMp3Version>1.0-alpha-1</mercuryMp3Version>
    <mercuryVersion>1.0-alpha-6-SNAPSHOT</mercuryVersion>
    <wagonVersion>1.0-beta-4</wagonVersion>
    <classWorldsVersion>1.3</classWorldsVersion>
    <woodstoxVersion>3.2.6</woodstoxVersion>
    <doxiaVersion>1.0-alpha-9</doxiaVersion>
    <plexusVersion>1.0-beta-3.0.7</plexusVersion>
  </properties>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-mercury</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-lifecycle</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-reporting-api</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-profile</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-model</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-project</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-plugin-api</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-toolchain</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-embedder</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-core</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-project-builder</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-repository</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven</groupId>
        <artifactId>maven-compat</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.plexus</groupId>
        <artifactId>plexus-utils</artifactId>
        <version>${plexusUtilsVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.plexus</groupId>
        <artifactId>plexus-container-default</artifactId>
        <version>${plexusVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.plexus</groupId>
        <artifactId>plexus-component-annotations</artifactId>
        <version>${plexusVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.plexus</groupId>
        <artifactId>plexus-classworlds</artifactId>
        <version>${classWorldsVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.plexus</groupId>
        <artifactId>plexus-interpolation</artifactId>
        <version>${plexusInterpolationVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.plexus</groupId>
        <artifactId>plexus-interactivity-api</artifactId>
        <version>${plexusInteractivityVersion}</version>
        <exclusions>
          <exclusion>
            <groupId>org.codehaus.plexus</groupId>
            <artifactId>plexus-component-api</artifactId>
          </exclusion>
        </exclusions>
      </dependency>
      <dependency>
        <groupId>org.sonatype.plexus</groupId>
        <artifactId>plexus-jetty6</artifactId>
        <version>${plexusJetty6Version}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.sonatype.spice</groupId>
        <artifactId>plexus-webdav</artifactId>
        <version>${plexusWebdavVersion}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.wagon</groupId>
        <artifactId>wagon-provider-api</artifactId>
        <version>${wagonVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.wagon</groupId>
        <artifactId>wagon-file</artifactId>
        <version>${wagonVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.wagon</groupId>
        <artifactId>wagon-http-lightweight</artifactId>
        <version>${wagonVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.wagon</groupId>
        <artifactId>wagon-ssh</artifactId>
        <version>${wagonVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.wagon</groupId>
        <artifactId>wagon-ssh-external</artifactId>
        <version>${wagonVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.doxia</groupId>
        <artifactId>doxia-sink-api</artifactId>
        <version>${doxiaVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.sonatype.spice</groupId>
        <artifactId>model-builder</artifactId>
        <version>${modelBuilderVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.woodstox</groupId>
        <artifactId>wstx-asl</artifactId>
        <version>${woodstoxVersion}</version>
      </dependency>
      <dependency>
        <groupId>commons-cli</groupId>
        <artifactId>commons-cli</artifactId>
        <version>${commonsCliVersion}</version>
        <exclusions>
          <exclusion>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
          </exclusion>
          <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
          </exclusion>
        </exclusions>
      </dependency>
      <dependency>
        <groupId>commons-jxpath</groupId>
        <artifactId>commons-jxpath</artifactId>
        <version>${jxpathVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-artifact</artifactId>
        <version>${mercuryVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-external</artifactId>
        <version>${mercuryVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-plexus</artifactId>
        <version>${mercuryVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-repo-virtual</artifactId>
        <version>${mercuryVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.sonatype.mercury</groupId>
        <artifactId>mercury-mp3-cli</artifactId>
        <version>${mercuryMp3Version}</version>
      </dependency>
      <dependency>
        <groupId>org.sonatype.plexus</groupId>
        <artifactId>plexus-sec-dispatcher</artifactId>
        <version>${securityDispatcherVersion}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-repo-local-m2</artifactId>
        <version>${mercuryVersion}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-repo-remote-m2</artifactId>
        <version>${mercuryVersion}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-md-sat</artifactId>
        <version>${mercuryVersion}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-util</artifactId>
        <version>${mercuryVersion}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-transport-http</artifactId>
        <version>${mercuryVersion}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.apache.maven.mercury</groupId>
        <artifactId>mercury-transport-http</artifactId>
        <version>${mercuryVersion}</version>
        <type>test-jar</type>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>org.sonatype.plexus</groupId>
        <artifactId>plexus-plugin-manager</artifactId>
        <version>${plexusPluginManagerVersion}</version>
      </dependency>
      <dependency>
        <groupId>easymock</groupId>
        <artifactId>easymock</artifactId>
        <version>${easyMockVersion}</version>
        <scope>test</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>${junitVersion}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <extensions>
      <extension>
        <groupId>org.apache.maven.wagon</groupId>
        <artifactId>wagon-ftp</artifactId>
        <version>2.10</version>
      </extension>
    </extensions>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.codehaus.plexus</groupId>
          <artifactId>plexus-component-metadata</artifactId>
          <version>${plexusVersion}</version>
          <executions>
            <execution>
              <goals>
                <goal>generate-metadata</goal>
                <goal>generate-test-metadata</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>2.0.2</version>
          <configuration>
            <source>1.5</source>
            <target>1.5</target>
          </configuration>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-release-plugin</artifactId>
          <configuration>
            <tagBase>https://svn.apache.org/repos/asf/maven/components/tags</tagBase>
          </configuration>
        </plugin>
        <plugin>
          <groupId>org.codehaus.modello</groupId>
          <artifactId>modello-maven-plugin</artifactId>
          <version>${modelloVersion}</version>
          <executions>
            <execution>
              <id>site-docs</id>
              <phase>pre-site</phase>
              <goals>
                <goal>xdoc</goal>
                <goal>xsd</goal>
              </goals>
            </execution>
            <execution>
              <id>standard</id>
              <goals>
                <goal>java</goal>
                <goal>xpp3-reader</goal>
                <goal>xpp3-writer</goal>
              </goals>
            </execution>
          </executions>
          <configuration>
            <useJava5>true</useJava5>
          </configuration>
        </plugin>
        <plugin>
          <groupId>org.apache.felix</groupId>
          <artifactId>maven-bundle-plugin</artifactId>
          <version>1.0.0</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>2.4.2</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-assembly-plugin</artifactId>
          <version>2.2-beta-2</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-resources-plugin</artifactId>
          <version>2.4-SNAPSHOT</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
  <profiles>
    <profile>
      <id>osgi</id>
      <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>1.8</jdk>
      </activation>
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.felix</groupId>
            <artifactId>maven-bundle-plugin</artifactId>
            <executions>
              <execution>
                <goals>
                  <goal>manifest</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>2.1</version>
            <configuration>
              <archive>
                <manifestFile>
                  ${project.build.outputDirectory}/META-INF/MANIFEST.MF
                </manifestFile>
              </archive>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
    <profile>
      <id>release</id>
      <activation>
        <os>
          <name>Windows XP</name>
          <family>Windows</family>
          <arch>x86</arch>
          <version>5.1.2600</version>
        </os>
      </activation>
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <executions>
              <execution>
                <id>make-assembly</id>
                <phase>package</phase>
                <goals>
                  <goal>single</goal>
                </goals>
              </execution>
            </executions>
            <inherited>false</inherited>
            <configuration>
              <finalName>maven-${project.version}-src</finalName>
              <tarLongFileMode>gnu</tarLongFileMode>
              <descriptors>
                <descriptor>src/main/assembly/src.xml</descriptor>
              </descriptors>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
    <profile>
      <id>strict</id>
      <activation>
        <property>
          <name>debug</name>
        </property>
      </activation>
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-enforcer-plugin</artifactId>
            <version>1.0-alpha-3</version>
            <executions>
              <execution>
                <id>enforce-jdk-15</id>
                <goals>
                  <goal>enforce</goal>
                </goals>
                <configuration>
                  <rules>
                    <requireJavaVersion>
                      <version>1.5</version>
                    </requireJavaVersion>
                  </rules>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>
</project>
"""

    val resolveOpts = ConfigResolveOptions.defaults().setAllowUnresolved(true)
    val project1 = readPOM(ConfigFactory.load(
      "pom4-conf", ConfigParseOptions.defaults(), resolveOpts))
    project1.makeModelObject() // just to make sure it doesn't NPE
    /*
    project1.toXmlString should be (correct)

    val project2 = new Project(scala.xml.XML.loadString(project1.toXmlString))
    // required because Ficus creates Vectors which aren't equal to the
    // Lists created by the XML DOM reading constructors
    val removedVectors = 
      project1.toString.replaceAllLiterally("Vector(", "List(")
    project2.toString should be (removedVectors)
     */
  }
}

