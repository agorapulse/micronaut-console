import com.agorapulse.micronaut.console.ConsoleService
import com.agorapulse.micronaut.console.Script
import com.agorapulse.micronaut.console.User

ConsoleService service = ctx.getBean(ConsoleService)

service.execute(new Script('groovy', "'Hello Leo!'", new User('someuser', null, '/127.0.0.1')))
