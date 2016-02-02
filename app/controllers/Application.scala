package controllers

import com.gargoylesoftware.htmlunit.{BrowserVersion, NicelyResynchronizingAjaxController, WebClient}
import com.gargoylesoftware.htmlunit.html.HtmlPage
import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import play.api.db._

object Application extends Controller {


  def hello = Action{
    val webClient = new WebClient(BrowserVersion.FIREFOX_38)
    
    webClient.getOptions().setThrowExceptionOnScriptError(false)
    //webClient.setThrowExceptionOnScriptError(false);
    webClient.setJavaScriptTimeout(10000)
      //webClient.setJavaScriptEnabled(true);
    webClient.getOptions().setJavaScriptEnabled(true)
      webClient.setAjaxController(new NicelyResynchronizingAjaxController())
           
      
        val page:HtmlPage = webClient.getPage("http://demo.dataaccess.eu/weborder")
        val pageAsXml = page.asXml()
        val pageAsText = page.asText()
    webClient.waitForBackgroundJavaScript(3000)
    Ok("test" +pageAsXml + "<br><br>"+ pageAsXml +"<br><br>" + page.getHtmlElementById("viewport").asXml())
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
