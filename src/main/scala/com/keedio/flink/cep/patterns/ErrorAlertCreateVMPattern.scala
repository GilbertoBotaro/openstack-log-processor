package com.keedio.flink.cep.patterns

import com.keedio.flink.cep.IAlertPattern
import com.keedio.flink.cep.alerts.ErrorAlert
import com.keedio.flink.entities.LogEntry
import com.keedio.flink.utils.SyslogCode
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.windowing.time.Time

import scala.collection.JavaConverters._

/**
  * Created by luislazaro on 26/4/17.
  * lalazaro@keedio.com
  * Keedio
  */
class ErrorAlertCreateVMPattern extends IAlertPattern[LogEntry, ErrorAlert] {

  override def create(pattern: java.util.Map[String, LogEntry]): ErrorAlert = {
    val first: LogEntry = pattern.get("First Event")
    val second: LogEntry = pattern.get("Second Event")
    new ErrorAlert(first, second)
  }

  /**
    * Genereate an Alert if and only if it matches two consecutive LogEntries for the same host
    * withi severity ERROR
    *
    * @return
    */
  override def getEventPattern(): Pattern[LogEntry, _] = {
    //        Pattern
    //          .begin[LogEntry]("First Event")
    //          .subtype(classOf[LogEntry])
    //          .where(event => event.severity == SyslogCode.numberOfSeverity("ERROR") && event.service == "Nova" && event.body.contains("CEP_ID"))
    //          .next("Second Event")
    //          .subtype(classOf[LogEntry])
    //          .where(
    //            (event, ctx) => {
    //               event.severity == SyslogCode.numberOfSeverity("INFO") && event.service == "Neutron" && event.body.contains("CEP_ID") &&
    //              ctx.getEventsForPattern("First Event").asScala.toSeq.exists(
    //                logEntry => logEntry.body.split("CEP_ID=")(1).split("\\s+")(0) == event.body.split("CEP_ID=")(1).split("\\s+")(0))
    //            }
    //          )
    //          .within(Time.minutes(10))
    //      }

    Pattern
      .begin[LogEntry]("First Event")
      .subtype(classOf[LogEntry])
      .where(event => event.severity == SyslogCode.numberOfSeverity("ERROR"))
      .where(event => event.service == "Nova")
      .where(event => event.body.contains("CEP_ID"))
      .followedBy("Second Event")
      .subtype(classOf[LogEntry])
      .where(event => event.severity == SyslogCode.numberOfSeverity("INFO"))
      .where(event => event.service == "Neutron")
      .where(event => event.body.contains("CEP_ID"))
      .where(
        (event, ctx) => {
          val matches: Seq[LogEntry] = ctx.getEventsForPattern("First Event").asScala.toSeq.filter(_.body.contains("CEP_ID="))
          matches.exists(logEntry => logEntry.body.split("CEP_ID=")(1).split("\\s+")(0) == event.body.split("CEP_ID=")(1).split("\\s+")(0))
        }
      )
      .within(Time.minutes(10))
  }


}
