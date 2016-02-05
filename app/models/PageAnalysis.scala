package models

/**
  * Created by peanut on 05/02/16.
  */
class PageAnalysis(val pageBEforeJS:String, val pageAfterJS:String, val networkConnections:Seq[(String,String,Map[String,String])]) {

}
