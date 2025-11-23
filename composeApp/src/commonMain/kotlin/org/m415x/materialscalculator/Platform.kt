package org.m415x.materialscalculator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform