package com.agorapulse.micronaut.console

import spock.lang.Specification
import spock.lang.Unroll

class UserTest extends Specification {

    @Unroll
    void 'user "#user" is printed correctly as "#expected"'() {
        expect:
            expected == user.toString()
        where:
            expected                                | user
            'First Last (first_last) @ 127.0.0.1'   | new User('first_last', 'First Last', '127.0.0.1')
            'first_last @ 127.0.0.1'                | new User('first_last', null, '127.0.0.1')
            'first_last'                            | new User('first_last', null, null)
            'Anonymous'                             | new User(null, null, null)
            'First Last'                            | new User(null, 'First Last', null)
            'Anonymous @ 127.0.0.1'                 | new User(null, null, '127.0.0.1')
            'First Last @ 127.0.0.1'                | new User(null, 'First Last', '127.0.0.1')
    }

}
