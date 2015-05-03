package org.hzengin.contacts.tests

import org.scalatest.{Matchers, FlatSpec}

import org.hzengin.contacts.util

class ArgsTest extends FlatSpec with Matchers {
  "ArgParser" should "parse --find-by-name argument correctly" in {
    val args = Array("--find-by-name", "Han")
    util.parseArgs(args).name.get should be ("Han")
  }

  it should "parse --import arguments correctly" in {
    val args = Array("--import", "a.xml", "b.xml")
    util.parseArgs(args).files.get.length should be (2)
    util.parseArgs(args).files.get.apply(0) should be ("a.xml")
    util.parseArgs(args).files.get.apply(1) should be ("b.xml")
  }

  it should "detect that invalid arguments if exists" in {
    val args1 = Array("--try a.xml")
    util.parseArgs(args1).files should be (None)
    util.parseArgs(args1).name should be (None)
    val args2 = Array("--find-by-name a.xml b.xml")
    util.parseArgs(args2).files should be (None)
    util.parseArgs(args2).name should be (None)
    val args3 = Array("--find-by-name --import ads")
    util.parseArgs(args3).files should be (None)
    util.parseArgs(args3).name should be (None)


  }

}
