package com.agilogy.json

import org.scalatest.FlatSpec
import play.api.libs.json.{ Json, JsNumber }

class JsonComparatorWithEllipsisSpec extends FlatSpec {

  import JsonComparator._

  it should "compare Json strings with ellipsis when nothing replaces the ellipsis" in {
    val actual = """{"a":1}"""
    val expected = """{"a":1,...}"""
    assert(diff(expected, actual) === Seq())
  }

  it should "compare Json strings with ellipsis when nothing replaces the ellipsis in nested objects" in {
    val actual = """{"b":{"a":1}}"""
    val expected = """{"b":{"a":1,...}}"""
    assert(diff(expected, actual) === Seq())
  }

  it should "compare Json objects with ellipsis only" in {
    val expected = """{...}"""
    val actual = """{"a":1}"""
    assert(diff(expected, actual) === Seq())
  }

  it should "compare Json objects with ellipsis in the value" in {
    val expected = """{"a":..., "b":5, "c":...}"""
    val actual = """{"a":1, "b":5, "c": 3}"""
    assert(diff(expected, actual) === Seq())
  }

  it should "compare Json objects with ellipsis only when they are nested in an array" in {
    val expected = """{"b": [{"a":1},...,{"a":3}]}"""
    val actual = """{"b": [{"a":1},{"a":2},{"a":3}]}"""
    assert(diff(expected, actual) === Seq())
  }

  it should "compare Json objects assuming {...} represents whatever object" in {
    val expected = """{"b": [...,{...},...,{"a":3},...]}"""
    val actual = """{"b": [{"a":3}]}"""
    assert(diff(expected, actual) === Seq(Difference("/b[1]", Some(Json.obj("a" -> 3)), None)))
  }

  it should "fail comparing Json strings with ellipsis when something outside the ellipsis differs" in {
    val actual = """{"a":1}"""
    val expected = """{"a":2,...}"""
    assert(diff(expected, actual) === Seq(Difference("/a", Some(JsNumber(2)), Some(JsNumber(1)))))
  }

  it should "fail comparing Json strings with ellipsis in nested objects when something outside the ellipsis differs" in {
    val actual = """{"b":{"a":1}}"""
    val expected = """{"b":{"a":2,...}}"""

    assert(diff(expected, actual) === Seq(Difference("/b/a", Some(JsNumber(2)), Some(JsNumber(1)))))
  }

  it should "accept additional attributes when ellipsis is found in expected object" in {
    val actual = """{"a":1,"b":2}"""
    val expected = """{"a":1,...}"""

    assert(diff(expected, actual) === Seq())
  }

  it should "accept additional attributes when ellipsis is found in nested expected object" in {
    val actual = """{"c":{"a":1,"b":2}}"""
    val expected = """{"c":{"a":1,...}}"""

    assert(diff(expected, actual) === Seq())
  }

  it should "compare json arrays with ellipsis when the ellipsis represents zero elements" in {

    assert(diff("""[1,...]""", """[1]""") === Seq())
    //assert(diff("""[...,1,...]""","""[1]""","Test")
  }

  it should "compare json arrays with nested ellipsis when the ellipsis represents zero elements" in {
    assert(diff("""{"a":[1,...]}""", """{"a":[1]}""") === Seq())
    //assert(diff("""[...,1,...]""","""[1]""","Test")
  }

  it should "compare json arrays with ellipsis between elements" in {
    assert(diff("""{"a":[1,...,2]}""", """{"a":[1,2]}""") === Seq())
    assert(diff("""{"a":[1,...,2]}""", """{"a":[1,3,4,2]}""") === Seq())
    assert(diff("""{"a":[1,...,2]}""", """{"a":[1,3,4]}""") === Seq(Difference("/a[3]", Some(JsNumber(2)), None)))
  }

  it should "fail to compare json arrays with ellipsis when something differs" in {
    assert(diff("""[1,...]""", """[2]""") === Seq(Difference("[0]", Some(JsNumber(1)), Some(JsNumber(2)))))
  }

  it should "accept additional elements when an array has ellipsis" in {
    assert(diff("""[1,...]""", """[1,2,3]""") === Seq())
  }

  it should "compare json arrays with ellipsis at the end" in {
    assert(diff("""{"a":[1,...]}""", """{"a":[1]}""") === Seq())
    assert(diff("""{"a":[1,...]}""", """{"a":[1,3,4,2]}""") === Seq())
    assert(diff("""{"a":[...]}""", """{"a":[]}""") === Seq())
  }

}

