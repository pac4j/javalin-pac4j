package org.pac4j.javalin;

import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;

public class JavalinHttpActionAdapter implements HttpActionAdapter<Void, JavalinWebContext> {
    public static final JavalinHttpActionAdapter INSTANCE = new JavalinHttpActionAdapter();

    @Override
    public Void adapt(HttpAction action, JavalinWebContext context) {
        if (action instanceof WithLocationAction) {
            context.getJavalinCtx().redirect(((WithLocationAction) action).getLocation());
            return null;
        } else switch (action.getCode()) {
            case HttpConstants.UNAUTHORIZED:
                throw new UnauthorizedResponse();
            case HttpConstants.FORBIDDEN:
                throw new ForbiddenResponse();
            default:
                throw new TechnicalException("No action provided");
        }
    }
}
