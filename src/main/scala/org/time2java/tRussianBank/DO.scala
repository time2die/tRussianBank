package org.time2java.tRussianBank

/**
  * Created by time2die on 07.01.17.
  */
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
      sb.append("\nВзносы за текущий месяц: " + getBooleanValueFromGAPI(this.hasCurrentMounthsPays))
    }
    catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    sb.toString
  }
}

case class sAnswer(val values: java.util.List[java.util.List[String]])