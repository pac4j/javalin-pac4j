package org.pac4j.javalin;

import io.javalin.ForbiddenResponse;
import io.javalin.UnauthorizedResponse;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.http.adapter.HttpActionAdapter;

public class DefaultHttpActionAdapter implements HttpActionAdapter<Void, Pac4jContext> {

    @Override
    public Void adapt(int code, Pac4jContext context) {
        if (code == HttpConstants.UNAUTHORIZED) {
            throw new UnauthorizedResponse();
        } else if (code == HttpConstants.FORBIDDEN) {
            throw new ForbiddenResponse();
        } else if (code == HttpConstants.TEMP_REDIRECT) {
            String location = context.getJavalinCtx().res.getHeader(HttpConstants.LOCATION_HEADER);
            context.getJavalinCtx().redirect(location);
        }
        return null;
    }

}
