package org.pac4j.javalin;

import io.javalin.http.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jee.context.session.JEESessionStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CallbackHandlerTest {

    private final TestCallbackLogic testCallbackLogic = new TestCallbackLogic();
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final Context ctx = mock(Context.class);
    private final FormClient formClient = new FormClient();
    private final Config config = new Config(formClient);
    private final CallbackHandler handler = new CallbackHandler(config, "DefaultClient");

    @BeforeEach
    public void setCallbackLogic() {
        config.setCallbackLogic(testCallbackLogic);
        formClient.setCallbackUrl("http://example.org/callbackUrl");
        when(ctx.res()).thenReturn(res);
        when(ctx.req()).thenReturn(req);
    }

    @Test
    public void testDefaultSessionStore() {
        handler.handle(ctx);

        assertThat(testCallbackLogic.sessionStore).isEqualTo(JEESessionStore.INSTANCE);
        assertThat(testCallbackLogic.webContext).isExactlyInstanceOf(JavalinWebContext.class);
        assertThat(testCallbackLogic.config).isSameAs(config);
    }

    @Test
    public void testCustomSessionStore() {
        final SessionStore mockSessionStore = mock(SessionStore.class);
        config.setSessionStore(mockSessionStore);

        handler.handle(ctx);

        assertThat(testCallbackLogic.sessionStore).isNotEqualTo(JEESessionStore.INSTANCE);
        assertThat(testCallbackLogic.sessionStore).isEqualTo(mockSessionStore);
    }

    @Test
    public void testDefaultAdapter() {
        handler.handle(ctx);

        assertThat(testCallbackLogic.httpActionAdapter).isEqualTo(JavalinHttpActionAdapter.INSTANCE);
    }

    @Test
    public void testCustomAdapter() {
        HttpActionAdapter actionAdapter = new JavalinHttpActionAdapter();
        config.setHttpActionAdapter(actionAdapter);

        handler.handle(ctx);

        assertThat(testCallbackLogic.httpActionAdapter).isNotEqualTo(JavalinHttpActionAdapter.INSTANCE);
        assertThat(testCallbackLogic.httpActionAdapter).isEqualTo(actionAdapter);
    }

    @Test
    public void testCustomClientName() {
        formClient.setName("my-name");

        handler.handle(ctx);

        assertThat(testCallbackLogic.defaultClient).isEqualTo("my-name");
    }

    @Test
    public void testCustomDefaultUrl() {
        final Config config = new Config(formClient);
        config.setCallbackLogic(testCallbackLogic);
        final CallbackHandler handler = new CallbackHandler(config, "http://example.org",  true);

        handler.handle(ctx);

        assertThat(testCallbackLogic.defaultUrl).isEqualTo("http://example.org");
    }

    @Test
    public void testDefaultRenewSession() {
        handler.handle(ctx);

        assertThat(testCallbackLogic.renewSession).isNull();
    }

    @Test
    public void testCustomRenewSessionTrue() {
        final Config config = new Config(formClient);
        config.setCallbackLogic(testCallbackLogic);
        final CallbackHandler handler = new CallbackHandler(config, "http://example.org", true);

        handler.handle(ctx);

        assertThat(testCallbackLogic.renewSession).isTrue();
    }

    @Test
    public void testCustomRenewSessionFalse() {
        final Config config = new Config(formClient);
        config.setCallbackLogic(testCallbackLogic);
        final CallbackHandler handler = new CallbackHandler(config, "http://example.org", false);

        handler.handle(ctx);

        assertThat(testCallbackLogic.renewSession).isFalse();
    }

    public class TestCallbackLogic implements CallbackLogic {

        private WebContext webContext;
        private SessionStore sessionStore;
        private Config config;
        private HttpActionAdapter httpActionAdapter;
        private String defaultUrl;
        private Boolean renewSession;
        private String defaultClient;

        @Override
        public Object perform(WebContext webContext, SessionStore sessionStore, Config config,
                              HttpActionAdapter httpActionAdapter, String defaultUrl, Boolean renewSession, String defaultClient) {
            this.webContext = webContext;
            this.sessionStore = sessionStore;
            this.config = config;
            this.httpActionAdapter = httpActionAdapter;
            this.defaultUrl = defaultUrl;
            this.renewSession = renewSession;
            this.defaultClient = defaultClient;
            return null;
        }
    }
}
