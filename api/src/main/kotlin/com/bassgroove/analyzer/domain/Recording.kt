package com.bassgroove.analyzer.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant
import java.util.*

@Entity
class Recording(
    @Id val id: UUID = UUID.randomUUID(),
    val original: String,
    val uri: String,
    val createdAt: Instant = Instant.now()
)
