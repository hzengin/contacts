package org.hzengin.contacts


import java.io.File

import org.hzengin.contacts.models.Contact
import scala.util.Try
import scala.xml.{Node, XML}

object Importer {

  def getFile(path: String): Option[File] = {
    val file = new File(path)
    file.exists() match {
      case false =>
        None
      case true =>
        Some(file)
    }
  }

  def importFile(file: File): Try[List[Contact]] = {
    Try(mergeDuplicates(parse(file)))
  }

  def parse(file: File): List[Contact] = {
    (XML.loadFile(file) \ "contact").toList.map { node =>
      Contact(None, (node \ "name").text,(node \ "lastName").text,List((node \ "phone").text))
    }
  }

  def mergeDuplicates(contacts: List[Contact]): List[Contact] = {
    contacts.groupBy(c => (c.name, c.lastName)).map(p => p._2.foldLeft(p._2.head)(_.merge(_))).toList
  }

}
