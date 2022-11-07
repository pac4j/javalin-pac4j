package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.servlet.JavalinServletContext;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.jee.context.session.JEESessionStore;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

public class SecurityHandler implements Handler {
    private static final String AUTH_GRANTED = "AUTH_GRANTED";

    public Config config;
    public String clients;
    public String authorizers;
    public String matchers;

    public SecurityHandler(Config config, String clients) {
        this(config, clients, null, null);
    }

    public SecurityHandler(Config config, String clients, String authorizers) {
        this(config, clients, authorizers, null);
    }

    public SecurityHandler(Config config, String clients, String authorizers, String matchers) {
        assertNotNull("config", config);
        this.config = config;
        this.clients = clients;
        this.authorizers = authorizers;
        this.matchers = matchers;
    }

    @Override
    public void handle(@NotNull Context javalinCtx) {
        final SessionStore bestSessionStore = FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(null, config, JavalinHttpActionAdapter.INSTANCE);
        final SecurityLogic bestLogic = FindBest.securityLogic(null, config, DefaultSecurityLogic.INSTANCE);

        JavalinWebContext context = new JavalinWebContext(javalinCtx);
        Object result = bestLogic.perform(
            context,
            bestSessionStore,
            this.config,
            (ctx, sessionStore, profiles, parameters) -> AUTH_GRANTED,
            bestAdapter,
            this.clients,
            this.authorizers,
            this.matchers
        );
        if (result != AUTH_GRANTED) {
            ((JavalinServletContext) javalinCtx).getTasks().clear(); // Used to throw UnauthorizedResponse
        }
    }
}
