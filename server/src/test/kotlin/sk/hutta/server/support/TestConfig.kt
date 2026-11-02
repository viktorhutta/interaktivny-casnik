package sk.hutta.server.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.testcontainers.postgresql.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestConfig {

    // Samostatná testovacia databáza v Dockeri. Vznikne pri štarte testov a po nich zanikne.
    @Bean
    @ServiceConnection
    fun postgres() = PostgreSQLContainer("postgres:18")

    // Namiesto skutočnej brány sa všade vloží simulovaná.
    @Bean
    @Primary
    fun fakeFinbyClient() = FakeFinbyClient()
}