package org.pac4j.javalin;

import org.pac4j.core.context.FrameworkParameters;
import org.pac4j.core.context.WebContextFactory;
import org.pac4j.core.exception.TechnicalException;

/**
 * Build a Javalin context from parameters.
 *
 * @author Jacob Burroughs
 * @since 7.0.0
 */
public class JavalinContextFactory implements WebContextFactory {
    /** Constant <code>INSTANCE</code> */
    public static final WebContextFactory INSTANCE = new JavalinContextFactory();

    /** {@inheritDoc} */
    @Override
    public JavalinWebContext newContext(final FrameworkParameters parameters) {
        if (parameters instanceof JavalinFrameworkParameters jeeFrameworkParameters) {
            return new JavalinWebContext(jeeFrameworkParameters.getContext());
        }
        throw new TechnicalException("Bad parameters type");
    }
}
