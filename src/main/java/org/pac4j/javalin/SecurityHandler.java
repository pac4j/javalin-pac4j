package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

public class SecurityHandler implements Handler {
    private final String AUTH_GRANTED = "AUTH_GRANTED";

    public SecurityLogic<Object, JavalinWebContext> securityLogic = new DefaultSecurityLogic<>();
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
    public void handle(@NotNull Context javalinCtx) {
        final SessionStore<JavalinWebContext> bestSessionStore = FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
        final HttpActionAdapter<Object, JavalinWebContext> bestAdapter = FindBest.httpActionAdapter(null, config, JavalinHttpActionAdapter.INSTANCE);
        final SecurityLogic<Object, JavalinWebContext> bestLogic = FindBest.securityLogic(securityLogic, config, DefaultSecurityLogic.INSTANCE);

        assertNotNull("config", config);

        JavalinWebContext context = new JavalinWebContext(javalinCtx, bestSessionStore);
        Object result = bestLogic.perform(
            context,
            this.config,
            (ctx, profiles, parameters) -> AUTH_GRANTED, bestAdapter,
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
