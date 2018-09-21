package com.agilogy.json

import org.scalatest.FlatSpec
import play.api.libs.json.Json

class JsonComparatorSpec extends FlatSpec {

  import JsonComparator._

  behavior of "JsonComparator"

  it should "compare two exactly equal json primitives" in {
    assert(diff(""""a"""", """"a"""") === List())
  }

  it should "compare two different json primitives" in {
    val r = diff(""""a"""", """"b"""")
    assert(r.size === 1)
    val diff0 = r.head
    assert(diff0 === Difference("", Some(""""a""""), Some(""""b"""")))
  }

  val json1 = """{"a":1,"b":2}"""

  it should "compare two exactly equal json objects" in {
    val json1Copy = Json.parse(json1)
    assert(diff(json1, json1Copy) === List())
  }

  it should "compare two equal json nodes ignoring differences in white spaces" in {
    val json1s =
      """{"a":1,
          "b":2}"""
    assert(diff(json1, json1s) === List())
  }

  it should "compare two objects with differences in one field value" in {
    val json2 = """{"a":1,"b":3}"""
    assert(diff(json1, json2) === List(Difference("/b", Some("2"), Some("3"))))
  }

  it should "compare two objects with a missing field in the right one" in {
    val json2 = """{"a":1}"""
    assert(diff(json1, json2) === List(Difference("/b", Some("2"), None)))
  }

  it should "compare two objects with a missing field in the left one" in {
    val json2 = """{"a":1,"b":2,"c":8}"""
    assert(diff(json1, json2) === List(Difference("/c", None, Some("8"))))
  }

  val jsonArr1 = """[1,2,3]"""

  it should "compare two equal arrays" in {
    val jsonArr1Copy = Json.parse(jsonArr1)
    assert(diff(jsonArr1, jsonArr1Copy) === List())
  }

  it should "compare two arrays with differences in a common position" in {
    val jsonArr2 = """[1,"woohoo!",3]"""
    assert(diff(jsonArr1, jsonArr2) === List(Difference("[1]", Some("2"), Some(""""woohoo!""""))))
  }

  it should "compare a longer array with a shorter one" in {
    val jsonArr2 = """[1,2]"""
    assert(diff(jsonArr1, jsonArr2) === List(Difference("[2]", Some("3"), None)))
  }

  it should "compare a shorter array with a longer one" in {
    val jsonArr2 = """[1,2,3,4]"""
    assert(diff(jsonArr1, jsonArr2) === List(Difference("[3]", None, Some("4"))))
  }

  it should "compare arrays nested in objects" in {
    val json1 = """{"a":{"b":[1,2,3]}}"""
    val json2 = """{"a":{"b":[1,55,3]}}"""
    assert(diff(json1, json2) === List(Difference("/a/b[1]", Some("2"), Some("55"))))
  }

  it should "accumulate diffs in a List" in {
    val json1 = """{"a":{"b":[1,2,3],"c":3}}"""
    val json2 = """{"a":{"b":[1,55,3,4],"d":"ddd"}}"""
    assert(diff(json1, json2) === List(
      Difference("/a/b[1]", Some("2"), Some("55")),
      Difference("/a/b[3]", None, Some("4")),
      Difference("/a/c", Some("3"), None),
      Difference("/a/d", None, Some(""""ddd""""))))
  }

  it should "compare apples and pears" in {
    val arr = """[1,2,3]"""
    val obj = """{"a":1}"""
    val value = "2"
    assert(diff(arr, obj) === List(Difference("", Some(arr), Some(obj))))
    assert(diff(obj, value) === List(Difference("", Some(obj), Some(value))))
  }

}

