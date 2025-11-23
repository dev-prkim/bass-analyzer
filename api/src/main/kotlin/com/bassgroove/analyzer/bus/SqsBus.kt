package com.bassgroove.analyzer.bus

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.stereotype.Service

@Service
class SqsBus(private val sqsTemplate: SqsTemplate) {
    fun send(queue: String, payloadJson: String) {
        sqsTemplate.send(queue, payloadJson)
    }
}
