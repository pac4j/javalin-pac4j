package org.pac4j.javalin;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JavalinWebContextTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void readsRawBodyAfterParameters(boolean allParameters) throws Exception {
        String body = "<LogoutRequest>\n  déconnexion\n</LogoutRequest>";
        try (var server = new LocalJavalinTestServer(cfg -> cfg.routes.post("/body", ctx -> {
            var webContext = new JavalinWebContext(ctx);
            if (allParameters) {
                webContext.getRequestParameters();
            } else {
                webContext.getRequestParameter("client_name");
            }
            ctx.result(webContext.getRequestContent());
        }))) {
            var response = server.request("POST", "/body?client_name=SAML2Client", body,
                Map.of("Content-Type", "text/xml; charset=UTF-8"));

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getContent()).isEqualTo(body);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sharesCachedBodyWithJavalinRegardlessOfWhoReadsFirst(boolean javalinFirst) throws Exception {
        String body = "username=test&password=secret";
        try (var server = new LocalJavalinTestServer(cfg -> cfg.routes.post("/body", ctx -> {
            var webContext = new JavalinWebContext(ctx);
            String first = javalinFirst ? ctx.body() : webContext.getRequestContent();
            String second = javalinFirst ? webContext.getRequestContent() : ctx.body();
            ctx.result(first + "\n" + second + "\n" + webContext.getRequestContent()
                + "\n" + webContext.getRequestParameter("username").orElse("missing"));
        }))) {
            var response = server.request("POST", "/body", body,
                Map.of("Content-Type", "application/x-www-form-urlencoded"));

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getContent()).isEqualTo(body + "\n" + body + "\n" + body + "\ntest");
        }
    }
}
