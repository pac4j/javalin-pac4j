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
    public CallbackLogic<Object, JavalinWebContext> callbackLogic;
    public Config config;
    public String defaultUrl;
    public Boolean multiProfile;
    public Boolean saveInSession;
    public Boolean renewSession;

    public CallbackHandler(Config config) {
        this(config, null);
    }

    public CallbackHandler(Config config, String defaultUrl) {
        this(config, defaultUrl, null);
    }

    public CallbackHandler(Config config, String defaultUrl, Boolean multiProfile) {
        this(config, defaultUrl, multiProfile, null);
    }

    public CallbackHandler(Config config, String defaultUrl, Boolean multiProfile, Boolean renewSession) {
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.multiProfile = multiProfile;
        this.renewSession = renewSession;
    }

    @Override
    public void handle(@NotNull Context javalinCtx) {
        final SessionStore<JavalinWebContext> bestSessionStore = FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
        final HttpActionAdapter<Object, JavalinWebContext> bestAdapter = FindBest.httpActionAdapter(null, config, JavalinHttpActionAdapter.INSTANCE);
        final CallbackLogic<Object, JavalinWebContext> bestCallbackLogic = FindBest.callbackLogic(callbackLogic, config, DefaultCallbackLogic.INSTANCE);

        assertNotNull("config", config);

        JavalinWebContext context = new JavalinWebContext(javalinCtx, bestSessionStore);
        bestCallbackLogic.perform(context,
                this.config,
                bestAdapter,
                this.defaultUrl,
                this.saveInSession,
                this.multiProfile,
                this.renewSession,
                "FormClient"
        );
    }
}
