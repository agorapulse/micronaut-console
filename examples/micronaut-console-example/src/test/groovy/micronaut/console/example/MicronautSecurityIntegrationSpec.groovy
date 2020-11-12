package micronaut.console.example

import com.agorapulse.gru.Gru
import com.agorapulse.gru.http.Http
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class MicronautSecurityIntegrationSpec extends Specification {

    @Shared @AutoCleanup ApplicationContext context
    @Shared @AutoCleanup EmbeddedServer server
    @Shared AccessRefreshTokenGenerator generator

    @Rule Gru gru = Gru.equip(Http.steal(this))


    void setupSpec() {
        context = ApplicationContext.build().build()

        context.start()

        generator = context.getBean(AccessRefreshTokenGenerator)

        server = context.getBean(EmbeddedServer)
        server.start()
    }

    void setup() {
        gru.prepare(server.URL.toString())
    }

    void 'cannot access the console without authentication'() {
        expect:
            gru.test {
                post '/console/execute/result', {
                    content inline('"Hello World"'), 'text/groovy'
                }
                expect {
                    status UNAUTHORIZED
                }
            }
    }

    void 'can access the console with the token'() {
        given:
            String token = generator.generate(new UserDetails('sherlock', [])).get().accessToken
        expect:
            gru.test {
                post '/console/execute/result', {
                    headers Authorization: "Bearer $token"
                    content inline('"Hello World"'), 'text/groovy'
                }
                expect {
                    text inline('Hello World')
                }
            }
    }


}
