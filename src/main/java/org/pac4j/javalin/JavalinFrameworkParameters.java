package org.pac4j.javalin;

import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pac4j.core.context.FrameworkParameters;

/**
 * Specific Javalin parameters.
 *
 * @author Jacob Burroughs
 * @since 7.0.0
 */
@AllArgsConstructor
@Getter
public class JavalinFrameworkParameters implements FrameworkParameters {
    private final Context context;
}
