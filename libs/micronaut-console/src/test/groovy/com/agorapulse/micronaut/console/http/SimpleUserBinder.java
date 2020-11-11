package com.agorapulse.micronaut.console.http;

import com.agorapulse.micronaut.console.User;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import javax.inject.Singleton;

@Singleton
@Replaces(FallbackUserBinder.class)
public class SimpleUserBinder implements TypedRequestArgumentBinder<User> {

    private final FallbackUserBinder delegate = new FallbackUserBinder();

    @Override
    public Argument<User> argumentType() {
        return delegate.argumentType();
    }

    @Override
    public BindingResult<User> bind(ArgumentConversionContext<User> context, HttpRequest<?> source) {
        return () -> delegate.bind(context, source).getValue().map(u -> new User(
            source.getHeaders().get("X-Console-User"),
            source.getHeaders().get("X-Console-Name"),
            u.getAddress()
        ));
    }
}
