package org.time2java.tRussianBank

import com.typesafe.config.{Config, ConfigFactory}
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.{ApiContextInitializer, TelegramBotsApi}

import scala.collection.JavaConversions._


/**
  * Created by time2die on 05.01.17
  */

object Main {
  def main(args: Array[String]): Unit = {
    ApiContextInitializer.init()
    new TelegramBotsApi().registerBot(new RussianBot)
  }
}

case class sAnswer(val values: java.util.List[java.util.List[String]])

case class Account(name: String,
                   tgId: String,
                   vkID: String,
                   city: String,
                   paymentNum: Int,
                   paymentSum: Double,
                   debtCount: Int,
                   currentDeb: Double,
                   returnDate: String,
                   earlyReturn: Integer,
                   delayReturn: Integer,
                   hasLastMounthsPays: Boolean,
                   hasCurrentMounthsPays: Boolean) {
  override def toString(): String = {
    val sb: StringBuffer = new StringBuffer
    try {
      sb.append(this.name)
      sb.append("\nВсего взносов: " + this.paymentNum)
      sb.append("\nНа сумму: " + this.paymentSum)
      sb.append("\nВсего займов: " + this.debtCount)
      if ("" != this.returnDate) {
        sb.append("\nСейчас должен: " + this.currentDeb)
        sb.append("\nДата возврата: " + this.returnDate)
      }
      sb.append("\nДосрочных погашений: " + (if (0 == this.earlyReturn) "нет" else this.earlyReturn))
      sb.append("\nПросрочек: " + (if (0 == this.delayReturn) "нет" else this.delayReturn))

      def getBooleanValueFromGAPI(value: Boolean): String = if (value) "уплачено" else "не уплачено"

      sb.append("\nВзносы за прошедшие 3 месяца: " + getBooleanValueFromGAPI(this.hasLastMounthsPays))
      sb.append("\nВзносы за текущий месяц: " + getBooleanValueFromGAPI(this.hasLastMounthsPays))
    }
    catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    sb.toString
  }
}


class RussianBot extends TelegramLongPollingBot {
  private[tRussianBank] val conf: Config = ConfigFactory.load

  def onUpdateReceived(update: Update) {
    if (updateStartWithCommand(update, "/status") && userHasRights(update)) sendMessage(update, GoogleApiClient.getStatus())
    else if (updateHasCommand(update, "/search урбанист") && userHasRights(update)) sendMessage(update, "Ой, всё")
    else if (updateStartWithCommand(update, "/search") && userHasRights(update)) processSearchOperation(update)
    else if (updateStartWithCommand(update, "/debts") && userHasRights(update)) processDebtsCommand(update)
    else if (updateStartWithCommand(update, "/cards") && userHasRights(update)) processCardsCommand(update)
    else if (updateStartWithCommand(update, "/rules") && userHasRights(update)) sendMessage(update, "Правила работы кассы\n" + conf.getString("rules"))
    else if (updateStartWithCommand(update, "/fullstats") && userHasRights(update)) sendMessage(update, "Учет кассы\n" + conf.getString("fullstats"))
    else if (updateStartWithCommand(update, "/aboutme")) processAboutMe(update)
    else if (updateStartWithCommand(update, "/myid")) processIdMessage(update, this)
    else {
    }
  }

  def processAboutMe(update: Update): Unit = {
    val user = convertGAtoAcconut(GoogleApiClient.getAllUser).filter(_.tgId == update.getMessage.getFrom.getId.toString)
    user match {
      case user :: nil => sendMessage(update, user.toString())
      case _ => sendMessage(update, "Информации об этом пользователи пока нету в базе")
    }
  }

  private def processCardsCommand(update: Update) {
    val ga: sAnswer = GoogleApiClient.getCardsInfo
    var number: String = ga.values(0).get(0)
    var summ: String = ga.values.get(0).get(1)
    var city: String = ga.values.get(2).get(0)
    var result: String = ""
    result += "номер карты: " + number
    result += "\n"
    result += "сумма на карте: " + summ
    result += "\n"
    result += "город: " + city
    number = ga.values.get(17).get(0)
    summ = ga.values.get(17).get(1)
    city = ga.values.get(19).get(0)
    if ("" != number) {
      result += "\n\nномер карты: " + number
      result += "\n"
      result += "сумма на карте: " + summ
      result += "\n"
      result += "город: " + city
    }
    sendMessage(update, result)
  }

  def processSearchOperation(update: Update) {
    val text: String = try {
      update.getMessage.getText.split(" ")(1).toLowerCase.replace('ё', 'е')
    }
    catch {
      case ex: ArrayIndexOutOfBoundsException => {
        return sendMessage(update, "Следует указать кого вы ищите.\nПример работы: /search урбанист")
      }
    }

    val searchResult: List[Account] = search(GoogleApiClient.getAllUser, text)

    def needProcessPositiveCase(): Boolean = {
      if (searchResult.size == 0) {
        sendMessage(update, "Совпадений нет")
        false
      }
      else if (searchResult.size > 1) {
        sendMessage(update, "Количество совпадений: " + searchResult.size + "\nИспользуйте другой запрос")
        false
      }
      true
    }

    if (needProcessPositiveCase())
      sendMessage(update, searchResult.head.toString)
  }


  private def search(ga: sAnswer, text: String): List[Account] = {
    convertGAtoAcconut(ga).filter(_.name.toLowerCase().indexOf(text) != -1)
  }

  private[tRussianBank] def processElseVariant(update: Update) {
    if (userHasRights(update)) sendMessage(update, "Я пока так не умею" + update.getMessage.getText + "<")
    else sendMessage(update, "У вас не достаточно прав для выполнения запроса")
  }

  def sendMessage(update: Update, text: String) {
    val message: SendMessage = new SendMessage().setChatId(update.getMessage.getChatId).setText(text)
    try {
      sendMessage(message)
    }
    catch {
      case e: TelegramApiException => {
        e.printStackTrace()
      }
    }
  }

  def processIdMessage(update: Update, bot: TelegramLongPollingBot): Unit = {
    val message = update.getMessage
    val chatId = message.getChatId
    val fromId = message.getFrom.getId
    val fName = message.getFrom.getFirstName
    val sName = message.getFrom.getLastName
    val result = s"chatId:${chatId} from:${fName} ${sName} fromId:${fromId}"

    if (chatId == fromId) sendMessage(update, result, bot)

    //log
    val log = new SendMessage()
    log.setChatId(77960859l)
    log.setText("log: " + result)
    sendMessage(log)
  }

  def processDebtsCommand(update: Update) {
    val ga: sAnswer = GoogleApiClient.getAllUser
    val searchResult = searchDebts(ga)
    val sb: StringBuffer = new StringBuffer("")
    for (resultUser <- searchResult) {
      val v0: String = resultUser.name
      val v6: String = resultUser.currentDeb.toString
      var v7: String = resultUser.returnDate
      v7 = v7.split("\\.")(0) + "." + v7.split("\\.")(1)
      sb.append(v0)
      sb.append(": " + v6)
      sb.append("\tдо " + v7)
      sb.append("\n")
    }
    sendMessage(update, sb.toString)
  }

  private def searchDebts(ga: sAnswer): List[Account] = {
    convertGAtoAcconut(ga).filter(iter => iter.currentDeb > 0).sortBy(_.name)
  }

  def convertGAtoAcconut(ga: sAnswer): List[Account] = {
    ga.values.toList.map(iter => {
      val rawAccount = iter.toList
      def strToInt(x: String): Integer = if ("" == x) 0 else x.toInt
      def strToB(x: String): Boolean = if ("" == x) false else true
      def strToD(x: String): Double = x.replace(',', '.').toDouble
      rawAccount match {
        case name :: tgId :: vkId :: city :: paymentNum :: paymentSum :: debtCount :: currentDeb :: returnDate :: earlyReturn :: delayReturn :: hasLastMounthsPays :: hasCurrentMounthsPays :: xs
        => Account(name, tgId, vkId, city, paymentNum.toInt, strToD(paymentSum), strToInt(debtCount), strToD(currentDeb), returnDate, strToInt(earlyReturn), strToInt(delayReturn), strToB(hasLastMounthsPays), strToB(hasCurrentMounthsPays))
      }
    })
  }

  def sendMessage(update: Update, text: String, bot: TelegramLongPollingBot) {
    val message: SendMessage = new SendMessage().setChatId(update.getMessage.getChatId).setText(text)
    try {
      bot.sendMessage(message)
    }
    catch {
      case e: TelegramApiException => {
        e.printStackTrace()
      }
    }
  }

  def updateHasCommand(update: Update, message: String): Boolean =
    update.hasMessage && update.getMessage.hasText && message == update.getMessage.getText.trim.toLowerCase

  def updateStartWithCommand(update: Update, message: String): Boolean =
    update.hasMessage && update.getMessage.hasText && update.getMessage.getText.toLowerCase.startsWith(message)

  def userHasRights(update: Update): Boolean =
    (update.getMessage.getChatId == 69711013) || (update.getMessage.getChatId == 77960859) || (update.getMessage.getChatId == -29036710)

  override def getBotUsername: String = "tRussianBank"

  override def getBotToken: String = conf.getString("tgBotKey")
}
