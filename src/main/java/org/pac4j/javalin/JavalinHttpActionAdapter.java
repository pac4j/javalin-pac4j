package org.pac4j.javalin;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.CommonHelper;

/**
 * @author Maximilian Hippler
 * @since 3.0.0
 */
public class JavalinHttpActionAdapter implements HttpActionAdapter {
    public static final JavalinHttpActionAdapter INSTANCE = new JavalinHttpActionAdapter();

    @Override
    public Void adapt(HttpAction action, WebContext webContext) {
        CommonHelper.assertNotNull("action", action);
        CommonHelper.assertNotNull("context", webContext);
        if (webContext instanceof JavalinWebContext == false) {
            throw new RuntimeException("not a Javalin web context, but " + webContext.getClass().getName());
        }
        JavalinWebContext context = (JavalinWebContext) webContext;

        if(action instanceof WithContentAction){
            context.getJavalinCtx().status(action.getCode());
            context.getJavalinCtx().result(((WithContentAction) action).getContent());
            return null;
        } else if (action instanceof WithLocationAction) {
            context.getJavalinCtx().redirect(((WithLocationAction) action).getLocation(), action.getCode());
            return null;
        } else switch (action.getCode()) {
            case HttpConstants.UNAUTHORIZED:
                throw new UnauthorizedResponse();
            case HttpConstants.FORBIDDEN:
                throw new ForbiddenResponse();
            case HttpConstants.BAD_REQUEST:
                throw new BadRequestResponse();
            default: {
                context.getJavalinCtx().status(action.getCode());
                return null;
            }
        }
    }
}
