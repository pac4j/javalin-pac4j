package org.pac4j.javalin;

import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pac4j.core.context.FrameworkParameters;

@AllArgsConstructor
@Getter
public class JavalinFrameworkParameters implements FrameworkParameters {
    private final Context context;
}
