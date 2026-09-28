package org.pac4j.javalin.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.javalin.LocalJavalinTestServer;

import static org.assertj.core.api.Assertions.assertThat;

class JavalinPac4jExampleTest {
    private Config config() {
        var authenticator = new TrivialUserPassAuthenticator("test", "secret");
        var facebook = new FormClient("/facebook-login", authenticator);
        facebook.setName("FacebookClient");
        var config = new Config(new Clients("http://localhost/callback",
            new FormClient("/login-form", authenticator), facebook));
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);
        return config;
    }

    @ParameterizedTest
    @CsvSource({"/, <h1>Examples</h1>", "/login-form, name=\"username\"", "/jwt, <h1>Generate JWT token</h1>"})
    void rendersExamplePages(String path, String expectedContent) throws Exception {
        Config config = config();
        try (var server = new LocalJavalinTestServer(cfg -> JavalinPac4jExample.configure(cfg, config))) {
            var response = server.get(path);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.get("Content-Type")).startsWith("text/html");
            assertThat(response.getContent()).contains(expectedContent);
        }
    }

    @Test
    void forceLoginUsesClientSelectedByTheExampleLink() throws Exception {
        Config config = config();
        try (var server = new LocalJavalinTestServer(cfg -> JavalinPac4jExample.configure(cfg, config))) {
            var response = server.get("/force-login?client_name=FacebookClient");

            assertThat(response.getStatus()).isEqualTo(302);
            assertThat(response.get("Location")).startsWith("/facebook-login");
        }
    }
}
