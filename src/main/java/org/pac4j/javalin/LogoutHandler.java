package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.jee.context.JEEFrameworkParameters;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

public class LogoutHandler implements Handler {
    public Config config;
    public String defaultUrl;
    public String logoutUrlPattern;
    public Boolean localLogout;
    public Boolean destroySession;
    public Boolean centralLogout;

    public LogoutHandler(Config config) {
        this(config, null);
    }

    public LogoutHandler(Config config, String defaultUrl) {
        this(config, defaultUrl, null);
    }

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
            new JEEFrameworkParameters(javalinCtx.req(), javalinCtx.res())
        );
    }
}
