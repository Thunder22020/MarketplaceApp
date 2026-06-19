package com.daniel.marketplaceapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class MarketplaceAppApplication

fun main(args: Array<String>) {
    runApplication<MarketplaceAppApplication>(*args)
}
