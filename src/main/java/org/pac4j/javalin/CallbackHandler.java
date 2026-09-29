package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * Completes authentication for indirect clients on the callback endpoint.
 */
public class CallbackHandler implements Handler {
    /** The pac4j configuration used to process the callback. */
    public Config config;
    /** Redirect destination when no saved request exists, or null to use the callback logic's default. */
    public String defaultUrl;
    /** Whether to renew the session after authentication, or null to use the callback logic's default. */
    public Boolean renewSession;
    /** Optional fallback client name; null lets pac4j select the sole indirect client. */
    public String defaultClient;

    /**
     * Creates a callback handler using the callback logic's default settings.
     *
     * @param config the non-null pac4j configuration
     */
    public CallbackHandler(Config config) {
        this(config, null);
    }

    /**
     * Creates a callback handler with a fallback redirect destination.
     *
     * @param config the non-null pac4j configuration
     * @param defaultUrl the redirect destination when no saved request exists, or null to use the default
     */
    public CallbackHandler(Config config, String defaultUrl) {
        this(config, defaultUrl, null);
    }

    /**
     * Creates a callback handler with a session renewal setting.
     *
     * @param config the non-null pac4j configuration
     * @param defaultUrl the redirect destination when no saved request exists, or null to use the default
     * @param renewSession whether to renew the session, or null to use the callback logic's default
     */
    public CallbackHandler(Config config, String defaultUrl, Boolean renewSession) {
        this(config, defaultUrl, renewSession, null);
    }

    /**
     * Creates a callback handler with an optional fallback client.
     *
     * @param config the non-null pac4j configuration
     * @param defaultUrl the redirect destination when no saved request exists, or null to use the default
     * @param renewSession whether to renew the session, or null to use the callback logic's default
     * @param defaultClient the fallback client name when the request does not identify a client,
     *                      or null to let pac4j select the sole indirect client
     */
    public CallbackHandler(Config config, String defaultUrl, Boolean renewSession, String defaultClient) {
        assertNotNull("config", config);
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.renewSession = renewSession;
        this.defaultClient = defaultClient;
    }

    @Override
    public void handle(@NotNull Context javalinCtx) {
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        config.getCallbackLogic().perform(
                this.config,
                this.defaultUrl,
                this.renewSession,
                this.defaultClient,
                new JavalinFrameworkParameters(javalinCtx)
        );

    }
}
