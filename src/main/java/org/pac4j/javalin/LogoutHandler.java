package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * Handles local application logout and logout at the identity provider.
 */
public class LogoutHandler implements Handler {
    /** The pac4j configuration used to process logout. */
    public Config config;
    /** Fallback redirect destination after logout, or null to use the logout logic's default. */
    public String defaultUrl;
    /** Regular expression for permitted requested logout URLs, or null to use the logout logic's default. */
    public String logoutUrlPattern;
    /** Whether to remove local profiles, or null to use the logout logic's default. */
    public Boolean localLogout;
    /** Whether to destroy the session, or null to use the logout logic's default. */
    public Boolean destroySession;
    /** Whether to trigger identity provider logout, or null to use the logout logic's default. */
    public Boolean centralLogout;

    /**
     * Creates a logout handler using the logout logic's default settings.
     *
     * @param config the non-null pac4j configuration
     */
    public LogoutHandler(Config config) {
        this(config, null);
    }

    /**
     * Creates a logout handler with a fallback redirect destination.
     *
     * @param config the non-null pac4j configuration
     * @param defaultUrl the fallback redirect destination after logout, or null to use the default
     */
    public LogoutHandler(Config config, String defaultUrl) {
        this(config, defaultUrl, null);
    }

    /**
     * Creates a logout handler with a pattern for validating requested redirect destinations.
     *
     * @param config the non-null pac4j configuration
     * @param defaultUrl the fallback redirect destination after logout, or null to use the default
     * @param logoutUrlPattern the regular expression for permitted requested logout URLs,
     *                         or null to use the logout logic's default
     */
    public LogoutHandler(Config config, String defaultUrl, String logoutUrlPattern) {
        assertNotNull("config", config);
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.logoutUrlPattern = logoutUrlPattern;
    }

    @Override
    public void handle(@NotNull Context javalinCtx) {
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        config.getLogoutLogic().perform(
            this.config,
            this.defaultUrl,
            this.logoutUrlPattern,
            this.localLogout,
            this.destroySession,
            this.centralLogout,
            new JavalinFrameworkParameters(javalinCtx)
        );
    }
}
