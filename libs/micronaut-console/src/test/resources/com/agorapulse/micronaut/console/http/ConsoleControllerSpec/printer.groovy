import io.netty.util.concurrent.FastThreadLocalThread

if (Thread.currentThread() instanceof FastThreadLocalThread) {
    throw new IllegalStateException('Running on FastThreadLocalThread will fail execution of HTTP client requests')
}

println "This is a debug message"

"Hello Developer!"
