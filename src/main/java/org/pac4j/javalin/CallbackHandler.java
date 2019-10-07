package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

public class CallbackHandler implements Handler {

    public CallbackLogic<Object, Pac4jContext> callbackLogic = new DefaultCallbackLogic<>();
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
    public void handle(Context javalinCtx) {
        assertNotNull("callbackLogic", callbackLogic);
        assertNotNull("config", config);
        Pac4jContext context = new Pac4jContext(javalinCtx, config.getSessionStore());
        callbackLogic.perform(
            context,
            this.config,
            this.config.getHttpActionAdapter(),
            this.defaultUrl,
            this.saveInSession,
            this.multiProfile,
            this.renewSession,
            "FormClient"
        );
    }

}
