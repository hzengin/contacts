package org.hzengin.contacts.tests

import org.hzengin.contacts.{Config, ContactDao}
import org.hzengin.contacts.models.Contact
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span, Seconds}
import org.scalatest.{BeforeAndAfter, Matchers, FlatSpec}
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.api.{MongoConnection, MongoDriver}
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONObjectID, BSONDocument}

import scala.concurrent.ExecutionContext.Implicits.global

class ContactTest extends FlatSpec with Matchers with ScalaFutures {

  private val driver = new MongoDriver
  private val connection: MongoConnection =
    MongoConnection.parseURI(Config.URI).map { parsedUri =>
      driver.connection(parsedUri)
    }.get
  private val db = connection(Config.databaseName)
  private val collection = db[BSONCollection](Config.collectionName)

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(2, Seconds), interval = Span(5, Millis))


  def resetTestEnv(): Unit = {
    collection.remove(BSONDocument()) //clear database collection
  }.futureValue


  "Contacts" should "be inserted in database correctly" in {
    resetTestEnv()
    val contact1 = Contact(None, "Huseyin", "Zengin", List("+905535252336"))
    val contact2 = Contact(None, "Han", "Solo", List("+90123456789"))
    val contactsToInsert = List(contact1, contact2)
    ContactDao.insertAll(contactsToInsert).futureValue
    val contacts = collection.find(BSONDocument()).cursor[Contact].collect[List]().futureValue
    contacts.length should be (2)
    contacts.find(_.name == "Huseyin") shouldNot be (None)
    contacts.find(_.name == "Han") shouldNot be (None)
  }

  it should "be merged with older contacts (which has same name-lastName pair) in database" in {
    resetTestEnv()
    val contact1 = Contact(None, "Huseyin", "Zengin", List("+905535252336"))
    val contact2 = Contact(None, "Han", "Solo", List("+90123456789"))
    val contactsToInsert = List(contact1,contact2)
    ContactDao.insertAll(contactsToInsert).futureValue
    val contact3 = Contact(None, "Huseyin", "Zengin", List("31415926535"))
    ContactDao.insertAll(List(contact3)).futureValue
    val mergedContact = collection.find(BSONDocument("name" -> "Huseyin")).one[Contact].futureValue.get
    mergedContact.phones.length should be (2)
    mergedContact.phones.find(_ == "31415926535") shouldNot be (None)
    mergedContact.phones.find(_ == "+905535252336") shouldNot be (None)
  }

  it should "be found by exact name" in {
    resetTestEnv()
    val contact1 = Contact(None, "Huseyin", "Zengin", List("+905535252336"))
    val contact2 = Contact(None, "Han", "Solo", List("+90123456789"))
    val contactsToInsert = List(contact1,contact2)
    ContactDao.insertAll(contactsToInsert).futureValue
    val wantedContact = ContactDao.findByName("Huseyin").futureValue.head
    wantedContact.name should be ("Huseyin")
    wantedContact.lastName should be ("Zengin")
    wantedContact.phones.head should be ("+905535252336")
  }

  it should "be found by only one word in name" in {
    resetTestEnv()
    val contact1 = Contact(None, "Huseyin", "Zengin", List("+905535252336"))
    val contact2 = Contact(None, "Obi Wan", "Kenobi", List("+90123456789"))
    val contactsToInsert = List(contact1,contact2)
    ContactDao.insertAll(contactsToInsert).futureValue
    val result = ContactDao.findByName("Obi").futureValue
    val result2 = ContactDao.findByName("Wan").futureValue
    result shouldEqual result2
    result.head.lastName should be("Kenobi")
    resetTestEnv()
  }






}
