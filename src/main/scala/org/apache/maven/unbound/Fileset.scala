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

import java.io.{ ObjectInputStream, ObjectOutputStream }

import scala.xml.Elem

import com.typesafe.config.ConfigFactory
import org.json4s._

protected[unbound] object Fileset extends CommonJsonReader {

  implicit val formats = JsonReader.formats

  private def writeObject(stream: ObjectOutputStream): Unit =
    stream.defaultWriteObject()

  private def readObject(stream: ObjectInputStream): Unit =
    stream.defaultReadObject()

  class FilesetSerializer extends CustomSerializer[Fileset](format => (
    {
      case obj @ JObject(fields) =>
        new Fileset(
          readStr(fields, DirectoryStr).getOrElse(null),
          readStr(fields, "lineEnding").getOrElse(null),
          readBool(fields, "followSymlinks").getOrElse(false),
          readStr(fields, "outputDirectory").getOrElse(null),
          readBool(fields, "useDefaultExcludes").getOrElse(true),
          readStringSequence(fields, "includes"),
          readStringSequence(fields, "excludes"),
          readStr(fields, "modeFile").getOrElse("0644"),
          readStr(fields, "modeDirectory").getOrElse("0755"),
          readObject[Mapper](obj, "mapper")
        )
    },
    {
      case f: Fileset =>
        JObject(Seq[Option[JField]](
          writeStr(DirectoryStr, f.directory),
          writeStr("lineEnding", f.lineEnding),
          writeBool("followSymlinks", f.followSymlinks, false),
          writeStr("outputDirectory", f.outputDirectory),
          writeBool("useDefaultExcludes", f.useDefaultExcludes, true),
          writeStringSequence(Includes, f.includes),
          writeStringSequence(Excludes, f.excludes),
          writeStr("fileMode", f.fileMode),
          writeStr("directoryMode", f.directoryMode),
          writeObject("mapper", f.mapper)
        ).flatten.toList)
    }
  ))
}

/**
  * @author Hunter Payne
  */
case class Fileset(
  directory: String = null, lineEnding: String = null,
  followSymlinks: Boolean = false,
  outputDirectory: String = null, useDefaultExcludes: Boolean = true,
  includes: Seq[String] = Seq[String](), excludes: Seq[String] = Seq[String](),
  fileMode: String = "0644", directoryMode: String = "0755",
  mapper: Mapper = null
) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.DirectoryStr).text),
    emptyToNull((elem \ "lineEnding").text),
    emptyToDefaultBool((elem \ "followSymlinks").text, false),
    emptyToNull((elem \ "outputDirectory").text),
    emptyToDefaultBool((elem \ "useDefaultExcludes").text, true),
    (elem \ SL.Includes).map { _.text },
    (elem \ SL.Excludes).map { _.text },
    emptyToDefault((elem \ "fileMode").text, "0644"),
    emptyToDefault((elem \ "directoryMode").text, "0755"),
    (elem \ "mapper").map { case e: Elem =>
      new Mapper(e) }.headOption.getOrElse(null)
  )

  lazy val xml =
    <fileset>
      { if (directory != null) <directory>{directory}</directory> }
      { if (lineEnding != null) <lineEnding>{lineEnding}</lineEnding> }
      { if (followSymlinks) <followSymlinks>true</followSymlinks> }
      { if (outputDirectory != null)
        <outputDirectory>{outputDirectory}</outputDirectory> }
      { if (!useDefaultExcludes)
        <useDefaultExcludes>false</useDefaultExcludes> }
      { if (!includes.isEmpty) <includes>
        { includes.map { Include(_).xml } }
        </includes> }
      { if (!excludes.isEmpty) <excludes>
        { excludes.map { Exclude(_).xml } }
        </excludes> }
      { if (fileMode != null && fileMode != "0644")
        <fileMode>{fileMode}</fileMode> }
      { if (directoryMode != null && directoryMode != "0755")
        <directoryMode>{directoryMode}</directoryMode> }
      { if (mapper != null) mapper.xml }
    </fileset>
}

case class Mapper(
  `type`: String = "identity", from: String = null,
  to: String = null, classname: String = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.TypeStr).text, "identity"),
    emptyToNull((elem \ "from").text),
    emptyToNull((elem \ "to").text),
    emptyToNull((elem \ "classname").text)
  )

  lazy val xml =
    <mapper>
      { if (`type` != null) <type>{`type`}</type> }
      { if (from != null) <from>{from}</from> }
      { if (to != null) <to>{to}</to> }
      { if (classname != null) <classname>{classname}</classname> }
    </mapper>
}
