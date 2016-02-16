package models

import models.State.State


/**
  * Created by peanut on 05/02/16.
  */


object State extends Enumeration{
  type State = Value
  val Okay = Value("Okay")
  val Info = Value("Info")
  val Warn = Value("Warn")
  val Danger = Value("Danger")
}
trait DFCheck{
  val analysis:PageAnalysis
  def shouldPerformCheck:Boolean
  def performCheck()
  def getTitle:String
  def getDescription:String
  def getResult:String
  def POC:String = ???
  def getState:State.State

}


class ElectosDefaultLoginCheck(override val analysis:PageAnalysis)    extends DFCheck{
  override def shouldPerformCheck: Boolean = {
    analysis.pageBeforeJS.contains("ElectosStyles") || analysis.pageAfterJS.contains("ElectosStyles")
  }
  var hasDefaultElectosPassword=false
  override def performCheck(): Unit = {

    val electosUrl = analysis.url +"/Electos/"
    val electosAnalysis = PageAnalysis.getAnalysis(electosUrl)
    if (electosAnalysis.pageBeforeJS.contains("The default user account (username:admin, password:admin) ")) hasDefaultElectosPassword = true

  }
  override def getDescription: String = "Has the default login been changed?"

  override def getResult: String = {
    if (hasDefaultElectosPassword) "Default password for Electos login is set"
    else "Okay"
  }

  override def getTitle: String = "Check for default login"

  override def getState = {
    if (hasDefaultElectosPassword) State.Danger
    else State.Okay
  }
}


class SitemanagerDefaultLoginCheck(override val analysis:PageAnalysis)    extends DFCheck{
  override def shouldPerformCheck: Boolean = {
    analysis.pageBeforeJS.contains("ElectosStyles") || analysis.pageAfterJS.contains("ElectosStyles")
  }
  var hasDefaultSitemanagerPassword=false
  override def performCheck(): Unit = {

    val sitemanagerUrl = analysis.url +"/SiteManager/"
    val sitemanagerAnalysis = PageAnalysis.getAnalysis(sitemanagerUrl)
    if (sitemanagerAnalysis.pageBeforeJS.contains("The default user account (username:admin, password:admin) ")) hasDefaultSitemanagerPassword = true
    println(sitemanagerAnalysis.pageBeforeJS)

  }

  override def getDescription: String = "Has the default login been changed?"

  override def getResult: String = {
    if (hasDefaultSitemanagerPassword) "Default password for Sitemanager login is set"
    else "Okay"
  }

  override def getTitle: String = "Check for default login"

  override def getState = {
    if (hasDefaultSitemanagerPassword) State.Danger
    else State.Okay
  }
}

class ElectosStudioAccessible(override val analysis:PageAnalysis)    extends DFCheck{
  override def shouldPerformCheck: Boolean = {
    analysis.pageBeforeJS.contains("ElectosStyles") || analysis.pageAfterJS.contains("ElectosStyles")
  }
  var hasElectosAccessible = false
  override def performCheck(): Unit = {

    val electosUrl = analysis.url +"/Electos/"
    val electosAnalysis = PageAnalysis.getAnalysis(electosUrl)
    if (electosAnalysis.pageBeforeJS.contains("<form action=\"default.asp\" method=\"post\" id=\"login\">")) hasElectosAccessible = true

  }

  override def getDescription: String = "Has the default login been changed?"

  override def getResult: String = {
    if (hasElectosAccessible)" Default Electos Studio is accessible"
    else "Default Electos Studio is not accessible"
  }

  override def getTitle: String = "Check for default login"
  override def getState = {
    if (hasElectosAccessible) State.Warn
    else State.Okay
  }
}


class SitemanagerAccessible(override val analysis:PageAnalysis)    extends DFCheck{
  override def shouldPerformCheck: Boolean = {
    analysis.pageBeforeJS.contains("ElectosStyles") || analysis.pageAfterJS.contains("ElectosStyles")
  }
  var hasSitemanagerAccessible = false
  override def performCheck(): Unit = {

    val sitemanagerUrl = analysis.url +"/SiteManager/"
    val sitemanagerAnalysis = PageAnalysis.getAnalysis(sitemanagerUrl)
    if (sitemanagerAnalysis.pageBeforeJS.contains("<form action=\"default.asp\" method=\"post\" id=\"login\">")) hasSitemanagerAccessible = true
    println(sitemanagerAnalysis.pageBeforeJS)

  }

  override def getDescription: String = "Has the default login been changed?"

  override def getResult: String = {
    if (hasSitemanagerAccessible)  "Sitemanager is accessible"
    else "Sitemanager is not accessible"
  }

  override def getTitle: String = "Check for default login"

  override def getState = {
    if (hasSitemanagerAccessible) State.Warn
    else State.Okay
  }
}

class WSOArbitraryCodeExecutionCheck(override val analysis: PageAnalysis) extends DFCheck{

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
  override def getState = {
    if (dfVersion.toFloat<18.0) State.Danger
    else State.Okay
  }

}
