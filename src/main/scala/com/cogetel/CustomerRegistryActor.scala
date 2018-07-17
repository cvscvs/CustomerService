package com.cogetel

//#user-registry-actor
import akka.actor.{ Actor, ActorLogging, Props }

//#user-case-classes
final case class Customer(name: String, age: Int, countryOfResidence: String)
final case class Customers(customers: Seq[Customer])
//#user-case-classes

object CustomerActor {
  final case class ActionPerformed(description: String)
  final case object GetCustomers
  final case class CreateCustomer(customer: Customer)
  final case class GetCustomer(name: String)
  final case class DeleteCustomer(name: String)

  def props: Props = Props[CustomerActor]
}

class CustomerActor extends Actor with ActorLogging {
  import CustomerActor._

  var customers = Set.empty[Customer]

  def receive: Receive = {
    case GetCustomers =>
      sender() ! Customers(customers.toSeq)
    case CreateCustomer(customer) =>
      customers += customer
      sender() ! ActionPerformed(s"Customer ${customer.name} created.")
    case GetCustomer(name) =>
      sender() ! customers.find(_.name == name)
    case DeleteCustomer(name) =>
      customers.find(_.name == name) foreach { customer => customers -= customer }
      sender() ! ActionPerformed(s"Customer ${name} deleted.")
  }
}
//#user-registry-actor