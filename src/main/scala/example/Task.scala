package example

import java.io.{FileOutputStream, PrintStream}

import ujson.Obj
import ujson.Value.Value

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object Task extends App {

  case class Country(name: String, capital: String, area: Double)

  def parse(url: String, limit: Int, region: String): ArrayBuffer[Obj] = {
    val file = Source.fromURL(url)
    val json = file.getLines.mkString
    val data = ujson.read(json)

    val countries = getCountries(data, limit, region)

    countries.map(country => {
      ujson.Obj(
        "name" -> ujson.Str(country.name),
        "capital" -> ujson.Str(country.capital),
        "area" -> ujson.Num(country.area)
      )
    })
  }

  def getCountries(data: Value, limit: Int, region: String): ArrayBuffer[Country] = {
    data.arr.filter(record => {
      record("region").str == region
    })
      .map(record => {
        val capital = Option(record("capital").arr) filter {
          _.nonEmpty
        } map { arr => arr(0) } map {
          _.str
        }

        Country(record("name")("official").str, capital getOrElse "", record("area").num)
      })
      .sortBy(_.area)(Ordering[Double].reverse)
      .take(limit)
  }

  val url = "https://raw.githubusercontent.com/mledoze/countries/master/countries.json"

  val result = parse(url, 10, "Africa")

  val outputFile = args(0)

  private val fos = new FileOutputStream(outputFile)
  private val printer = new PrintStream(fos)

  printer.println(ujson.write(result))
}
