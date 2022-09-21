package app.infra

import com.twitter.finagle.{Mysql, ServerErrorMonitor}
import com.twitter.finagle.client.DefaultPool
import com.twitter.util.Duration
import com.twitter.conversions.DurationOps._
import com.twitter.finagle.mysql.{Client, Transactions}


object MysqlClient {

  def init(): Client with Transactions = {
    Mysql.client.withSessionPool
         .ttl(
           Duration.fromSeconds(300) // connection will recreated after 5 minutes
         )
         .withSession
         .acquisitionTimeout(28600.seconds)
         .withSession
         .maxLifeTime(28700.seconds)
         .withCredentials("test", "test")
         .withMaxConcurrentPrepareStatements(256)
         .withSessionQualifier
         .noFailureAccrual
         .withDatabase("test")
         .newRichClient("localhost:3306")
  }


}
