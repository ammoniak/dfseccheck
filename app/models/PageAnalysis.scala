package models

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.{WebResponse, WebRequest, WebClient}
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper

import scala.collection.mutable
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
  def POC:String = ???
}

class ElectosDefaultLoginCheck(override val analysis:PageAnalysis)    extends DFDheck{
  override def shouldPerformCheck: Boolean = {
    analysis.pageBeforeJS.contains("ElectosStyles") || analysis.pageAfterJS.contains("ElectosStyles")
  }
  var hasDefaultSitemanagerPassword=false
  var hasSitemanagerAccessible = false
  var hasDefaultElectosPassword=false
  var hasElectosAccessible = false
  override def performCheck(): Unit = {

    val sitemanagerUrl = analysis.url +"/SiteManager/"
    val sitemanagerAnalysis = PageAnalysis.getAnalysis(sitemanagerUrl)
    if (sitemanagerAnalysis.pageBeforeJS.contains("<form action=\"default.asp\" method=\"post\" id=\"login\">")) hasSitemanagerAccessible = true
    if (sitemanagerAnalysis.pageBeforeJS.contains("The default user account (username:admin, password:admin) ")) hasDefaultSitemanagerPassword = true
    val electosUrl = analysis.url +"/Electos/"
    val electosAnalysis = PageAnalysis.getAnalysis(electosUrl)
    if (electosAnalysis.pageBeforeJS.contains("<form action=\"default.asp\" method=\"post\" id=\"login\">")) hasElectosAccessible = true
    if (electosAnalysis.pageBeforeJS.contains("The default user account (username:admin, password:admin) ")) hasDefaultElectosPassword = true
    println(sitemanagerAnalysis.pageBeforeJS)

  }

  override def getDescription: String = "Has the default login been changed?"

  override def getResult: String = {
    var result=""
    if (hasDefaultElectosPassword) result += " Default password for Electos login is set"
    if (hasDefaultSitemanagerPassword) result += " Default password for Sitemanager login is set"
    if (hasElectosAccessible) result += " Default Electos Studio is accessible"
    if (hasSitemanagerAccessible) result += " Sitemanager is accessible"
    if (result.isEmpty) result = "Okay"
    result
  }

  override def getTitle: String = "Check for default login"
}

class WSOArbitraryCodeExecutionCheck(override val analysis: PageAnalysis) extends DFDheck{

  override def shouldPerformCheck: Boolean = {
    analysis.networkConnections.exists(con=>con.getPath.contains(".wso"))
  }
  val dfVersion = analysis.networkConnections.find(_.getPath.contains(".wso")) match {
    case Some(connection) => connection.getHeader.getOrElse("WebService","0")
    case _ => "0"
  }
  override def performCheck(): Unit = {
  }

  override def getDescription: String = "Is this WebApp vulnerable to arbitrary code execution?"

  override def getResult: String =  dfVersion.toFloat match{
    case version if version> 0 && version < 18.0 => "Vulnerable"
    case 0 => "Unknown"
    case _ => "Ok"
  } //TODO: implement! (1. check if version is vurlnerable, 2. check if public function that is vuln. exists)

  override def getTitle: String = "Arbitrary Code Execution"

}

class PageAnalysis(val url:String,val pageBeforeJS:String, val pageAfterJS:String, val networkConnections:Seq[Connection]) {
  val checks = new mutable.ListBuffer[DFDheck]
  checks += new WSOArbitraryCodeExecutionCheck(this)
  checks += new ElectosDefaultLoginCheck(this)
  def performChecks(): Unit ={
    checks.foreach(_.performCheck())
  }
  def getResults={
    //TODO: make it lazy, etc
    performChecks()
    checks.filter(_.shouldPerformCheck).map(check => (check.getTitle,check.getDescription,check.getResult) )
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