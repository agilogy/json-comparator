package com.agilogy.json

import play.api.libs.json._
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import scala.annotation.tailrec

case class Difference(path: String, expected: Option[JsValue], actual: Option[JsValue])

object JsonComparator {

  val ellipsisAttribute: (String, JsValue) = "$$hasEllipsis$$" -> JsBoolean(value = true)
  val ellipsisValue = Json.obj("$$ellipsis$$" -> true)

  private val ellipsisValueInRegex = """{"\$\$ellipsis\$\$":true}"""
  private val ellipsisAttributeInRegex = """"\$\$hasEllipsis\$\$":true"""

  private def jsonifyEllipsis(s: String) =
    s.replaceAll("""\:\s*\.\.\.\s*\}""", ":" + ellipsisValueInRegex + "}")
      .replaceAll("""\.\.\.\s*\}""", ellipsisAttributeInRegex + "}")
      // When the ellipsis is followed by , or ] we assume it is inside an array
      .replaceAll("""\.\.\.\s*([\,\]])""", ellipsisValueInRegex + "$1")

  implicit def stringToJson(s: String): JsValue = {
    val json = jsonifyEllipsis(s)
    Json.parse(json)
  }

  def diff(expected: String, actual: String): Seq[Difference] = diff(stringToJson(expected), stringToJson(actual))(ellipsisValue, ellipsisAttribute)

  def diff(expected: JsValue, actual: JsValue)(implicit
    ellipsisValue: JsObject = ellipsisValue,
    ellipsisAttribute: (String, JsValue) = ellipsisAttribute): Seq[Difference] = {

    def diff1(e: JsValue, a: JsValue, path: String): Seq[Difference] = (e, a) match {
      case (`ellipsisValue`, _) => Seq()
      case (_, `ellipsisValue`) => Seq()
      case (eo: JsObject, ao: JsObject) => diffObjects(eo, ao, path)
      case (ea: JsArray, aa: JsArray) => diffArrays(ea.value.toList, aa.value.toList, path, 0)
      case _ if e == a => Seq()
      case _ => Seq(Difference(path, Some(e), Some(a)))
    }

    @tailrec
    def diffArrays(expectedElements: List[JsValue], actualElements: List[JsValue], path: String, idx: Int, ellipsisFound: Boolean = false, acc: Seq[Difference] = Seq()): Seq[Difference] = {
      (expectedElements, actualElements) match {
        case (e :: et, a) if e == ellipsisValue => diffArrays(et, a, path, idx, ellipsisFound = true, acc = acc)
        // TODO: case the ellipsisValue is in the midle of the array
        case (e :: et, Nil) => diffArrays(et, Nil, path, idx + 1, acc = acc ++ Seq(Difference(s"$path[$idx]", Some(e), None)))
        case (e :: et, a :: at) =>
          val valueDiff = diff1(e, a, s"$path[$idx]")
          val continueSkippingElements = ellipsisFound && valueDiff.nonEmpty
          if (continueSkippingElements)
            diffArrays(expectedElements, at, path, idx + 1, continueSkippingElements, acc = acc)
          else
            diffArrays(et, at, path, idx + 1, continueSkippingElements, acc = acc ++ valueDiff)
        case (Nil, a :: at) =>
          if (ellipsisFound)
            acc
          else
            acc ++ actualElements.zipWithIndex.map(aewi => Difference(s"$path[${idx + aewi._2}]", None, Some(aewi._1)))
        case (Nil, Nil) => acc
      }
    }

    def diffObjects(expected: JsObject, actual: JsObject, path: String) = {
      val fieldEllipsis = expected.fields.contains(ellipsisAttribute)
      val expectedFieldsMap = expected.value.filterNot(_ == ellipsisAttribute)
      val actualFieldsMap =
        if (fieldEllipsis) actual.value.filter(p => expectedFieldsMap.contains(p._1))
        else actual.value
      val fieldsDiff: Seq[Difference] = expectedFieldsMap.foldLeft(Seq[Difference]()) {
        (diffs: Seq[Difference], entry: (String, JsValue)) =>
          if (actualFieldsMap.contains(entry._1)) {
            diffs ++ diff1(entry._2, actualFieldsMap(entry._1), path + "/" + entry._1)
          } else {
            diffs ++ Set(Difference(path + "/" + entry._1, Some(entry._2), None))
          }
      }
      val actualUnexpectedFields = actualFieldsMap.keySet.toSeq diff expectedFieldsMap.map(_._1).toSeq
      fieldsDiff ++ actualUnexpectedFields.map(fn => Difference(path + "/" + fn, None, Some(actualFieldsMap(fn))))

    }

    diff1(expected, actual, "")
  }
}
