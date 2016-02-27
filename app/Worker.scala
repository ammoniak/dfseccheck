import akka.actor.Actor
import akka.actor.Actor.Receive
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._

import play.api.Logger
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

object Worker extends GlobalSettings {

  Akka.system.scheduler.schedule(0 seconds, 30 minutes, testActor, "tick")
}

class MyActor extends Actor{
  override def receive: Receive = {
    case "tick" => println("execute!")
    case _ => println("ups?")
  }
}