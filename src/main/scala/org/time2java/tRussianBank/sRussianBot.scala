package org.time2java.tRussianBank

import com.typesafe.config.{Config, ConfigFactory}
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.{ApiContextInitializer, TelegramBotsApi}

/**
  * Created by time2die on 05.01.17
  */
object Main {
  def main(args: Array[String]): Unit = {
    ApiContextInitializer.init()
    new TelegramBotsApi().registerBot(RussianBot)
  }
}

object RussianBot extends RussianBot

class RussianBot extends TelegramLongPollingBot {
  private[tRussianBank] val conf: Config = ConfigFactory.load

  def onUpdateReceived(update: Update) {
    new ComandProcessor(update, conf, this, convertGAtoAccount(NGA.getAllUser))
  }

  def convertGAtoAccount(ga: Answer): List[Account] = {
    ga.values.map {
      case name :: tgId :: vkId :: city :: paymentNum :: paymentSum :: debtCount :: currentDeb :: returnDate :: earlyReturn :: delayReturn :: hasLastMonthsPays :: hasCurrentMonthsPays :: xs
      => Account(name,
        tgId,
        vkId,
        city,
        paymentNum.toInt,
        strToD(paymentSum),
        strToInt(debtCount),
        strToD(currentDeb),
        returnDate,
        strToInt(earlyReturn),
        strToInt(delayReturn),
        strToB(hasLastMonthsPays),
        strToB(hasCurrentMonthsPays))
    }
  }

  def strToInt(x: String): Integer = if ("" == x) 0 else x.toInt

  def strToD(x: String): Double = x.replace(',', '.').toDouble

  def strToB(x: String): Boolean = "" == x || "0" == x

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

  override def getBotUsername: String = "tRussianBank"

  override def getBotToken: String = conf.getString("tgBotKey")
}

