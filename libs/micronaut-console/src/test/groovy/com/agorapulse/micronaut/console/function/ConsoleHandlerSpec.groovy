package com.agorapulse.micronaut.console.function

import com.agorapulse.testing.fixt.Fixt
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ConsoleHandlerSpec extends Specification {

    @Shared @AutoCleanup ConsoleHandler handler = new ConsoleHandler()

    @Rule Fixt fixt = Fixt.create(ConsoleHandlerSpec)

    void 'execute simple groovy script'() {
        expect:
            handler.apply('"Hello World"') == "Hello World"
    }

    void 'execute script with prints'() {
        expect:
            handler.apply(fixt.readText('prints.groovy')) == fixt.readText('prints.txt')
    }

    void 'execute script with exception'() {
        given:
            String errorResult = handler.apply(fixt.readText('error.groovy'))
            // this is bit brittle, use the following line to regenerate the file
            // fixt.writeText('error.txt', errorResult)
        expect:
            errorResult == fixt.readText('error.txt')
    }

}
