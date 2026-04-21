package com.example.jetpackcomposeapp.ImageRecognition

data class SightengineResponse(
    val status: String,
    val type: TypeData?
)

data class TypeData(
    val ai_generated: Double
)
