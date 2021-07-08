package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

public class CallbackHandler implements Handler {
    public CallbackLogic callbackLogic;
    public Config config;
    public String defaultUrl;
    public Boolean renewSession;

    public CallbackHandler(Config config) {
        this(config, null);
    }

    public CallbackHandler(Config config, String defaultUrl) {
        this(config, defaultUrl, null);
    }

    public CallbackHandler(Config config, String defaultUrl, Boolean renewSession) {
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.renewSession = renewSession;
    }

    @Override
    public void handle(@NotNull Context javalinCtx) {
        final SessionStore bestSessionStore = FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(null, config, JavalinHttpActionAdapter.INSTANCE);
        final CallbackLogic bestCallbackLogic = FindBest.callbackLogic(callbackLogic, config, DefaultCallbackLogic.INSTANCE);

        assertNotNull("config", config);

        JavalinWebContext context = new JavalinWebContext(javalinCtx);
        bestCallbackLogic.perform(context,
                bestSessionStore,
                this.config,
                bestAdapter,
                this.defaultUrl,
                this.renewSession,
                "FormClient"
        );
    }
}
