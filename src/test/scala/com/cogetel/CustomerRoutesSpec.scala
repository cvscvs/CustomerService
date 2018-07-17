package com.cogetel

//#user-routes-spec
//#test-top
import akka.actor.ActorRef
import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

//#set-up
class CustomerRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with CustomerRoutes {
  //#test-top

  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes, 
  // but we could "mock" it by implementing it in-place or by using a TestProbe() 
  override val customerActor: ActorRef =
    system.actorOf(CustomerActor.props, "customer")

  lazy val routes = customerRoutes

  override def config: Config = ConfigFactory.load()
  val apiUrl = "/customers"//config.getString("http.api")

  //#set-up

  //#actual-test
  "CustomerRoutes" should {
    "return no customers if no present (GET /customers)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = apiUrl)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"customers":[]}""")
      }
    }
    //#actual-test

    //#testing-post
    "be able to add customers (POST /customers)" in {
      val customer = Customer("Kapi", 42, "jp")
      val customerEntity = Marshal(customer).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post(apiUrl).withEntity(customerEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"Customer Kapi created."}""")
      }
    }
    //#testing-post

    "be able to remove customers (DELETE /customers)" in {
      // user the RequestBuilding DSL provided by ScalatestRouteSpec:
      val request = Delete(uri = s"$apiUrl/Kapi")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"description":"Customer Kapi deleted."}""")
      }
    }
    //#actual-test
  }
  //#actual-test

  //#set-up

}
//#set-up
//#user-routes-spec
