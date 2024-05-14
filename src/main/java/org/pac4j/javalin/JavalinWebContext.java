package org.pac4j.javalin;

import io.javalin.http.Context;
import org.pac4j.jee.context.JEEContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Web context that uses the Javalin implementations of parameter handling instead of the servlet ones
 *
 * @author Jacob Burroughs
 * @since 7.0.0
 */
public class JavalinWebContext extends JEEContext {
    private final Context context;

    public JavalinWebContext(final Context context) {
        super(context.req(), context.res());

        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    // We use our own implementation here because javalin does not use the servlet getParameter, and using the
    // servlet getParameter method will consume the body and break javalin's formParams
    @Override
    public Optional<String> getRequestParameter(String name) {
        return Optional.ofNullable(context.formParam(name)).or(() -> Optional.ofNullable(context.queryParam(name)));
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        Map<String, List<String>> allParams = new HashMap<>(context.formParamMap());
        context.queryParamMap().forEach((k, v) -> allParams.merge(k, v, (v1, v2) ->
                Stream.concat(v1.stream(), v2.stream()).toList()));
        return allParams.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().toArray(new String[0])
        ));
    }
}
