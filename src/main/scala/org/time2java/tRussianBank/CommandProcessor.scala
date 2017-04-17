package org.time2java.tRussianBank

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update

/**
  * Created by time2die on 07.01.17.
  */

object CommandProcessor {
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
  val logger = LoggerFactory.getLogger("errors")
}


class CommandProcessor(update: Update, conf: Config, bot: RussianBot, accounts: List[Account]) {
  if (!(userHasRights || isMainChatRoom)) sendMessage("У вас нет прав")
  else if (updateStartWithCommand("/status")) processStatusCommand()
  else if (updateStartWithCommand("/search")) processSearchOperation()
  else if (updateStartWithCommand("/debts")) processDebtsCommand()
  else if (updateStartWithCommand("/cards")) processCardsCommand()

  else if (updateStartWithCommand("/rules")) sendMessage("Правила работы кассы\n" + conf.getString("rules"))
  else if (updateStartWithCommand("/aboutme")) processAboutMe()
  else if (updateStartWithCommand("/shout")) processShout()


  def processAboutMe() {
    accounts.filter(filterByTGid) match {
      case user :: nil => sendMessage(user.toString())
      case _ => processIdMessage()
    }
  }

  def processShout() {18
    val text = update.getMessage.getText.split(" ").tail.mkString(" ")
    if (text.isEmpty) {
      return
    }

    val userId = update.getMessage.getFrom.getId
    if (isAdmin(userId)) {
//      List("69711013123").foreach(userId => sendTextToUser(text, userId))
            accounts.filter(_.tgId.isEmpty == false).foreach(user => sendTextToUser(text, user.tgId))
    }
  }

  def isMainChatRoom = update.getMessage.getChatId == -29036710

  def isAdmin(userId: Integer): Boolean = {
    import collection.JavaConversions._
    val admins = conf.getStringList("admins").toList
    admins.contains(userId + "")
  }


  def processStatusCommand() {
    val values = NGA.getStatus().values

    val result: StringBuilder = new StringBuilder
    for (iter1 <- values) {
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

    sendMessage(result.toString())
  }

  def processCardsCommand() {
    val ga: Answer = NGA.getCardHolder()
    var number: String = ga.values.head.head
    var summ: String = ga.values.head(1)
    var city: String = ga.values(2).head
    var result: String = ""
    result += "номер карты: " + number
    result += "\n"
    result += "сумма на карте: " + summ
    result += "\n"
    result += "город: " + city
    number = ga.values(17).head
    summ = ga.values(17)(1)
    city = ga.values(19).head
    if ("" != number) {
      result += "\n\nномер карты: " + number
      result += "\n"
      result += "сумма на карте: " + summ
      result += "\n"
      result += "город: " + city
    }
    sendMessage(result)
  }

  def processSearchOperation() {
    val text: String = try {
      update.getMessage.getText.split(" ")(1).toLowerCase.replace('ё', 'е')
    }
    catch {
      case ex: ArrayIndexOutOfBoundsException => {
        return sendMessage("Следует указать кого вы ищите.\nПример работы: /search урбанист")
      }
    }

    val searchResult: List[Account] = accounts.filter(filterByName(text))

    def needProcessPositiveCase(): Boolean = {
      if (searchResult.isEmpty)
        sendMessage("Совпадений нет")
      else if (searchResult.size > 1)
        sendMessage("Количество совпадений: " + searchResult.size + "\nИспользуйте другой запрос")
      true
    }

    if (needProcessPositiveCase())
      sendMessage(searchResult.head.toString)
  }

  def processIdMessage() {
    val message = update.getMessage
    val fromId = message.getFrom.getId
    val fName = message.getFrom.getFirstName
    val sName = message.getFrom.getLastName

    val result = s"fromId:$fromId from:$fName $sName"


    if (fromId == message.getChatId) {
      sendMessage("Информации о этом пользователи пока нет в базе, все необходмые данные были отправлены держателю. Подождите, пожалуйста, пока их обработают")
    } else {
      sendMessage("Мы вас запомнили. Попробуйте позже.")
    }

    sendTextToAdmin("aboutme: " + result)
  }


  def processDebtsCommand() {
    val ga: Answer = NGA.getAllUser()
    val searchResult = accounts.filter(filterByDebs).sortWith(filterAccount)
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
    sendMessage(sb.toString)
  }

  def filterAccount(f: Account, s: Account): Boolean = {
    val fDate = LocalDate.parse(f.returnDate, CommandProcessor.dateFormatter)
    val sDate = LocalDate.parse(s.returnDate, CommandProcessor.dateFormatter)

    if (fDate.compareTo(sDate) != 0) fDate.isBefore(sDate)
    else if (f.currentDeb.compareTo(s.currentDeb) >= 0) false else true
  }

  def sendMessage(text: String) = {
    this.bot.sendMessage(this.update, text)
    CommandProcessor.logger.debug(s"send: $text to ${this.update.getMessage.getChatId}")
  }

  def sendTextToAdmin(text: String) {
    sendTextToUser(text, "77960859l")
  }

  def sendTextToUser(text: String, userId: String) {
    try {
      val log = new SendMessage()
      log.setChatId(userId)
      log.setText(text)
      bot.sendMessage(log)
      CommandProcessor.logger.debug(s"send: $text to $userId")
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        CommandProcessor.logger.error(e.getLocalizedMessage)
      case any => CommandProcessor.logger.error(s"error:$any")
    }
  }

  def userHasRights: Boolean = accounts.count(filterByTGid) == 1

  def filterByDebs: (Account => Boolean) = _.currentDeb > 0

  def filterByTGid: (Account => Boolean) = _.tgId == update.getMessage.getFrom.getId.toString

  def filterByName(searchText: String): (Account => Boolean) = _.name.toLowerCase().replace('ё', 'е').indexOf(searchText) != -1

  def updateStartWithCommand(message: String): Boolean =
    update.hasMessage && update.getMessage.hasText && update.getMessage.getText.toLowerCase.startsWith(message)
}