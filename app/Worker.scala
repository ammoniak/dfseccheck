
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._

import play.api.Logger
import scala.concurrent.Future

import akka.actor._

import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current

object Worker extends GlobalSettings {


  override def onStart(app: Application): Unit = {
    val testActor = Akka.system.actorOf(MyActor.props)
    Akka.system.scheduler.schedule(0 seconds, 5 seconds, testActor, "tick")
  }
}

object  MyActor{
  def props = Props[MyActor]
}
class MyActor extends Actor{
  import MyActor._
  override def receive: Receive = {
    case "tick" => println("execute!")
    case _ => println("ups?")
  }
}