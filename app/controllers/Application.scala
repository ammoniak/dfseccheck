package controllers

import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper
import com.gargoylesoftware.htmlunit._
import com.gargoylesoftware.htmlunit.html.{DomElement, HtmlPage}
import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import collection.JavaConversions._
import play.api.db._

import scala.collection.mutable.ListBuffer

object Application extends Controller {


  def hello = Action{
    val webClient = new WebClient()

    val connection = new WebConnectionWrapper(webClient.getWebConnection){
      val files = new ListBuffer[(String,String,Seq[(String,String)])]
      override def getResponse(request: WebRequest): WebResponse = {
        println(request.getUrl.toString)

        val response = super.getResponse(request)
        val headers:Seq[(String,String)] = response.getResponseHeaders.toList.map(nvp  => (nvp.getName,nvp.getValue))
        files.+=:(request.getUrl.toString,request.getUrl.getPath,headers)
        response
      }
    }
    webClient.setWebConnection(connection)
    /*
    webClient.getOptions().setThrowExceptionOnScriptError(false)
    //webClient.setThrowExceptionOnScriptError(false);
    webClient.setJavaScriptTimeout(10000)
      //webClient.setJavaScriptEnabled(true);
    webClient.getOptions().setJavaScriptEnabled(true)
      webClient.setAjaxController(new NicelyResynchronizingAjaxController())
      */
        val page:HtmlPage = webClient.getPage("http://demo.dataaccess.eu/weborder")
        val pageAsXml = page.asXml()
        val pageAsText = page.asText()
    webClient.waitForBackgroundJavaScript(1000)
    webClient.getJavaScriptEngine.pumpEventLoop(1000)
    webClient.waitForBackgroundJavaScript(1000)
    Thread.sleep(2000)
    val viewport:DomElement = page.getHtmlElementById("viewport")
    Ok(views.html.hello("test" +pageAsXml + "<br><br>"+ pageAsXml +"<br><br>" + page.asXml(), connection.files))
  }

  def index = Action {
    Ok(views.html.index(null))
  }

  def db = Action {
    var out = ""
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)")
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())")

      val rs = stmt.executeQuery("SELECT tick FROM ticks")

      while (rs.next) {
        out += "Read from DB: " + rs.getTimestamp("tick") + "\n"
      }
    } finally {
      conn.close()
    }
    Ok(out)
  }
}
