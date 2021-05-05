package com.agorapulse.micronaut.console.function

import com.agorapulse.testing.fixt.Fixt
import groovy.transform.CompileDynamic
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@CompileDynamic
class AuthConsoleHandlerSpec extends Specification {

    @Shared @AutoCleanup AuthConsoleHandler handler = new AuthConsoleHandler()

    Fixt fixt = Fixt.create(ConsoleHandlerSpec)

    void 'execute simple groovy script'() {
        expect:
            handler.apply(script('"Hello World"')) == 'Hello World'
    }

    void 'execute script with prints'() {
        expect:
            handler.apply(script(fixt.readText('prints.groovy'))) == fixt.readText('prints.txt')
    }

    void 'execute script with exception'() {
        given:
            String errorResult = handler.apply(script(fixt.readText('error.groovy')))
        expect:
            errorResult.startsWith('com.agorapulse.micronaut.console.ConsoleException: Exception during script execution')
    }

    private AuthorizedScript script(String body) {
        return new AuthorizedScript(
            body: body,
            user: new AuthorizedScript.Executor(id: 'executor', name: 'Script Executor', address: '/10.0.0.1')
        )
    }

}
