package com.agorapulse.micronaut.console.function;

import com.agorapulse.micronaut.console.User;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class AuthorizedScript {

    static class Executor {
        private String id;
        private String name;
        private String address;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public User toUser() {
            return new User(id, name, address);
        }
    }

    private String body;
    private Executor user = new Executor();

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Executor getUser() {
        return user;
    }

    public void setUser(Executor user) {
        this.user = user;
    }

}
