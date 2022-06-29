package com.conbus.pilot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PilotJsonDiffApplication

fun main(args: Array<String>) {
    runApplication<PilotJsonDiffApplication>(*args)
}
