package com.daniel.marketplaceapp.testsupport.annotations

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceIntegrationTest
