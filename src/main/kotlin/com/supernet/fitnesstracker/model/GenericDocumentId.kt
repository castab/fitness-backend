package com.supernet.fitnesstracker.model

import org.springframework.data.annotation.Id

data class GenericDocumentId(
    @Id
    val id: String
)