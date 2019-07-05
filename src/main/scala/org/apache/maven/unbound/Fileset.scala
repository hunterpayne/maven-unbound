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
          readStr(fields, LineEnding).getOrElse(null),
          readBool(fields, FollowSymlinks).getOrElse(false),
          readStr(fields, OutputDirectory).getOrElse(null),
          readBool(fields, UseDefaultExcludes).getOrElse(true),
          readStringSequence(fields, Includes),
          readStringSequence(fields, Excludes),
          readStr(fields, FileMode).getOrElse(DefaultFileMode),
          readStr(fields, DirectoryMode).getOrElse(DefaultDirectoryMode),
          readObject[Mapper](obj, Mapper)
        )
    },
    {
      case f: Fileset =>
        JObject(Seq[Option[JField]](
          writeStr(DirectoryStr, f.directory),
          writeStr(LineEnding, f.lineEnding),
          writeBool(FollowSymlinks, f.followSymlinks, false),
          writeStr(OutputDirectory, f.outputDirectory),
          writeBool(UseDefaultExcludes, f.useDefaultExcludes, true),
          writeStringSequence(Includes, f.includes),
          writeStringSequence(Excludes, f.excludes),
          writeStr(FileMode, f.fileMode),
          writeStr(DirectoryMode, f.directoryMode),
          writeObject(Mapper, f.mapper)
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
  fileMode: String = SL.DefaultFileMode,
  directoryMode: String = SL.DefaultDirectoryMode,
  mapper: Mapper = null
) {

  def this(elem: Elem) = this(
    emptyToNull((elem \ SL.DirectoryStr).text),
    emptyToNull((elem \ SL.LineEnding).text),
    emptyToDefaultBool((elem \ SL.FollowSymlinks).text, false),
    emptyToNull((elem \ SL.OutputDirectory).text),
    emptyToDefaultBool((elem \ SL.UseDefaultExcludes).text, true),
    (elem \ SL.Includes).map { _.text },
    (elem \ SL.Excludes).map { _.text },
    emptyToDefault((elem \ SL.FileMode).text, SL.DefaultFileMode),
    emptyToDefault((elem \ SL.DirectoryMode).text, SL.DefaultDirectoryMode),
    (elem \ SL.Mapper).map { case e: Elem =>
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
      { if (fileMode != null && fileMode != SL.DefaultFileMode.toString)
        <fileMode>{fileMode}</fileMode> }
      { if (directoryMode != null &&
        directoryMode != SL.DefaultDirectoryMode.toString)
        <directoryMode>{directoryMode}</directoryMode> }
      { if (mapper != null) mapper.xml }
    </fileset>
}

case class Mapper(
  `type`: String = SL.Identity, from: String = null,
  to: String = null, classname: String = null) {

  def this(elem: Elem) = this(
    emptyToDefault((elem \ SL.TypeStr).text, SL.Identity),
    emptyToNull((elem \ SL.From).text),
    emptyToNull((elem \ SL.To).text),
    emptyToNull((elem \ SL.Classname).text)
  )

  lazy val xml =
    <mapper>
      { if (`type` != null) <type>{`type`}</type> }
      { if (from != null) <from>{from}</from> }
      { if (to != null) <to>{to}</to> }
      { if (classname != null) <classname>{classname}</classname> }
    </mapper>
}
