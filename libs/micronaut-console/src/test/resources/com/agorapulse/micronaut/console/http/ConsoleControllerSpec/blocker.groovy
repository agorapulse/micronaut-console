import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

Mono<String> mono = Mono.fromCallable {
    'Hello from blocker'
}

Flux.from(mono).blockFirst()
