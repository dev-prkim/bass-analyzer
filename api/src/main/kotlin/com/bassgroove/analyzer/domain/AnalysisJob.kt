package com.bassgroove.analyzer.domain

import com.bassgroove.analyzer.enums.JobStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
class AnalysisJob(
    @Id val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) val recording: Recording,
    @Enumerated(EnumType.STRING) var status: JobStatus = JobStatus.QUEUED,
    var bpmHint: Double? = null,
    var errorMsg: String? = null,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)
