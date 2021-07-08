package org.pac4j.javalin;

import io.javalin.http.Context;
import org.pac4j.core.context.JEEContext;

/**
 * @author Maximilian Hippler
 * @since 3.0.0
 */
public class JavalinWebContext extends JEEContext {
    private final Context javalinCtx;

    public JavalinWebContext(Context javalinCtx) {
        super(javalinCtx.req, javalinCtx.res);
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
