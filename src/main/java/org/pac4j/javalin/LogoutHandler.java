package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

public class LogoutHandler implements Handler {

    public LogoutLogic<Object, Pac4jContext> logoutLogic = new DefaultLogoutLogic<>();
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
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.logoutUrlPattern = logoutUrlPattern;
    }

    @Override
    public void handle(Context javalinCtx) {
        assertNotNull("logoutLogic", logoutLogic);
        assertNotNull("config", config);
        logoutLogic.perform(
            new Pac4jContext(javalinCtx, config.getSessionStore()),
            this.config,
            this.config.getHttpActionAdapter(),
            this.defaultUrl,
            this.logoutUrlPattern,
            this.localLogout,
            this.destroySession,
            this.centralLogout
        );
    }

}
