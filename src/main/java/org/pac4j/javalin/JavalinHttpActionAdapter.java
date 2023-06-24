package org.pac4j.javalin;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.RedirectResponse;
import io.javalin.http.UnauthorizedResponse;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.jee.context.JEEContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        if (webContext instanceof JEEContext == false) {
            throw new RuntimeException("not a JEEContext, but " + webContext.getClass().getName());
        }
        JEEContext context = (JEEContext) webContext;

        final int code = action.getCode();
        if (code == HttpConstants.UNAUTHORIZED) {
            throw new UnauthorizedResponse();
        } else if (code == HttpConstants.FORBIDDEN) {
            throw new ForbiddenResponse();
        } else if (code == HttpConstants.BAD_REQUEST) {
            throw new BadRequestResponse();
        } else if (action instanceof WithContentAction){
            context.getNativeResponse().setStatus(action.getCode());
            String responseData = ((WithContentAction) action).getContent();
            context.getNativeResponse().setContentLength(responseData.length());
            try {
                context.getNativeResponse().getOutputStream().write(responseData.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        } else if (action instanceof WithLocationAction) {
            context.getNativeResponse().setStatus(action.getCode());
            String location = ((WithLocationAction) action).getLocation();
            context.getNativeResponse().setHeader("Location", location);
            throw new RedirectResponse();
        } else {
            context.getNativeResponse().setStatus(action.getCode());
            return null;
        }
    }
}
