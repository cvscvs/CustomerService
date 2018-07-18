package com.cogetel

//#quick-start-server
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

//#main-class
object QuickstartServer extends App with CustomerRoutes {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#project config
  override def config = ConfigFactory.load()

  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem(config.getString("my.actorsystem"))
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  val customerActor: ActorRef = system.actorOf(CustomerActor.props, config.getString("my.actorcustomer"))

  //#main-class
  // from the UserRoutes trait
  lazy val routes: Route = customerRoutes
  //#main-class

  //#http-server
  val httpPort = config.getInt("http.port")
  val httpHost = config.getString("http.interface")
  Http().bindAndHandle(routes, httpHost, httpPort)

  println(s"Server online at $httpHost:$httpPort/")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class

}
//#main-class
//#quick-start-server
