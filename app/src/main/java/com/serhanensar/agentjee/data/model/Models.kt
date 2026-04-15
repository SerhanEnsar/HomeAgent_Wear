package com.serhanensar.agentjee.data.model

data class FileItem(
    val name: String,
    val isDir: Boolean,
    val size: Long
)

data class StatusData(
    val cpu: Double,
    val ram: Double,
    val disk: Double,
    val temp: Double?
)

data class DockerContainer(
    val name: String,
    val image: String,
    val state: String, // running, exited, etc.
    val status: String
)
