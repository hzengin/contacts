package org.hzengin.contacts

import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import org.hzengin.contacts.models.Contact
import org.hzengin.contacts.models.Contact.ContactHandlers._
import org.hzengin.contacts.ResultType.ResultType

object ResultType extends Enumeration {
  type ResultType = Value
  val Inserted, Updated, Failed = Value
}

object ContactDao {


  private val driver = new MongoDriver
  private val connection: MongoConnection =
    MongoConnection.parseURI(Config.URI).map { parsedUri =>
      driver.connection(parsedUri)
    }.get
  private val db = connection(Config.databaseName)
  private val collection = db[BSONCollection](Config.collectionName)
  prepareCollection()

  def insertAll(contacts: List[Contact]): Future[List[ResultType]] = {
    Future.sequence(
      contacts.map { contact =>
        insert(contact) { existing =>
          contact.merge(existing)
        }
      }
    )
  }

  def findByName(name: String): Future[List[Contact]] = {
    val query = BSONDocument("$text" -> BSONDocument("$search" -> name))
    collection.find(query).cursor[Contact].collect[List]()
  }

  private def insert(contact: Contact)(ifExists: (Contact) => Contact): Future[ResultType] = {
    getSingle(contact.name, contact.lastName).flatMap {
        case Some(existingContact: Contact) =>
          val newContact = ifExists(existingContact)
          update(existingContact, newContact)
        case _ =>
          collection.insert(contact).map(_ => ResultType.Inserted).recover{case _ => ResultType.Failed}
    }
  }


  private def update(oldContact: Contact, newContact: Contact): Future[ResultType] = {
    val selector = BSONDocument("$and" ->
      BSONArray(
        BSONDocument("name" -> oldContact.name),
        BSONDocument("lastName" -> oldContact.lastName)
      )
    )
    val modifier = BSONDocument(
      "$set" -> BSONDocument(
        "name" -> newContact.name,
        "lastName" -> newContact.lastName,
        "phones" -> newContact.phones
      )
    )
    collection.update(selector, modifier).map( _ => ResultType.Updated ).recover{case _ => ResultType.Failed}
  }

  private def getSingle(name: String, lastName: String): Future[Option[Contact]] = {
    val query = BSONDocument("$and" ->
      BSONArray(
        BSONDocument("name" -> name),
        BSONDocument("lastName" -> lastName)
      )
    )
    collection.find(query).one[Contact]
  }

  private def prepareCollection() = {
    val nameIndex = Index(Seq(("name",IndexType.Text)),name = Some("nameIndex"), unique = false, dropDups = false)
    collection.indexesManager.ensure(nameIndex)
  }

}
