package org.pac4j.javalin;

import io.javalin.http.Context;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;

/**
 * @author Maximilian Hippler
 * @since 3.0.0
 */
public class JavalinWebContext extends JEEContext {
    private final Context javalinCtx;

    public JavalinWebContext(Context javalinCtx) {
        this(javalinCtx, JEESessionStore.INSTANCE);
    }

    public JavalinWebContext(Context javalinCtx, SessionStore sessionStore) {
        super(javalinCtx.req, javalinCtx.res, sessionStore);
        this.javalinCtx = javalinCtx;
    }

    public Context getJavalinCtx() {
        return javalinCtx;
    }

    @Override
    public String getPath() {
        return javalinCtx.path();
    }
}
