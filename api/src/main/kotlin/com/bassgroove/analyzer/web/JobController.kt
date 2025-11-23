package com.bassgroove.analyzer.web

import com.bassgroove.analyzer.domain.*
import com.bassgroove.analyzer.enums.JobStatus
import com.bassgroove.analyzer.infra.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api")
class JobController(
    private val jobRepo: AnalysisJobRepository,
    private val resRepo: AnalysisResultRepository
) {
    private val om = jacksonObjectMapper()

    @GetMapping("/jobs/{id}")
    fun getJob(@PathVariable id: UUID): Map<String, Any?> {
        val job = jobRepo.findById(id).orElseThrow()
        val res = resRepo.findAll().find { it.job.id == id }
        return mapOf(
            "jobId" to id, "status" to job.status, "error" to job.errorMsg,
            "result" to res?.let {
                mapOf(
                    "tempoEst" to it.tempoEst, "meanMs" to it.meanMs, "medianMs" to it.medianMs,
                    "p95Ms" to it.p95Ms, "aheadPct" to it.aheadPct, "behindPct" to it.behindPct,
                    "pairsJson" to it.pairsJson
                )
            }
        )
    }

    @SqsListener("\${app.queues.result}")
    fun handleResult(message: String) {
        val node = om.readTree(message)
        val jobId = UUID.fromString(node.get("jobId").asText())
        val succeeded = node.get("succeeded").asBoolean()

        val job = jobRepo.findById(jobId).orElse(null) ?: return
        if (succeeded) {
            job.status = JobStatus.DONE
            job.updatedAt = Instant.now()
            jobRepo.save(job)

            val metrics = node.get("metrics")
            val pairs = node.get("pairs")
            resRepo.save(
                AnalysisResult(
                    job = job,
                    tempoEst = metrics.get("tempoEst").asDouble(),
                    meanMs = metrics.get("meanMs").asDouble(),
                    medianMs = metrics.get("medianMs").asDouble(),
                    p95Ms = metrics.get("p95Ms").asDouble(),
                    aheadPct = metrics.get("aheadPct").asDouble(),
                    behindPct = metrics.get("behindPct").asDouble(),
                    pairsJson = om.writeValueAsString(pairs)
                )
            )
        } else {
            job.status = JobStatus.FAILED
            job.errorMsg = node.get("error")?.asText()
            job.updatedAt = Instant.now()
            jobRepo.save(job)
        }
    }
}
