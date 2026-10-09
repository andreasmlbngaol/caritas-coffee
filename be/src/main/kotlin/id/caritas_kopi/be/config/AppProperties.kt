package id.caritas_kopi.be.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppProperties {
    var jwt: Jwt = Jwt()
    var admin: Admin = Admin()
    var storage: Storage = Storage()
    var appUrl: String = "http://localhost:8088"
    var corsAllowedOrigins: String = ""

    class Jwt {
        var secret: String = ""
        var ttlSeconds: Long = 604800
        var cookieName: String = "kopi_session"
        var cookieSecure: Boolean = false
    }

    class Admin {
        var username: String = ""
        var password: String = ""
    }

    class Storage {
        var path: String = "./data/uploads"
    }
}
