package com.bassgroove.analyzer.domain

import jakarta.persistence.*
import java.util.*

@Entity
class AnalysisResult(
    @Id val id: UUID = UUID.randomUUID(),
    @OneToOne(fetch = FetchType.LAZY) val job: AnalysisJob,
    var tempoEst: Double,
    var meanMs: Double,
    var medianMs: Double,
    var p95Ms: Double,
    var aheadPct: Double,
    var behindPct: Double,
    @Lob var pairsJson: String
)
