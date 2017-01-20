package org.time2java.tRussianBank

import java.io.{BufferedReader, IOException, InputStreamReader}

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder

/**
  * Created by time2die on 07.01.17.
  */
object GoogleApiClient {
  private[tRussianBank] val conf: Config = ConfigFactory.load

  def getStatus: String = {
    val docId: String = conf.getString("docId")
    val key: String = conf.getString("key")
    val url: String = "https://sheets.googleapis.com/v4/spreadsheets/" + docId + "/values/A5%3AB7?key=" + key
    val ga: sAnswer = getAnswerFromGA(url)
    val result: StringBuilder = new StringBuilder
    import scala.collection.JavaConversions._
    for (iter1 <- ga.values) {
      import scala.collection.JavaConversions._
      for (iter2 <- iter1) {
        result.append(iter2)
        result.append(" ")
      }
      result.append("\n")
    }
    var indexR: Int = result.indexOf("\u20BD")
    while (indexR != -1) {
      result.setCharAt(indexR, 'р')
      indexR = result.indexOf("\u20BD")
    }
    result.toString
  }

  private[tRussianBank] def getAllUser: sAnswer = {
    val docId: String = conf.getString("docId")
    val key: String = conf.getString("key")
    val url: String = "https://sheets.googleapis.com/v4/spreadsheets/" + docId + "/values/%D0%A3%D1%87%D0%B0%D1%81%D1%82%D0%BD%D0%B8%D0%BA%D0%B8!A2%3AM100?key=" + key
    getAnswerFromGA(url)
  }

  private[tRussianBank] def getCardsInfo: sAnswer = {
    val docId: String = conf.getString("docId")
    val key: String = conf.getString("key")
    val url: String = "https://sheets.googleapis.com/v4/spreadsheets/" + docId + "/values/%D0%94%D0%B5%D1%80%D0%B6%D0%B0%D1%82%D0%B5%D0%BB%D0%B8!A2%3AB23?key=" + key
    getAnswerFromGA(url)
  }

  private def getAnswerFromGA(uri: String): sAnswer = {
    val client: HttpClient = HttpClientBuilder.create.build
    val request: HttpGet = new HttpGet(uri)
    var response: HttpResponse = null
    try {
      response = client.execute(request)
    }
    catch {
      case e: IOException => {
        e.printStackTrace()
      }
    }
    var rd: BufferedReader = null
    try {
      rd = new BufferedReader(new InputStreamReader(response.getEntity.getContent))
    }
    catch {
      case e: IOException => {
        e.printStackTrace()
      }
    }
    val result: StringBuilder = new StringBuilder
    var line: String = ""
    try
        while ((line = rd.readLine) != null) result.append(line)

    catch {
      case e: IOException => {
        e.printStackTrace()
      }
    }
    {
      val om: ObjectMapper = new ObjectMapper
      var ga: gaAnswer = null
      try {
        ga = om.readValue(result.toString, classOf[gaAnswer])
      }
      catch {
        case e: IOException => {
          e.printStackTrace()
        }
      }
      new sAnswer(ga.values)
    }
  }
}

