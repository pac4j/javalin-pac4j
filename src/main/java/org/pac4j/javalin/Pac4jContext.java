package org.pac4j.javalin;

import io.javalin.http.Context;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.SessionStore;

public class Pac4jContext extends J2EContext {

    private Context javalinCtx;

    public Pac4jContext(Context javalinCtx) {
        this(javalinCtx, null);
    }

    public Pac4jContext(Context javalinCtx, SessionStore sessionStore) {
        super(javalinCtx.req, javalinCtx.res, sessionStore);
        this.javalinCtx = javalinCtx;
    }

    public Context getJavalinCtx() {
        return javalinCtx;
    }

    @Override
    public void writeResponseContent(String content) {
        javalinCtx.result(content);
    }

    @Override
    public void setResponseStatus(int code) {
        javalinCtx.status(code);
    }

    @Override
    public String getPath() {
        return javalinCtx.path();
    }

    @Override
    public void setResponseHeader(String name, String value) {
        super.setResponseHeader(name, value);
    }

}
