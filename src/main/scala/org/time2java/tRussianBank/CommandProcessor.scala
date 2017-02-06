package org.time2java.tRussianBank

import com.typesafe.config.Config
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update

/**
  * Created by time2die on 07.01.17.
  */
class ComandProcessor(update: Update, conf: Config, bot: RussianBot, accounts: List[Account]) {
  if (!(userHasRights || update.getMessage.getChatId == -29036710))
    sendMessage("У вас нет прав")
  else if (updateStartWithCommand(update, "/status")) processStatusCommand(update)

  //  else if (updateStartWithCommand(update, "/search")) processSearchOperation(update)
  //  else if (updateStartWithCommand(update, "/debts")) processDebtsCommand(update)
  else if (updateStartWithCommand(update, "/cards")) processCardsCommand(update)

  //  else if (updateStartWithCommand(update, "/rules")) sendMessage("Правила работы кассы\n" + conf.getString("rules"))
  //  else if (updateStartWithCommand(update, "/aboutme")) processAboutMe(update)
  //  else {
  //    processElseVariant(update)
  //  }

  //  def processAboutMe(update: Update): Unit = {
  //    accounts.filter(filterByTGid) match {
  //      case user :: nil => sendMessage(user.toString())
  //      case _ => processIdMessage(update)
  //    }
  //  }
  //

  def processStatusCommand(update: Update) {
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

  def processCardsCommand(update: Update) {
    val ga: Answer = NGA.getCardHolder()
    var number: String = ga.values(0)(0)
    var summ: String = ga.values(0)(1)
    var city: String = ga.values(2)(0)
    var result: String = ""
    result += "номер карты: " + number
    result += "\n"
    result += "сумма на карте: " + summ
    result += "\n"
    result += "город: " + city
    number = ga.values(17)(0)
    summ = ga.values(17)(1)
    city = ga.values(19)(0)
    if ("" != number) {
      result += "\n\nномер карты: " + number
      result += "\n"
      result += "сумма на карте: " + summ
      result += "\n"
      result += "город: " + city
    }
    sendMessage(result)
  }

  //
  //  def processSearchOperation(update: Update) {
  //    val text: String = try {
  //      update.getMessage.getText.split(" ")(1).toLowerCase.replace('ё', 'е')
  //    }
  //    catch {
  //      case ex: ArrayIndexOutOfBoundsException => {
  //        return sendMessage("Следует указать кого вы ищите.\nПример работы: /search урбанист")
  //      }
  //    }
  //
  //    val searchResult: List[Account] = accounts.filter(filterByName(text))
  //
  //    def needProcessPositiveCase(): Boolean = {
  //      if (searchResult.size == 0)
  //        sendMessage("Совпадений нет")
  //      else if (searchResult.size > 1)
  //        sendMessage("Количество совпадений: " + searchResult.size + "\nИспользуйте другой запрос")
  //      true
  //    }
  //
  //    if (needProcessPositiveCase())
  //      sendMessage(searchResult.head.toString)
  //  }
  //
  //  private[tRussianBank] def processElseVariant(update: Update) {
  //    if (update.getMessage.getFrom.getId != update.getMessage.getChatId)
  //      return
  //
  //    if (userHasRights) sendMessage("Я пока так не умею" + update.getMessage.getText + "<")
  //    else sendMessage("У вас не достаточно прав для выполнения запроса")
  //  }
  //
  //  def processIdMessage(update: Update): Unit = {
  //    val message = update.getMessage
  //    val fromId = message.getFrom.getId
  //    val fName = message.getFrom.getFirstName
  //    val sName = message.getFrom.getLastName
  //
  //    val result = s"fromId:${fromId} from:${fName} ${sName}"
  //
  //    //
  //    //    if (fromId == message.getChatId) {
  //    //      sendMessage("Информации об этом пользователи пока нет в базе, все необходмые данные были отправлены держателю. Подождите, пожалуйста, пока их обработают")
  //    //    } else {
  //    //      sendMessage("Мы вас запомнили. Попробуйте позже.")
  //    //    }
  //
  //    sendTextToAdmin("aboutme: " + result)
  //  }
  //
  //  def processDebtsCommand(update: Update) {
  //    val ga: sAnswer = GoogleApiClient.getAllUser
  //    val searchResult = accounts.filter(filterByDebs).sortBy(_.name)
  //    val sb: StringBuffer = new StringBuffer("")
  //    for (resultUser <- searchResult) {
  //      val v0: String = resultUser.name
  //      val v6: String = resultUser.currentDeb.toString
  //      var v7: String = resultUser.returnDate
  //      v7 = v7.split("\\.")(0) + "." + v7.split("\\.")(1)
  //      sb.append(v0)
  //      sb.append(": " + v6)
  //      sb.append("\tдо " + v7)
  //      sb.append("\n")
  //    }
  //    sendMessage(sb.toString)
  //  }

  def sendMessage(text: String) = this.bot.sendMessage(this.update, text)

  def sendTextToAdmin(text: String): Unit = {
    val log = new SendMessage()
    log.setChatId(77960859l)
    log.setText(text)
    bot.sendMessage(log)
  }

  def filterByDebs: (Account => Boolean) = _.currentDeb > 0

  def filterByTGid: (Account => Boolean) = _.tgId == update.getMessage.getFrom.getId.toString

  def filterByName(searchText: String): (Account => Boolean) = _.name.toLowerCase().replace('ё', 'е').indexOf(searchText) != -1

  def userHasRights: Boolean = false //    accounts.filter(filterByTGid).size == 1

  def updateStartWithCommand(update: Update, message: String): Boolean =
    update.hasMessage && update.getMessage.hasText && update.getMessage.getText.toLowerCase.startsWith(message)
}