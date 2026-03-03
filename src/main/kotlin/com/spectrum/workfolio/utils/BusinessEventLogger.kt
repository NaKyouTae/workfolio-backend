package com.spectrum.workfolio.utils

import org.slf4j.LoggerFactory
import org.slf4j.MDC

object BusinessEventLogger {
    private val logger = LoggerFactory.getLogger("BusinessEvent")

    fun logEvent(
        eventType: String,
        message: String,
        workerId: String? = null,
        amount: Int? = null,
        status: String? = null,
        referenceId: String? = null,
        referenceType: String? = null,
        paymentId: String? = null,
        templateId: String? = null,
        balanceBefore: Int? = null,
        balanceAfter: Int? = null,
        txType: String? = null,
        extra: Map<String, String> = emptyMap(),
    ) {
        try {
            MDC.put("event_type", eventType)
            workerId?.let { MDC.put("worker_id", it) }
            amount?.let { MDC.put("amount", it.toString()) }
            status?.let { MDC.put("status", it) }
            referenceId?.let { MDC.put("reference_id", it) }
            referenceType?.let { MDC.put("reference_type", it) }
            paymentId?.let { MDC.put("payment_id", it) }
            templateId?.let { MDC.put("template_id", it) }
            balanceBefore?.let { MDC.put("balance_before", it.toString()) }
            balanceAfter?.let { MDC.put("balance_after", it.toString()) }
            txType?.let { MDC.put("tx_type", it) }
            extra.forEach { (k, v) -> MDC.put(k, v) }

            logger.info(message)
        } finally {
            MDC.clear()
        }
    }

    fun logError(
        eventType: String,
        message: String,
        exception: Exception? = null,
        workerId: String? = null,
        extra: Map<String, String> = emptyMap(),
    ) {
        try {
            MDC.put("event_type", eventType)
            MDC.put("status", "FAILED")
            workerId?.let { MDC.put("worker_id", it) }
            extra.forEach { (k, v) -> MDC.put(k, v) }

            if (exception != null) {
                logger.error(message, exception)
            } else {
                logger.error(message)
            }
        } finally {
            MDC.clear()
        }
    }
}
