package org.time2java.tRussianBank

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.typesafe.config.Config
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * Created by time2die on 07.01.17.
  */


object CommandProcessor {
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
}


class CommandProcessor(update: Update, conf: Config, bot: RussianBot, accounts: List[FullAccount]) {

  if (!(userHasRights || isMainChatRoom)) processIdMessage
  else if (updateStartWithCommand("/status")) processStatusCommand()
  else if (updateStartWithCommand("/search")) processSearchOperation()
  else if (updateStartWithCommand("/debts")) processDebtsCommand()
  else if (updateStartWithCommand("/cards")) processCardsCommand()
  else if (updateStartWithCommand("/proxy")) processProxyCommand()

  else if (updateStartWithCommand("/duckList")) processDuckList()

  else if (updateStartWithCommand("/rules")) sendMessage("Правила работы кассы\n" + conf.getString("rules"))
  else if (updateStartWithCommand("/aboutme")) processAboutMe()
  else if (updateStartWithCommand("/aboutMyPayment".toLowerCase)) processAboutMyPayment()


  def processAboutMyPayment(): Unit = {
    if (isMainChatRoom) sendMessage("Этот функционал работает только в личных сообщениях")
    else {

      val payments = NGA.getAllPayments()

      val date = payments.values.head.tail.tail.tail
      val userPayments = payments.values.tail
      val user = accounts.filter(filterByTGid).head
      val userPaymentsHistory = userPayments.filter(_.head == user.name).head.tail.tail.tail
      val zipDateMoney = date.zip(userPaymentsHistory).filter(_._2 != "!")

      val text: StringBuilder = new StringBuilder("Статистика по платежам\n")
      zipDateMoney.foreach(f => {
        val when = f._1
        val howMany = f._2
        if (howMany != "") text.append(s"$when - $howMany рублей\n") else text.append(s"$when - не уплочено\n")
      })

      sendMessage(s"$text")
    }
  }

  def processAboutMe() {
    accounts.filter(filterByTGid) match {
      case user :: nil => sendMessage(user.toString())
      case _ => processIdMessage()
    }
  }


  def processDuckList(): Unit = {
    val hasRights:List[FullAccount] = accounts.filter(_.hasLastMonthsPays).filter(_.currentDeb.toInt < 0)

    val sb:StringBuilder = new StringBuilder("Следующие господа не имеют долгов и оплатили последние 3 месяца")

    hasRights.foreach{ iter =>
      sb.append(iter.name).append("\n")
    }
    sb.append("\n")

    val timeToGoAway:List[FullAccount] = accounts.filterNot(_.hasLastMonthsPays)
    sb.append("Следующие господа не платят более 3 месяцев")
    timeToGoAway.foreach{ iter =>
      sb.append(iter.name).append("\n")
    }
    sb.append("\n")
    sendMessage(sb.toString())
  }


  def isMainChatRoom = update.getMessage.getChatId == -29036710

  def isAdmin(userId: Integer): Boolean = {

    val admins = conf.getStringList("admins")
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

  def buildProxyLink():String = {
    val server = conf.getAnyRef("server").toString
    val port= conf.getAnyRef("port").toString
    val user = conf.getAnyRef("proxyUser").toString
    val pass = conf.getAnyRef("pass").toString
    s"https://t.me/socks?server=$server&port=$port&user=$user&pass=$pass"
  }

  def processProxyCommand(): Unit = {
    sendMessage(s"Нажмите на ссылку чтобы активировать прокси:\n${buildProxyLink()}")
  }

  def processCardsCommand() {
    val ga: Answer = NGA.getCardHolder()
    var number: String = ga.values.head.head
    var summ: String = ga.values.head(1)
    var city: String = ga.values(2).head
    var bankName: String = ga.values(3).head
    var systemName: String = ga.values(4).head
    var link: String = ga.values(5).head

    var result: String = ""

    result += s"номер карты: $number\n"
    result += s"сумма на карте: $summ\n"
    result += s"город: $city\n"
    result += s"карта: $bankName  $systemName"

    if (link != "") result += s"\nПополнить без комиссии можно по ссылке:\n$link"

    number = ga.values(17).head
    summ = ga.values(17)(1)
    city = ga.values(19).head
    bankName = ga.values(20).head
    systemName = ga.values(21).head

    if ("" != number) {
      result += s"\n\nномер карты: $number\n"
      result += s"сумма на карте: $summ\n"
      result += s"город: $city\n"
      result += s"карта: $bankName $systemName"
    }
    sendMessage(result)
  }

  def processSearchOperation() {
    if (isMainChatRoom) sendMessage("Лучше этим не пользоваться в групповом чате.")
    else processMessage

    def processMessage = {
      val text: Try[String] = Try {
        update.getMessage.getText.split(" ")(1).toLowerCase.replace('ё', 'е')
      }

      text match {
        case Failure(_: ArrayIndexOutOfBoundsException) => sendMessage("Следует указать кого вы ищите.")
        case Success(text: String) => searchAndSend(text)
        case _@(Failure(_) | Success(_)) => sendMessage("Свяжитесь с разработчиком.")
      }
    }

    def searchAndSend(text: String) = {
      val searchResult: List[FullAccount] = accounts.filter(filterByName(text))

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
  }

  def processIdMessage() {
    val message = update.getMessage
    val fromId = message.getFrom.getId
    val fName = message.getFrom.getFirstName
    val sName = message.getFrom.getLastName

    val result = s"fromId:$fromId from:$fName $sName\n${message.getText}"


    if (isMainChatRoom) {
      sendMessage("Информации о этом пользователи пока нет в базе, все необходмые данные были отправлены держателю. Подождите, пожалуйста, пока их обработают")
    } else {
      sendMessage("Мы вас запомнили. Дайте нам время,чтобы обработать запрос.")
    }

    sendTextToAdmin("aboutme: " + result)
  }


  def processDebtsCommand() {

    val searchResult: List[Account] = buildAccountsFromDebts(NGA.getDebsUser())

    val sb: StringBuffer = new StringBuffer("")
    for (resultUser <- searchResult) {
      val v0: String = resultUser.name
      var v6: String = resultUser.currentDeb.toString
      var v7: String = resultUser.returnDate
      //      v7 = v7.split("\\.")(0) + "." + v7.split("\\.")(1)
      v7 = v7.replace(".20", ".")

      v6 = v6.indexOf('.') match {
        case -1 => v6
        case i: Int => v6.substring(0, i)
      }


      sb.append(v0)
      sb.append(": " + v6)
      sb.append("\tдо " + v7)
      sb.append("\n")
    }
    sendMessage(sb.toString)
  }

  def getSafeDouble(in:String):Double = Try[Double]{
    in.replace(',','.').toDouble
  }.getOrElse(-1d)

  def buildAccountsFromDebts(ga: Answer): List[PureAccount] = ga.values.map(
    {
      case name :: debt :: returnDate :: tail => PureAccount(name, getSafeDouble(debt), returnDate)
      case _ => PureAccount("", 0.0, "")
    }
  ).filter(filterByDebs).sortWith(filterAccount)

  def filterAccount(f: PureAccount, s: PureAccount): Boolean = {
    val fDate = LocalDate.parse(f.returnDate, CommandProcessor.dateFormatter)
    val sDate = LocalDate.parse(s.returnDate, CommandProcessor.dateFormatter)

    if (fDate.compareTo(sDate) != 0) fDate.isBefore(sDate)
    else if (f.currentDeb.compareTo(s.currentDeb) >= 0) false else true
  }

  def sendMessage(text: String) = {
    this.bot.sendMessage(this.update, text)
  }

  def sendTextToAdmin(text: String) {
    val admins = conf.getStringList("admins").asScala
    admins.foreach(id => sendTextToUser(text, id))
  }

  def sendTextToUser(text: String, userId: String) {
    try {
      val log = new SendMessage()
      log.setChatId(userId)
      log.setText(text)
      bot.sendMessage(log)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
      case any => println(s"error:$any")
    }
  }

  def userHasRights: Boolean = accounts.count(filterByTGid) == 1

  def filterByDebs: (Account => Boolean) = _.currentDeb > 0

  def filterByTGid: (FullAccount => Boolean) = _.tgId == update.getMessage.getFrom.getId.toString

  def filterByName(searchText: String): (FullAccount => Boolean) = _.name.toLowerCase().replace('ё', 'е').indexOf(searchText) != -1

  def updateStartWithCommand(message: String): Boolean =
    update.hasMessage && update.getMessage.hasText && update.getMessage.getText.toLowerCase.startsWith(message)
}