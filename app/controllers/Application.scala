package controllers

import com.yubico.client.v2.YubicoClient
import models.PageAnalysis
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import collection.JavaConversions._
import play.api.db._

import play.api.Play.current
import play.api.i18n.Messages.Implicits._


object Application extends Controller {

  def login() = Action{implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.index(null,searchForm.fill(SearchParameters("http://demo.dataaccess.eu/WebOrder/")),formWithErrors))
      },
      loginData => {
        // clientId and secretKey are retrieved from https://upgrade.yubico.com/getapikey
        val client = YubicoClient.getClient(27266, "GZ6FJc8Gl8Ff4pDHR+XLEX636mc=")

        // otp is the OTP from the YubiKey
        try {val response = client.verify(loginData.otp)
        //val msg = "OTP isOk=" + response.isOk + " publicID="  + response.getPublicId +  " "  + response.getSessioncounter
        var out = ""
        val conn = DB.getConnection()
        try {
          //val stmt = conn.createStatement
          //stmt.executeUpdate("INSERT INTO ticks VALUES (now())")
          //stmt.executeUpdate("INSERT INTO useraccount () VALUES (now())")
          if (response.isOk) {

            val prep = conn.prepareStatement("SELECT * FROM useraccount WHERE id = ?")
            prep.setString(1, response.getPublicId)
            val rs = prep.executeQuery()

            if (!rs.isBeforeFirst && response.isOk) {
              val ins = conn.prepareStatement("INSERT INTO useraccount (id,email,fullname) VALUES (?,?,?)")
              ins.setString(1, response.getPublicId)
              ins.setString(2, "test@esheep.ch")
              ins.setString(3, "RTH")
              ins.execute()
            }
            while (rs.next) {
              out += "Read from DB: " + rs.getString("id") + "\n"
            }

            println(out)
          }
        } finally {
          conn.close()
        }
          //Ok(views.html.index(msg,searchForm,loginForm)).withSession(request.session + ("public_id"->response.getPublicId))
          Redirect(routes.Application.index()).withSession(request.session + ("public_id"->response.getPublicId))
        } catch {
          case e:IllegalArgumentException => BadRequest(views.html.index(null,searchForm.fill(SearchParameters("http://demo.dataaccess.eu/WebOrder/")),loginForm))
        }

      })
  }

  def hello(url:String) = Action{
    val analysis = PageAnalysis.getAnalysis(url)
    //val viewport:DomElement = page.getHtmlElementById("viewport")
    Ok(views.html.hello(analysis))
 }

  case class LoginParameters(otp:String)
  val loginForm = Form(
    mapping(
      "otp" -> text
    )(LoginParameters.apply)(LoginParameters.unapply)
  )

  case class SearchParameters(url:String){
  }
  val searchForm = Form(
    mapping(
      "url" -> text
    )(SearchParameters.apply)(SearchParameters.unapply)
  )


  def indexForm = Action{implicit request =>
    searchForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.index(null,formWithErrors,loginForm))
      },
      userData => {
        /* binding success, you get the actual value. */
        Redirect(routes.Application.hello(userData.url))
      }
    )
  }

  def index = Action {
    Ok(views.html.index(null,searchForm.fill(SearchParameters("http://demo.dataaccess.eu/WebOrder/")),loginForm))
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
