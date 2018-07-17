package com.cogetel

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import com.cogetel.CustomerActor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config

//#user-routes-class
trait CustomerRoutes extends JsonSupport {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[CustomerRoutes])

  def config: Config

  // other dependencies that UserRoutes use
  def customerActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  //#users-get-post
  //#users-get-delete

  lazy val customerRoutes: Route =
    pathPrefix("api" / "v1" / "customers") {
      concat(
        //#customers-get-delete
        pathEnd {
          concat(
            get {
              val customers: Future[Customers] =
                (customerActor ? GetCustomers).mapTo[Customers]
              complete(customers)
            },
            post {
              entity(as[Customer]) { customer =>
                val customerCreated: Future[ActionPerformed] =
                  (customerActor ? CreateCustomer(customer)).mapTo[ActionPerformed]
                onSuccess(customerCreated) { performed =>
                  log.info("Created customer [{}]: {}", customer.name, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        //#customers-get-post
        //#customers-get-delete
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-customer-info
              val maybeCustomer: Future[Option[Customer]] =
                (customerActor ? GetCustomer(name)).mapTo[Option[Customer]]
              rejectEmptyResponse {
                complete(maybeCustomer)
              }
              //#retrieve-customer-info
            },
            delete {
              //#customers-delete-logic
              val customerDeleted: Future[ActionPerformed] =
                (customerActor ? DeleteCustomer(name)).mapTo[ActionPerformed]
              onSuccess(customerDeleted) { performed =>
                log.info("Deleted customer [{}]: {}", name, performed.description)
                complete((StatusCodes.OK, performed))
              }
              //#customers-delete-logic
            }
          )
        }
      )
      //#customers-get-delete
    }
  //#all-routes
}
