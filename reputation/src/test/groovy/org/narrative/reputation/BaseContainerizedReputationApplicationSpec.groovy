package org.narrative.reputation

import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import groovy.util.logging.Slf4j
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Consumer

@Slf4j
@Testcontainers
@DirtiesContext
@SpringBootTest(classes = ReputationIntegTestApplication.class, webEnvironment=SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles ( profiles = ['default','local','containerInteg'] )
abstract class BaseContainerizedReputationApplicationSpec extends Specification {
    static final int MYSQL_CONTAINER_PORT = 3306
    static final int MYSQL_EXTERNAL_PORT = 3316
    static final int REDIS_CONTAINER_PORT = 6379
    static final int REDIS_EXTERNAL_PORT = 6479

    static Consumer<CreateContainerCmd> mySQLCmdModifier = { e -> e.withPortBindings(new PortBinding(Ports.Binding.bindPort(MYSQL_EXTERNAL_PORT), new ExposedPort(MYSQL_CONTAINER_PORT))) }
    static Consumer<CreateContainerCmd> redisCmdModifier = { e -> e.withPortBindings(new PortBinding(Ports.Binding.bindPort(REDIS_EXTERNAL_PORT), new ExposedPort(REDIS_CONTAINER_PORT))) }

    @Shared
    MySQLContainer mySQLContainer = (MySQLContainer) new MySQLContainer("mysql:5.7.22")
            .withExposedPorts(3306)
            .withCreateContainerCmdModifier(mySQLCmdModifier)
            .withLogConsumer(new Slf4jLogConsumer(log))

    @Shared
    GenericContainer redisContainer = new GenericContainer("redis:3.2")
            .withExposedPorts(REDIS_CONTAINER_PORT)
            .withCreateContainerCmdModifier(redisCmdModifier)
            .withLogConsumer(new Slf4jLogConsumer(log))

    def setupSpec() {
        log.info('MySQL URL is: {}', mySQLContainer.jdbcUrl)
        log.info('Redis URL is: {}', redisContainer.getContainerIpAddress() + ":" + redisContainer.getMappedPort(6379))
    }
}
