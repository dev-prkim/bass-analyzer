package com.bassgroove.analyzer.infra

import com.bassgroove.analyzer.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RecordingRepository : JpaRepository<Recording, UUID>
interface AnalysisJobRepository : JpaRepository<AnalysisJob, UUID>
interface AnalysisResultRepository : JpaRepository<AnalysisResult, UUID>
