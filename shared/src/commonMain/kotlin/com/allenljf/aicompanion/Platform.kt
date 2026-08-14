package com.allenljf.aicompanion

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform