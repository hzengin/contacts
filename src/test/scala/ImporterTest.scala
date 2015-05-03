package org.hzengin.contacts.tests

import org.scalatest._
import org.hzengin.contacts.Importer

class ImporterTest extends FlatSpec with Matchers {
  "Importer" should "know given file may not exist" in {
    Importer.getFile("non-existing-path") should be (None)
  }

  it should "know given file may exist" in {
    Importer.getFile(getClass.getResource("/empty.xml").getFile) shouldNot be (None)
  }

  it should "parse empty files without error and return an empty list" in {
    val file = Importer.getFile(getClass.getResource("/empty.xml").getFile).get
    Importer.parse(file).isEmpty should be (true)
  }

  it should "parse a valid file and should return a list of contacts " in {
    val file = Importer.getFile(getClass.getResource("/noduplicates.xml").getFile).get
    Importer.parse(file).isEmpty shouldNot be (true)
    Importer.parse(file).length should be (3)
  }

  it should "merge in-file duplicates correctly" in {
    val file = Importer.getFile(getClass.getResource("/withduplicates.xml").getFile).get
    val contacts = Importer.parse(file)
    Importer.mergeDuplicates(contacts).length should be (2)
    Importer.mergeDuplicates(contacts).find(_.name == "Han").get.phones.length should be (2)
    Importer.mergeDuplicates(contacts).find(_.name == "Han").get.phones.find(_ == "+000000000005") shouldNot be (None)
    Importer.mergeDuplicates(contacts).find(_.name == "Han").get.phones.find(_ == "+000000000001") shouldNot be (None)
  }

}
