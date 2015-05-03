package org.hzengin.contacts


import org.hzengin.contacts.models.Contact
import org.hzengin.contacts.util.CommandlineInput
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try


object Contacts {

  def main(args: Array[String]): Unit = {
    util.parseArgs(args) match {
      case CommandlineInput(None, None) =>
        println(util.usage)
        System.exit(0)

      case CommandlineInput(Some(files), None) =>
        files.map(Importer.getFile).map { fileOp =>
          fileOp match {
            case None =>
              println(s"File not found.")
            case Some(file: File) =>
              println(s"Importing ${file.getName}")
              val b = Importer.importFile(file).map { contacts =>
                if(contacts.length == 0) {
                  println("Couldn't found any contacts")
                } else {
                  val results = Await.result(ContactDao.insertAll(contacts), Duration.Inf)
                  println(s"Inserted ${results.count(_ == ResultType.Inserted)} contact(s)")
                  println(s"Updated ${results.count(_ == ResultType.Updated)} contact(s)")
                  println(s"Failed to insert/update ${results.count(_ == ResultType.Failed)} contact(s)")
                }
              }.recover {
                case e: org.xml.sax.SAXParseException =>
                  println(s"Invalid XML file ${file.getName}")
                case _ =>
                  println(s"Something we didn't expect just happened when importing ${file.getName} \n BTW penguins are still cute")
              }
          }
        }
        System.exit(0)


      case CommandlineInput(None, Some(name)) =>
        ContactDao.findByName(name).map { contacts =>
          contacts match {
            case Nil =>
              println(s"Couldn't find any contact with name: `$name` ")
            case list =>
              println(s"${list.length} contact(s) found: ")
              contacts.foreach(println)
          }
        }.onComplete {
          _ =>
            System.exit(0)
        }
    }
  }
}