package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.servlet.JavalinServletContext;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.jee.context.JEEFrameworkParameters;

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
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        Object result = config.getSecurityLogic().perform(
            this.config,
            (ctx, store, profiles) -> AUTH_GRANTED,
            this.clients,
            this.authorizers,
            this.matchers,
            new JEEFrameworkParameters(javalinCtx.req(), javalinCtx.res())
        );
        if (result != AUTH_GRANTED) {
            ((JavalinServletContext) javalinCtx).getTasks().clear(); // Used to throw UnauthorizedResponse
        }
    }
}
