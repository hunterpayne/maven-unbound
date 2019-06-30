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

import java.io.{ File, FileOutputStream, OutputStreamWriter, Writer }

import scala.util.Try
import scala.xml.{ Elem, PrettyPrinter, XML }

/**
  * A trait case classes can extend to get the ability to write themselves
  * as XML provided they have a field named xml of type Elem
  */
trait Writeable {

  val xml: Elem

  // max width: 80 chars
  // indent:     2 spaces
  /**
    * returns a string containing a pretty printed XML document represented
    * by the field xml
    */
  def toXmlString: String = {
    val printer = new PrettyPrinter(80, 2)
    val sb = new StringBuilder()
    printer.format(xml, sb)
    sb.toString()
  }

  /** writes this object into the provided Writer */
  def writePOM(writer: Writer): Unit = writer.write(toXmlString)

  /** writes this object to the specified file using UTF-8 character encoding */
  def writePOM(filename: String): Unit = {
    val writer =
      new OutputStreamWriter(new FileOutputStream(new File(filename)), "UTF-8")
    try { writePOM(writer) } finally { writer.close() }
  }
}
