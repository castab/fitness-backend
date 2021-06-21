package com.supernet.fitnesstracker.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.util.*

@Document(collection = "sets")
data class FitnessSet (
    @Id
    val id: String = UUID.randomUUID().toString(),
    @Indexed
    val exerciseId: String = "00000000-0000-0000-0000-000000000000",
    @Indexed
    val timestamp: ZonedDateTime = ZonedDateTime.now(),
    val order: Int = 0,
    val reps: Int = 0,
    val of: Float = 0f
)