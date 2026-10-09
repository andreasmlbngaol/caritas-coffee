package id.caritas_kopi.be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class BeApplication

fun main(args: Array<String>) {
    runApplication<BeApplication>(*args)
}
