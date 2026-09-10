# Groovy script patterns for Micronaut Console

Recipes that recur in console scripts. Every snippet assumes the `ctx` binding (`io.micronaut.context.ApplicationContext`). Imports must be explicit: only `java.lang`, `java.util`, `java.io`, `java.net`, `java.math.BigDecimal/BigInteger` and `groovy.lang` are implicit. `groovy-json` is usually not on the classpath, so use the Jackson `ObjectMapper` bean for JSON.

## Contents

1. Sanity and environment probes
2. Bean lookup
3. Printing results
4. Reading data (JPA, DynamoDB, Redis, Elasticsearch)
5. Dry run, batching and idempotency
6. Traceable logging
7. Triggering jobs, queues and events
8. Transactions
9. Groovy gotchas

## 1. Sanity and environment probes

```groovy
println 'console ok'                                  // smoke test
println ctx.environment.activeNames                   // [ec2, cloud, beta] etc.
println ctx.environment.getProperty('console.header-name', String).orElse('none')
println System.getenv('ECS_CONTAINER_METADATA_URI_V4') ? 'ecs task' : 'not ecs'
println Runtime.runtime.availableProcessors()
''
```

Print a whole property source only locally; on beta/prod it leaks secrets into the response and the audit event.

## 2. Bean lookup

```groovy
import io.micronaut.inject.qualifiers.Qualifiers

FooService foo = ctx.getBean(FooService)                          // Class literal, no `.class` needed
JobQueues queues = ctx.getBean(JobQueues, Qualifiers.byName('sqs'))
boolean present = ctx.containsBean(OptionalFeature)               // conditional beans
ctx.getBeanDefinitions(FooService).each { println it.beanType }   // which implementations exist
```

Prefer the interface (`FooService`) over `DefaultFooService`; the console resolves the same bean the application uses, including `@Replaces` and `@Primary`.

## 3. Printing results

```groovy
import com.fasterxml.jackson.databind.ObjectMapper

ObjectMapper mapper = ctx.getBean(ObjectMapper)
String json(Object value) { mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value) }

println json(repository.findByOrganizationUid('organization_1'))
''    // keep the result section empty when everything was already printed
```

- `println` goes to the `# Out #` section, the last expression to `# Result #`.
- Return a `Map` or `List` when a caller will parse the JSON endpoint (`/console/execute`); return `''` when the payload is only for humans.
- For counts and comparisons, print `expected` next to `actual` so the verification reads on its own.

## 4. Reading data

JPA / JDBC:

```groovy
import io.micronaut.transaction.SynchronousTransactionManager
import jakarta.persistence.EntityManager

EntityManager em = ctx.getBean(EntityManager)
SynchronousTransactionManager tx = ctx.getBean(SynchronousTransactionManager)

List<Object[]> rows = tx.executeRead { status ->
    em.createNativeQuery('SELECT id, account_id FROM subscription WHERE status_id = :s ORDER BY id LIMIT :n')
      .setParameter('s', 4).setParameter('n', 50).getResultList()
}
rows.each { println "${it[0]} -> ${it[1]}" }
```

DynamoDB scan with pagination:

```groovy
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*

AmazonDynamoDB dynamo = ctx.getBean(AmazonDynamoDB)
Map<String, AttributeValue> lastKey = null
int seen = 0
do {
    ScanResult page = dynamo.scan(new ScanRequest().withTableName('Link').withLimit(500).withExclusiveStartKey(lastKey))
    page.items.each { item -> seen++ }
    lastKey = page.lastEvaluatedKey
} while (lastKey != null && seen < 10_000)   // hard cap; a full scan is a cost and a timeout risk
println "seen: $seen"
```

Prefer the app's own `*DBService` / `*Repository` beans over raw clients: they apply the same mapping, compression and index rules as production code.

## 5. Dry run, batching and idempotency

```groovy
// Tunables ---------------------------------------------------------------
boolean dryRun = true          // flip to false only after reviewing the dry-run output
int batchSize = 100            // rows per invocation; re-run until "remaining" reaches 0
long sleepTimeMs = 200         // throttle downstream calls (billing API, social API, mail...)
// ------------------------------------------------------------------------

int changed = 0
int skipped = 0
Map<String, String> errors = [:]

candidates.take(batchSize).each { candidate ->
    if (dryRun) { println "[DRY] would fix ${candidate.uid}"; skipped++; return }
    try {
        service.fix(candidate)
        changed++
        Thread.sleep(sleepTimeMs)
    } catch (Exception e) {
        errors[candidate.uid] = e.message
    }
}
println "changed=$changed skipped=$skipped errors=${errors.size()} remaining=${candidates.size() - batchSize}"
errors.each { uid, message -> println "  $uid: $message" }
''
```

Select candidates by their current state (`WHERE status_id = 4`), never by a list computed on a previous run, so a re-run skips what is already fixed.

## 6. Traceable logging

```groovy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

Logger log = LoggerFactory.getLogger('sc-123456_subscription_cache_invalidation')
log.info('invalidated org {} -> {}', organizationId, result)
log.error('failed org {}', organizationId, e)
```

The HTTP response is lost when the tunnel drops; the logger is not. Name the logger after the ticket so the log platform can be filtered on the logger name.

## 7. Triggering jobs, queues and events

```groovy
import com.agorapulse.worker.queue.JobQueues
import io.micronaut.inject.qualifiers.Qualifiers

ctx.getBean(JobQueues, Qualifiers.byName('sqs')).sendMessage('daily_insight_synchronize', message)
```

```groovy
import io.micronaut.context.event.ApplicationEventPublisher

ctx.getBean(ApplicationEventPublisher).publishEvent(new AccountArchivedEvent(accountId))
```

Micronaut Worker jobs (`com.agorapulse.worker`) can also be started through their job bean (`ctx.getBean(MyJob).run()`); check the application's `console/` folder for the established way before inventing one. Client beans (`*Client`, generated from `@Client`) work from the console and are the safest way to call a sibling service.

## 8. Transactions

Console scripts run outside any `@Transactional` boundary. Wrap JPA writes:

```groovy
tx.executeWrite { status ->
    em.createNativeQuery('UPDATE subscription SET status_id = 0 WHERE id = :id').setParameter('id', id).executeUpdate()
}
```

Service methods annotated `@Transactional` keep their own boundary when called from the script; do not nest them inside `executeWrite` unless you want one transaction for the whole batch.

## 9. Groovy gotchas

- Variables declared without a type or `def` become script bindings; declaring them typed (`FooService foo = ...`) keeps IntelliJ completion working and is the house style.
- `ctx.getBean(Foo)` and `Foo.class` are equivalent; the `// language=groovy` line above the script gives IntelliJ the right highlighting.
- Groovy truth: an empty `Optional` is truthy. Use `optional.present` / `optional.isPresent()`.
- `each` cannot `break`; use `find`, `any`, `takeWhile`, or a `for` loop with `break`.
- Java lambdas: pass `{ status -> ... }` closures where a `Function`/`Callable` is expected; Groovy coerces them.
- Integer division: `7 / 2` is `3.5` (BigDecimal). Use `intdiv` or cast for indexes.
- No `@Grab`, no `Grape`: the script runs with the application classpath only.
- Static inner classes and enums need the `Outer.Inner` import form, or `import Outer` then `Outer.Inner` in code.
