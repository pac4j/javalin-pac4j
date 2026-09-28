package org.pac4j.javalin;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.javalin.example.TrivialUserPassAuthenticator;
import org.pac4j.jee.context.session.JEESessionStore;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CallbackHandlerIntegrationTest {
    @ParameterizedTest
    @ValueSource(strings = {"", "?client_name=FormClient"})
    void authenticatesWithIndirectClientEvenWhenDirectClientIsFirst(String query) throws Exception {
        var authenticator = new TrivialUserPassAuthenticator("test", "secret");
        var config = new Config(new Clients("http://localhost/callback",
            new DirectBasicAuthClient(authenticator), new FormClient("/login", authenticator)));
        var handler = new CallbackHandler(config, "/profile", true);

        assertAuthenticatedProfile(handler, query, "FormClient:test");
    }

    @ParameterizedTest
    @CsvSource({"'', AlternateFormClient:test", "?client_name=FormClient, FormClient:test"})
    void usesExplicitDefaultClientUnlessRequestSelectsAnother(String query, String expectedProfile) throws Exception {
        var authenticator = new TrivialUserPassAuthenticator("test", "secret");
        var alternate = new FormClient("/alternate-login", authenticator);
        alternate.setName("AlternateFormClient");
        var config = new Config(new Clients("http://localhost/callback",
            new FormClient("/login", authenticator), alternate));
        var handler = new CallbackHandler(config, "/profile", true, "AlternateFormClient");

        assertAuthenticatedProfile(handler, query, expectedProfile);
    }

    private void assertAuthenticatedProfile(CallbackHandler handler, String query, String expectedProfile)
            throws Exception {
        try (var server = new LocalJavalinTestServer(cfg -> cfg.routes
                .post("/callback", handler)
                .get("/profile", ctx -> {
                    var manager = new ProfileManager(new JavalinWebContext(ctx), new JEESessionStore());
                    ctx.result(manager.getProfile().map(profile -> profile.getClientName() + ":" + profile.getId())
                        .orElse("anonymous"));
                }))) {
            var response = server.request("POST", "/callback" + query, "username=test&password=secret",
                Map.of("Content-Type", "application/x-www-form-urlencoded"));

            assertThat(response.getStatus()).isBetween(302, 303);
            assertThat(response.get("Location")).isEqualTo("/profile");
            assertThat(response.get("Set-Cookie")).isNotNull();
            String cookie = response.get("Set-Cookie").split(";", 2)[0];
            var profileResponse = server.request("GET", "/profile", "", Map.of("Cookie", cookie));
            assertThat(profileResponse.getStatus()).isEqualTo(200);
            assertThat(profileResponse.getContent()).isEqualTo(expectedProfile);
        }
    }
}
