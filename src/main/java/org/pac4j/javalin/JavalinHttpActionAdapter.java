package org.pac4j.javalin;

import io.javalin.http.*;
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
            throw new RuntimeException("not a JavalinWebContext, but " + webContext.getClass().getName());
        }
        JavalinWebContext context = (JavalinWebContext) webContext;

        final int code = action.getCode();
        if (code == HttpConstants.UNAUTHORIZED) {
            throw new UnauthorizedResponse();
        } else if (code == HttpConstants.FORBIDDEN) {
            throw new ForbiddenResponse();
        } else if (code == HttpConstants.BAD_REQUEST) {
            throw new BadRequestResponse();
        } else if (action instanceof WithContentAction){
            context.getContext().status(code);
            String responseData = ((WithContentAction) action).getContent();
            context.getContext().result(responseData);
            return null;
        } else if (action instanceof WithLocationAction) {
            String location = ((WithLocationAction) action).getLocation();
            context.getContext().redirect(location, HttpStatus.forStatus(code));
            return null;
        } else {
            context.getContext().status(code);
            return null;
        }
    }
}
