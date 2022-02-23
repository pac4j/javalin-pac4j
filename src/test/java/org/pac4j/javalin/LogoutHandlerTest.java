package org.pac4j.javalin;

import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.http.client.indirect.FormClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LogoutHandlerTest {

    private final TestLogoutLogic logoutLogic = new TestLogoutLogic();
    private final FormClient formClient = new FormClient();
    private final Config config = new Config(formClient);
    private final LogoutHandler handler = new LogoutHandler(config);
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final Context ctx = new Context(req, res, Collections.emptyMap());

    @BeforeEach
    public void setCallbackLogic() {
        config.setLogoutLogic(logoutLogic);
    }

    @Test
    public void testDefaultSessionStore() {
        handler.handle(ctx);

        assertThat(logoutLogic.sessionStore).isSameAs(JavalinSessionStore.INSTANCE);
    }

    @Test
    public void testCustomSessionStore() {
        final SessionStore mockSessionStore = mock(SessionStore.class);
        config.setSessionStore(mockSessionStore);

        handler.handle(ctx);

        assertThat(logoutLogic.sessionStore).isSameAs(mockSessionStore);
    }

    @Test
    public void testDefaultHttpActionAdapter() {
        handler.handle(ctx);

        assertThat(logoutLogic.httpActionAdapter).isSameAs(JavalinHttpActionAdapter.INSTANCE);
    }

    @Test
    public void testCustomHttpActionAdapter() {
        final JavalinHttpActionAdapter adapter = new JavalinHttpActionAdapter();
        config.setHttpActionAdapter(adapter);

        handler.handle(ctx);

        assertThat(logoutLogic.httpActionAdapter).isSameAs(adapter);
    }

    @Test
    public void testCustomDefaultUrl() {
        LogoutHandler handler = new LogoutHandler(config, "http://example.org");

        handler.handle(ctx);

        assertThat(logoutLogic.defaultUrl).isEqualTo("http://example.org");
    }

    @Test
    public void testCustomLogoutUrlPattern() {
        final String logoutUrlPattern = "http://example.org/logout/*";
        LogoutHandler handler = new LogoutHandler(config, "http://example.org", logoutUrlPattern);

        handler.handle(ctx);

        assertThat(logoutLogic.logoutUrlPattern).isEqualTo(logoutUrlPattern);
    }

    @Test
    public void testLocalLogoutDefault() {
        handler.handle(ctx);
        assertThat(logoutLogic.localLogout).isNull();
    }

    @Test
    public void testLocalLogoutTrue() {
        handler.localLogout = true;
        handler.handle(ctx);
        assertThat(logoutLogic.localLogout).isTrue();
    }

    @Test
    public void testLocalLogoutFalse() {
        handler.localLogout = false;
        handler.handle(ctx);
        assertThat(logoutLogic.localLogout).isFalse();
    }

    @Test
    public void testDestroySessionDefault() {
        handler.handle(ctx);
        assertThat(logoutLogic.destroySession).isNull();
    }

    @Test
    public void testDestroySessionTrue() {
        handler.destroySession = true;
        handler.handle(ctx);
        assertThat(logoutLogic.destroySession).isTrue();
    }

    @Test
    public void testDestroySessionFalse() {
        handler.destroySession = false;
        handler.handle(ctx);
        assertThat(logoutLogic.destroySession).isFalse();
    }

    @Test
    public void testCentralLogoutDefault() {
        handler.handle(ctx);
        assertThat(logoutLogic.centralLogout).isNull();
    }

    @Test
    public void testCentralLogoutTrue() {
        handler.centralLogout = true;
        handler.handle(ctx);
        assertThat(logoutLogic.centralLogout).isTrue();
    }

    @Test
    public void testCentralLogoutFalse() {
        handler.centralLogout = false;
        handler.handle(ctx);
        assertThat(logoutLogic.centralLogout).isFalse();
    }

    public class TestLogoutLogic implements LogoutLogic {

        private WebContext context;
        private SessionStore sessionStore;
        private Config config;
        private HttpActionAdapter httpActionAdapter;
        private String defaultUrl;
        private String logoutUrlPattern;
        private Boolean localLogout;
        private Boolean destroySession;
        private Boolean centralLogout;

        @Override
        public Object perform(WebContext context, SessionStore sessionStore, Config config, HttpActionAdapter httpActionAdapter,
                              String defaultUrl, String logoutUrlPattern,
                              Boolean localLogout, Boolean destroySession, Boolean centralLogout) {
            this.context = context;
            this.sessionStore = sessionStore;
            this.config = config;
            this.httpActionAdapter = httpActionAdapter;
            this.defaultUrl = defaultUrl;
            this.logoutUrlPattern = logoutUrlPattern;
            this.localLogout = localLogout;
            this.destroySession = destroySession;
            this.centralLogout = centralLogout;
            return null;
        }
    }
}
