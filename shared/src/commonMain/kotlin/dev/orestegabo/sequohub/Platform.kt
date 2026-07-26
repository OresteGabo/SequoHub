package dev.orestegabo.sequohub

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform