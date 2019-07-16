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

import scala.compat.Platform.EOL
import scala.xml.{ Comment => XmlComment, Elem, Node, Null, Text, TopScope }

import com.typesafe.config.{
  Config, ConfigList, ConfigObject, ConfigValue, ConfigValueFactory, ConfigValueType }

sealed trait PathElement {

  def matchNode(parent: Elem, n: Node): Boolean

  def matchConf(parent: ConfigValue, key: String, n: ConfigValue): Boolean
}

case class ElementLabel(label: String) extends PathElement {

  def matchNode(parent: Elem, n: Node): Boolean = n match {
    case e: Elem => e.label == label
    case _ => false
  }

  def matchConf(parent: ConfigValue, key: String, n: ConfigValue): Boolean =
    key == label
}

case class ListIndex(idx: Int) extends PathElement {

  def matchNode(parent: Elem, n: Node): Boolean = n match {
    case e: Elem =>
      parent.child.toSeq.filter(_.isInstanceOf[Elem]).indexOf(e) == idx
    case _ => false
  }

  import scala.collection.JavaConverters._

  def matchConf(parent: ConfigValue, key: String, n: ConfigValue): Boolean =
    parent match {
      case l: ConfigList => if (l.size > idx) l.get(idx) == n else false
      case o: ConfigObject =>
        val keys = o.asScala.keys
        if (keys.size > idx) {
          o.get(keys.toSeq(idx)) == n
        } else false
    }
}

case class CommentPath(elems: PathElement*) {

  def insertConf(s: Seq[String], conf: Config): Config = {
    import scala.collection.JavaConverters._

    def findNext(
      it: Iterator[PathElement], cv: ConfigValue): Seq[ConfigValue] =
      if (it.hasNext) {
        val pe = it.next
        cv match {
          case cur: ConfigObject =>
            cur.asScala.find { case(k, v) => pe.matchConf(cur, k, v) } match {
              case Some((_, c)) => findNext(it, c) :+ cur
              case None =>
                println("no match " + cur + " looking for " + pe)
                Seq()
            }
          case cl: ConfigList =>
            cl.asScala.find { v => pe.matchConf(cl, "", v) } match {
              case Some(c) => findNext(it, c) :+ cl
              // special case to deal with lists that are empty but contain
              // comments and so their path has an extra ListIndex(0) on
              // the end which we consume here
              case None if (pe == ListIndex(0) && cl.isEmpty && !it.hasNext) =>
                Seq(cl)
              // special case for comments after the last element of a list
              case None if (pe == ListIndex(cl.size) && !it.hasNext) => Seq(cl)
              case None =>
                println("no match list " + cl + " looking for " + pe)
                Seq()
            }
        }
      } else Seq(cv)

    val it = elems.toIterator
    val path = findNext(it, conf.root())
    if (!path.isEmpty && elems.size <= path.size) {
      val relems = elems.reverse.toIterator

      // println("for elems " + elems + " path size " + path.size)
      // println(
      //  "paths " + path.map { s =>
      //    val str = s.toString
      //    if (str.size > 60) str.substring(0, 60) else str }.mkString(","))

      // add comments to path.head
      val oldComments = path.head.origin().comments().asScala
      val origin = path.head.origin().withComments((oldComments ++ s).asJava)
      // println("origin " + origin.comments.asScala.mkString)
      path.tail.foldLeft(path.head.withOrigin(origin)) { case(ch, par) =>
        (if (relems.hasNext) {
          par match {
            case cur: ConfigObject => relems.next match {
              case ElementLabel(label) => cur.withValue(label, ch)
              case ListIndex(idx) =>
                // special case to deal with lists that are empty but contain
                // comments and so their path has an extra ListIndex(0) on
                // the end which we consume here
                if (ch.valueType() == ConfigValueType.LIST &&
                  relems.hasNext && ch.asInstanceOf[ConfigList].isEmpty) {
                  // println("special zero " + ch)
                  relems.next match {
                    case ElementLabel(lbl) => cur.withValue(lbl, ch)
                    case _ =>
                      println("mismatch " + cur + " @ listidx=" + idx)
                      cur
                  }
                } else {
                  val keys = cur.asScala.keys.toSeq
                  if (keys.size > idx) {
                    val k = keys(idx)
                    cur.withValue(k, ch)
                  } else {
                    println("mismatch " + cur + " @ listidx=" + idx)
                    cur
                  }
                }
            }
            case cl: ConfigList => relems.next match {
              case ListIndex(idx) =>
                val lst = new java.util.ArrayList[ConfigValue](cl)
                lst.set(idx, ch)
                ConfigValueFactory.fromIterable(lst)
              case ElementLabel(label) =>
                println("mismatch " + label + " in " + cl + " elems " + elems)
                cl
            }
          }
        } else {
          par
        })}.asInstanceOf[ConfigObject].toConfig
    } else {
      conf
    }
  }

  def insertXml(s: Seq[String], root: Elem): Elem = {
    import scala.collection.JavaConverters._

    def findNext(it: Iterator[PathElement], cur: Elem): Seq[Elem] =
      if (it.hasNext) {
        it.next match {
          case ElementLabel(label) =>
            cur.child.find {
              case ch: Elem => ch.label == label
              case _ => false
            } match {
              case Some(ch: Elem) => findNext(it, ch) :+ cur
              case _ => Seq()
            }
          case ListIndex(idx) =>
            findNext(
              it,
              cur.child.filter { _.isInstanceOf[Elem]
              }(idx).asInstanceOf[Elem]) :+
            cur
        }
      } else Seq(cur)

    val it = elems.tail.toIterator // skip project
    val path = findNext(it, root)

    if (!path.isEmpty) {

      val relems = elems.tail.reverse.toIterator
      val newChildren: Seq[Node] =
        s.map { XmlComment(_) } ++ path.head.child.toSeq
      val oldLeaf = path.head
      val newLeaf = new Elem(
        oldLeaf.prefix, oldLeaf.label, oldLeaf.attributes, oldLeaf.scope,
        newChildren: _*)

      path.tail.foldLeft((newLeaf, oldLeaf)) { case((ch, oldCh), par) =>
        val newChildren = par.child.updated(par.child.indexOf(oldCh), ch)
        (new Elem(
          par.prefix, par.label, par.attributes, par.scope, newChildren: _*),
         par)
      }._1
    } else root
  }
}

case class Comments(s: Seq[String], path: CommentPath) {

  def insertConf(conf: Config): Config = path.insertConf(s, conf)

  def insertXml(conf: Elem): Elem = path.insertXml(s, conf)
}

case class DocComments(comments: Seq[Comments]) {

  def insertConf(conf: Config): Config =
    comments.foldLeft(conf) { case(c, comment) => comment.insertConf(c) }

  def insertXml(root: Elem): Elem =
    comments.foldLeft(root) { case(r, comment) => comment.insertXml(r) }
}

object CommentExtractor {

  def apply(conf: Config): DocComments = {
    import scala.collection.JavaConverters._

    def traverse(curr: ConfigValue, key: Any, path: List[PathElement]):
        Seq[Comments] = {

      val newPath = path :+ (key match {
        case s: String => ElementLabel(s)
        case i: Int => ListIndex(i)
      })

      val comments = curr.origin().comments().asScala.toSeq
      val thisComments =
        if (comments.isEmpty) Seq()
        else Seq(Comments(comments, CommentPath(newPath: _*)))
      val childComments: Seq[Comments] = curr match {
        case cur: ConfigObject =>
          cur.asScala.flatMap { case(k, v) => traverse(v, k, newPath) }.toSeq
        case cl: ConfigList =>
          cl.asScala.zipWithIndex.flatMap { case(v, idx) =>
            traverse(v, idx, newPath) }
        case _ => Seq()
      }
      thisComments ++ childComments
    }

    val root = conf.root()
    val buf = List()
    DocComments(
      root.asScala.flatMap { case(k, v) => traverse(v, k, buf) }.toSeq)
  }

  protected[unbound] def isListElem(cur: Elem): Boolean = {
    val elemChildren = cur.child.filter(_.isInstanceOf[Elem])
    if (elemChildren.size > 1 ||
      (1 == elemChildren.size &&
        singular(cur.label) == elemChildren(0).label)) {
      val head = elemChildren.head
      elemChildren.tail.forall { _ match {
        case e: Elem => e.label == head.label
        case t: Text => t.text.trim.isEmpty
        case c: XmlComment => true
        case _ => false
      }}
    } else {
      false
    }
  }

  def apply(root: Elem): DocComments = {

    def traverse(cur: Elem, path: List[Elem]): Seq[Comments] = {
      val newPath = path :+ cur
      var elemIdx = 0
      var lastLabel: String = null
      val elements = cur.child.filter { _.isInstanceOf[Elem] }
      val nextLabels: Map[String, String] =
        if (elements.size > 1)
          elements.map { _.label }.sliding(2).map { s => (s(0), s(1)) }.toMap
        else Map[String, String]()
      val isList = isListElem(cur)
      val numElems = cur.child.filter(_.isInstanceOf[Elem]).size
      cur.child.flatMap { n => n match {
        case e: Elem =>
          elemIdx = Math.min(elemIdx + 1, numElems - 1)
          lastLabel = e.label
          traverse(e, newPath)
        case c: XmlComment =>
          val newPathElems = elemPathToPath(newPath)
          val comments = c.commentText.split(Array('\n', '\r'))
          // dependencies are always in lists and the string dependency is never
          // a key
          if (null == lastLabel || isList ||
            lastLabel == SL.DependencyStr.toString)
            Seq(Comments(
              comments.toSeq,
              CommentPath((newPathElems ++ Seq(ListIndex(elemIdx))): _*)))
          else Seq(Comments(
            comments.toSeq,
            CommentPath((newPathElems ++ Seq(ElementLabel(
              nextLabels.getOrElse(lastLabel, lastLabel)))): _*)))
        case _ => Seq()
      }}
    }

    def elemPathToPath(path: List[Elem]): Seq[PathElement] = path match {
      case h :: h2 :: rest =>
        if (isListElem(h))
          Seq(
            ElementLabel(h.label),
            ListIndex(h.child.filter(_.isInstanceOf[Elem]).indexOf(h2))) ++
            elemPathToPath(rest)
        else Seq(ElementLabel(h.label)) ++ elemPathToPath(h2 :: rest)
      case h :: Nil => Seq(ElementLabel(h.label))
      case Nil => Seq()
    }

    val buf = List(root)
    DocComments(root.child.flatMap { _ match {
      case e: Elem => traverse(e, buf)
      case _ => Seq()
    }})
  }
}
