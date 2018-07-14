package org.pac4j.javalin.example;

import io.javalin.HaltException;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.javalin.DefaultHttpActionAdapter;
import org.pac4j.javalin.Pac4jContext;

public class ExampleHttpActionAdapter extends DefaultHttpActionAdapter {

    @Override
    public Void adapt(int code, Pac4jContext context) {
        if (code == HttpConstants.UNAUTHORIZED) {
            throw new HaltException(401, "Unauthorized");
        } else if (code == HttpConstants.FORBIDDEN) {
            throw new HaltException(403, "Forbidden");
        } else {
            return super.adapt(code, context);
        }
    }
}
