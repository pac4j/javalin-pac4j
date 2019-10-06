package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

public class SecurityHandler implements Handler {

    private String AUTH_GRANTED = "AUTH_GRANTED";

    public SecurityLogic<Object, Pac4jContext> securityLogic = new DefaultSecurityLogic<>();
    public Config config;
    public String clients;
    public String authorizers;
    public String matchers;
    public Boolean multiProfile;

    public SecurityHandler(Config config, String clients) {
        this(config, clients, null, null);
    }

    public SecurityHandler(Config config, String clients, String authorizers) {
        this(config, clients, authorizers, null);
    }

    public SecurityHandler(Config config, String clients, String authorizers, String matchers) {
        this(config, clients, authorizers, matchers, null);
    }

    public SecurityHandler(Config config, String clients, String authorizers, String matchers, Boolean multiProfile) {
        this.config = config;
        this.clients = clients;
        this.authorizers = authorizers;
        this.matchers = matchers;
        this.multiProfile = multiProfile;
    }

    @Override
    public void handle(Context javalinCtx) {
        assertNotNull("securityLogic", securityLogic);
        assertNotNull("config", config);
        Pac4jContext context = new Pac4jContext(javalinCtx, config.getSessionStore());
        Object result = securityLogic.perform(
            context,
            this.config,
            (ctx, profiles, parameters) -> AUTH_GRANTED, config.getHttpActionAdapter(),
            this.clients,
            this.authorizers,
            this.matchers,
            this.multiProfile
        );
        if (result != AUTH_GRANTED) {
            throw new UnauthorizedResponse();
        }
    }
}
