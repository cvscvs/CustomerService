package com.cogetel

//#customer-registry-actor
import akka.actor.{ Actor, ActorLogging, Props }

//#customer-case-classes
final case class Customer(custId: Long, custName: String, vatAddress: Option[String])
final case class Customers(customers: Seq[Customer])
//#customer-case-classes

object CustomerActor {
  final case class ActionPerformed(description: String)
  final case class GetCustomer(custId: String)
  final case class GetCustomers()
  final case class UpdateCustomer(customer: Customer)
  /*final case class CreateCustomer(ctusomer: Customer)

  final case class DeleteCustomer(name: String)*/
  //  final case class GetCustomerById(custId: Long)

  def props: Props = Props[CustomerActor]
}

class CustomerActor extends Actor with ActorLogging {
  import CustomerActor._
  import CustomerRepository._
  //  var customers = Set.empty[Customer]

  def receive: Receive = {
    case GetCustomers =>
      sender() ! Customers(getCustomers())
    /*case CreateCustomer(customer) =>
    customers += customer
    sender() ! ActionPerformed(s"Customer ${customer.name} created.")*/
    case GetCustomer(custId) =>
      sender() ! getCustomerById(custId)
    /*case DeleteCustomer(name) =>
      customers.find(_.name == name) foreach { customer => customers -= customer }
      sender() ! ActionPerformed(s"Customer ${name} deleted.")*/
    case UpdateCustomer(customer) =>
      updateCustomer(customer)
      sender() ! ActionPerformed(s"Customer ${customer.custId}:${customer.custName} updated.")
  }

}
//#user-registry-actor