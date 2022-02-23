package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;

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
    public void handle(Context javalinCtx) {
        final SessionStore bestSessionStore = FindBest.sessionStore(null, config, JavalinSessionStore.INSTANCE);
        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(null, config, JavalinHttpActionAdapter.INSTANCE);
        final LogoutLogic bestLogic = FindBest.logoutLogic(null, config, DefaultLogoutLogic.INSTANCE);

        bestLogic.perform(
            new JavalinWebContext(javalinCtx),
            bestSessionStore,
            this.config,
                bestAdapter,
            this.defaultUrl,
            this.logoutUrlPattern,
            this.localLogout,
            this.destroySession,
            this.centralLogout
        );
    }
}
