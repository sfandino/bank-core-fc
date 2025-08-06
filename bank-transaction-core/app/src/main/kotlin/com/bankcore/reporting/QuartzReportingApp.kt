package com.bankcore.reporting

import com.bankcore.payments.HikariProvider
import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import mu.KotlinLogging
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

// This file defines the scheduler but can be also be used for other schedules, not just reporting (any cleaning tasks, delete old data, etc.)

// The job that runs the Daily All Users Balance report
class DailyAllUserBalancesJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info { "[Quartz] Executing DailyAllUserBalancesJob" }
        val ds = HikariProvider.dataSource()
        val yesterday = LocalDate.now().minusDays(1)
        try {
            val balances = ReportingService.getUserBalances(ds, yesterday)
            // we could implement a more sophisticated export - PDF or email, Slack notification, etc.
            balances.forEach { balance ->
                logger.info { "User ${balance.userId} balance on $yesterday: ${balance.balanceTotal}" }
            }
            logger.info { "[Quartz] DailyAllUserBalancesJob completed successfully" }
        } catch (e: Exception) {
            logger.error(e) { "[Quartz] Error during DailyAllUserBalancesJob" }
        }
    }
}

fun main() {
    // followed https://www.quartz-scheduler.org/documentation/quartz-2.3.0/quick-start.html
    val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()
    scheduler.start()
    logger.info { "[Quartz] Scheduler started" }

    // Define job for DailyAllUserBalancesJob 
    val jobDetail = JobBuilder.newJob(DailyAllUserBalancesJob::class.java)
        .withIdentity(JobKey.jobKey("dailyAllUserBalancesJob", "reporting"))
        .build()

    // We use a cron job here as set a trigger to run it daily at 05:00 UTC - is weird but Quartz uses 6-field syntax - is more flexible I think
    val trigger = TriggerBuilder.newTrigger()
        .withIdentity("dailyBalancesTrigger", "reporting")
        .withSchedule(
            CronScheduleBuilder.cronSchedule("0 0 5 * * ?")
        )
        .forJob(jobDetail)
        .build()

    // Schedule the job
    scheduler.scheduleJob(jobDetail, trigger)
    logger.info { "[Quartz] Scheduled DailyAllUserBalancesJob with cron: 0 0 5 * * ? - 5:00 UTC" }

    // In case we want to shutdown the scheduler
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "[Quartz] Shutting down scheduler..." }
        scheduler.shutdown(true)
    })
}