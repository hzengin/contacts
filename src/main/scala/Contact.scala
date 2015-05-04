package org.hzengin.contacts.models

import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader, BSONObjectID}

import scala.xml.Node


case class Contact(id: Option[BSONObjectID], name: String, lastName: String, phones: List[String]) {
  override def equals(other: Any) = other match {
    case contact: Contact =>
      (contact.name == this.name) && (contact.lastName == this.lastName)
    case _ =>
      false
  }

  def merge(other: Contact): Contact = {
    val mergedPhones = (this.phones ++ other.phones).distinct //merge, remove duplicates
    Contact(id, name, lastName, mergedPhones)
  }

  override def toString: String = {
    s"First Name: $name \nLast Name: $lastName \nPhone(s): \n ${phones.foldLeft("")(_ + _ + " ")} \n"
  }

}

object Contact {
  implicit object ContactHandlers extends BSONDocumentReader[Contact] with BSONDocumentWriter[Contact] {
    override def read(bson: BSONDocument): Contact = {
      Contact(bson.getAs[BSONObjectID]("_id"),
        bson.getAs[String]("name").get,
        bson.getAs[String]("lastName").get,
        bson.getAs[List[String]]("phones").get)
    }
    override def write(contact: Contact): BSONDocument = {
      BSONDocument("_id" -> BSONObjectID.generate,
        "name" -> contact.name,
        "lastName" -> contact.lastName,
        "phones" -> contact.phones)
    }
  }
}



