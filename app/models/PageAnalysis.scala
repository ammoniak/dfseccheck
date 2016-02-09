package models

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.{WebResponse, WebRequest, WebClient}
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper

import scala.collection.mutable.ListBuffer

import collection.JavaConversions._

/**
  * Created by peanut on 05/02/16.
  */

trait DFDheck{
  val analysis:PageAnalysis
  def shouldPerformCheck:Boolean
  def performCheck()
  def getTitle:String
  def getDescription:String
  def getResult:String
  def POC:String
}

class WSOArbitraryCodeExecutionCheck(override val analysis: PageAnalysis) extends DFDheck{

  override def shouldPerformCheck: Boolean = {
    analysis.networkConnections.exists(con=>con.getPath.contains(".wso"))
  }

  override def performCheck(): Unit = {

  }

  override def getDescription: String = "Is this WebApp vulnerable to arbitrary code execution?"

  override def getResult: String = "Maybe" //TODO: implement! (1. check if version is vurlnerable, 2. check if public function that is vuln. exists)

  override def getTitle: String = "Arbitrary Code Execution"

  override def POC = ???
}

class PageAnalysis(val url:String,val pageBeforeJS:String, val pageAfterJS:String, val networkConnections:Seq[Connection]) {
  def performChecks(): Unit ={

  }
}
object PageAnalysis{
  def getAnalysis(url:String):PageAnalysis={

    val webClient = new WebClient()

    val connection = new WebConnectionWrapper(webClient.getWebConnection){
      val files = new ListBuffer[Connection]
      override def getResponse(request: WebRequest): WebResponse = {
        val response = super.getResponse(request)
        files.+=:(Connection(request,response))
        response
      }
    }
    webClient.setWebConnection(connection)
    webClient.getOptions.setThrowExceptionOnFailingStatusCode(false)
    webClient.getOptions.setThrowExceptionOnScriptError(false)
    /*
    webClient.setJavaScriptTimeout(10000)
      //webClient.setJavaScriptEnabled(true);
    webClient.getOptions().setJavaScriptEnabled(true)
      webClient.setAjaxController(new NicelyResynchronizingAjaxController())
      */
    //val page:HtmlPage = webClient.getPage("http://demo.dataaccess.eu/weborder")
    val page:HtmlPage = webClient.getPage(url)
    val pageAsXml = page.asXml()
    webClient.waitForBackgroundJavaScript(1000)
    webClient.getJavaScriptEngine.pumpEventLoop(1000)
    webClient.waitForBackgroundJavaScript(1000)
    Thread.sleep(2000)

    new PageAnalysis(url,pageAsXml,page.asXml(), connection.files.reverse)
  }

}

case class Connection(request:WebRequest,response:WebResponse){
  def getStatus = response.getStatusCode
  def getUrl = request.getUrl.toString
  def getPath = request.getUrl.getPath
  def getHeader:Map[String,String] = response.getResponseHeaders.toList.map(nvp  => (nvp.getName,nvp.getValue)).toMap
}