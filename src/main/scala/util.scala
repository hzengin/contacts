package org.hzengin.contacts

import org.hzengin.contacts.ResultType.ResultType

import scala.concurrent.Future

object util {

  val usage = """Usage: contacts [--find-by-name name] [--import filename1 filename2 ...]"""

  case class CommandlineInput(files: Option[List[String]], name: Option[String])

  def parseArgs(args: Array[String]): CommandlineInput = {
    var files = Seq[String]()
    args.toList match {
      case "--import" :: file :: tail =>
        def parseFilenames(args: List[String]): CommandlineInput = {
          args match {
            case file :: tail if file(0) == '-' =>
              CommandlineInput(None, None)
            case file :: tail =>
              files :+= file
              parseFilenames(tail)
            case Nil =>
              CommandlineInput(Some(files.toList),None)
          }
        }
        files :+= file
        parseFilenames(tail)
      case "--find-by-name" :: name :: Nil =>
        CommandlineInput(None, Some(name))
      case Nil =>
        CommandlineInput(None, None)
      case _ =>
        CommandlineInput(None, None)
    }
  }

}
