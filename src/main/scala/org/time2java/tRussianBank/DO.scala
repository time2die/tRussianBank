package org.time2java.tRussianBank

/**
  * Created by time2die on 07.01.17.
  */

trait Account {
  val name: String
  val currentDeb: Double
  val returnDate: String
}

case class PureAccount(name: String,
                       currentDeb: Double,
                       returnDate: String) extends Account

case class FullAccount(name: String,
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
                       hasLastMonthsPays: Boolean,
                       hasCurrentMonthsPays: Boolean) extends Account {

  override def toString: String = {
    val sb: StringBuffer = new StringBuffer
    try {
      sb.append(this.name)
      sb.append("\nВсего взносов: " + this.paymentNum)
      sb.append("\nНа сумму: " + this.paymentSum)
      sb.append("\nx5: " + this.paymentSum.toInt*5)
      sb.append("\nВсего займов: " + this.debtCount)
      if ("" != this.returnDate) {
        sb.append("\nСейчас должен: " + this.currentDeb)
        sb.append("\nДата возврата: " + this.returnDate)
      }
      sb.append("\nДосрочных погашений: " + (if (0 == this.earlyReturn) "нет" else this.earlyReturn))
      sb.append("\nПросрочек: " + (if (0 == this.delayReturn) "нет" else this.delayReturn))

      def getBooleanValueFromGAPI(value: Boolean): String = if (value) "уплачено" else "не уплачено"

      sb.append("\nВзносы за прошедшие 3 месяца: " + getBooleanValueFromGAPI(this.hasLastMonthsPays))
      sb.append("\nВзносы за текущий месяц: " + getBooleanValueFromGAPI(this.hasCurrentMonthsPays))
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
    }
    sb.toString
  }
}

case class Answer(values: List[List[String]])
