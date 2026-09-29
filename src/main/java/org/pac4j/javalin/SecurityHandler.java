package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.servlet.JavalinServletContext;
import io.javalin.http.servlet.Task;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * Applies pac4j authentication and authorization as a Javalin before handler.
 */
public class SecurityHandler implements Handler {
    private static final String AUTH_GRANTED = "AUTH_GRANTED";

    /** The pac4j security configuration. */
    public Config config;
    /** Comma-separated client names passed to the security logic. */
    public String clients;
    /** Comma-separated authorizer names passed to the security logic. */
    public String authorizers;
    /** Comma-separated matcher names passed to the security logic. */
    public String matchers;

    /**
     * Creates a security handler for the specified clients.
     *
     * @param config the non-null pac4j configuration
     * @param clients the comma-separated client names, or null to use the security logic's default
     */
    public SecurityHandler(Config config, String clients) {
        this(config, clients, null, null);
    }

    /**
     * Creates a security handler with explicit authorizers.
     *
     * @param config the non-null pac4j configuration
     * @param clients the comma-separated client names, or null to use the security logic's default
     * @param authorizers the comma-separated authorizer names, or null to use the security logic's default
     */
    public SecurityHandler(Config config, String clients, String authorizers) {
        this(config, clients, authorizers, null);
    }

    /**
     * Creates a security handler with explicit authorizers and matchers.
     *
     * @param config the non-null pac4j configuration
     * @param clients the comma-separated client names, or null to use the security logic's default
     * @param authorizers the comma-separated authorizer names, or null to use the security logic's default
     * @param matchers the comma-separated matcher names, or null to use the security logic's default
     */
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
            new JavalinFrameworkParameters(javalinCtx)
        );
        if (result != AUTH_GRANTED) {
            // Same logic javalin natively uses for skipping future tasks after a redirect in a before handler
            ((JavalinServletContext) javalinCtx).getTasks().removeIf(Task::getSkipOnExceptionAndRedirect);
        }
    }
}
