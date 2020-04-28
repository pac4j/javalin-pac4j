package org.pac4j.javalin.example;

import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.javalin.JavalinHttpActionAdapter;
import org.pac4j.javalin.JavalinWebContext;

public class ExampleHttpActionAdapter extends JavalinHttpActionAdapter {
    @Override
    public Void adapt(HttpAction action, JavalinWebContext context) {
        switch(action.getCode()){
            case HttpConstants.UNAUTHORIZED: throw new UnauthorizedResponse("Unauthorized - Please Login first");
            case HttpConstants.FORBIDDEN: throw new ForbiddenResponse("Forbidden - You don't have access to this resource");
            default: return super.adapt(action, context);
        }
    }
}
