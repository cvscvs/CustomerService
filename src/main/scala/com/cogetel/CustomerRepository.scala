package com.cogetel

import scalikejdbc._

trait tCustomerRepository {
  def getCustomerById(custId: String): Option[Customer]
  def getCustomers: Seq[Customer]
  def updateCustomer(cust: Customer)
}

object CustomerRepository extends tCustomerRepository {

  val driver: String = "oracle.jdbc.OracleDriver"
  val url: String = "jdbc:oracle:thin:@localhost:1521:xe"
  val userName: String = "ordstest"
  val password: String = "ordstest"

  connect

  def connect: Unit = {
    Class.forName(driver)
    ConnectionPool.singleton(url, userName, password)
  }

  override def getCustomerById(custId: String): Option[Customer] = {
    //    if (custId == null) Option[Customer]
    val customer: Option[Customer] = DB readOnly { implicit session =>
      SQL("select * from TBLCUSTOMER where CUST_ID = ?")
        .bind(custId.toLong)
        .map(rs =>
          Customer(
            custId = rs.long("CUST_ID"),
            custName = rs.string("CUST_NAME"),
            vatAddress = rs.stringOpt("VAT_ADDRESS")

          )).single.apply()
    }
    customer
  }

  override def getCustomers(): Seq[Customer] = {
    val list: Seq[Customer] = DB readOnly { implicit session =>
      SQL("select * from TBLCUSTOMER where rownum <= 10 order by CUST_NAME desc")
        .map(rs =>
          Customer(
            custId = rs.long("CUST_ID"),
            custName = rs.string("CUST_NAME"),
            vatAddress = rs.stringOpt("VAT_ADDRESS")
          )).list.apply()

    }
    list
  }

  override def updateCustomer(cust: Customer) = {
    DB localTx { implicit session =>
      sql"""UPDATE TBLCUSTOMER SET VAT_ADDRESS = ${cust.vatAddress} WHERE CUST_ID = ${cust.custId}""".update.apply()
    }
  }
}
