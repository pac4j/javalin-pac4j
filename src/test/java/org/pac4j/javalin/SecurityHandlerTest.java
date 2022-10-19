package org.pac4j.javalin;

import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jee.context.session.JEESessionStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityHandlerTest {

    private final TestSecurityLogic securityLogic = new TestSecurityLogic();
    private final FormClient formClient = new FormClient();
    private final Config config = new Config(formClient);
    private final SecurityHandler handler = new SecurityHandler(config, "my-clients");
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final Context ctx = mock(Context.class);

    @BeforeEach
    public void setCallbackLogic() {
        config.setSecurityLogic(securityLogic);
        when(ctx.res()).thenReturn(res);
        when(ctx.req()).thenReturn(req);
    }

    @Test
    public void testSessionStoreDefault() {
        handler.handle(ctx);

        assertThat(securityLogic.sessionStore).isSameAs(JEESessionStore.INSTANCE);
    }

    @Test
    public void testSessionStoreCustom() {
        final SessionStore mockSessionStore = mock(SessionStore.class);
        config.setSessionStore(mockSessionStore);

        handler.handle(ctx);

        assertThat(securityLogic.sessionStore).isSameAs(mockSessionStore);
    }

    @Test
    public void testHttpAdapterDefault() {
        handler.handle(ctx);

        assertThat(securityLogic.httpActionAdapter).isSameAs(JavalinHttpActionAdapter.INSTANCE);
    }

    @Test
    public void testHttpAdapterCustom() {
        final JavalinHttpActionAdapter adapter = new JavalinHttpActionAdapter();
        config.setHttpActionAdapter(adapter);

        handler.handle(ctx);

        assertThat(securityLogic.httpActionAdapter).isSameAs(adapter);
    }

    @Test
    public void testClients() {
        handler.handle(ctx);

        assertThat(securityLogic.clients).isEqualTo("my-clients");
    }

    @Test
    public void testCustomAuthorizers() {
        handler.authorizers = "my-authorizers";

        handler.handle(ctx);

        assertThat(securityLogic.authorizers).isEqualTo("my-authorizers");
    }

    @Test
    public void testCustomMatchers() {
        handler.matchers = "my-matchers";

        handler.handle(ctx);

        assertThat(securityLogic.matchers).isEqualTo("my-matchers");
    }

    @Test
    public void testResultNotGranted() {
        securityLogic.result = "WHATEVER";

        assertThatThrownBy(() -> handler.handle(ctx)).isExactlyInstanceOf(UnauthorizedResponse.class);
    }

    private final class TestSecurityLogic implements SecurityLogic {

        private String result = "AUTH_GRANTED";
        private WebContext context;
        private SessionStore sessionStore;
        private Config config;
        private SecurityGrantedAccessAdapter securityGrantedAccessAdapter;
        private HttpActionAdapter httpActionAdapter;
        private String clients;
        private String authorizers;
        private String matchers;

        @Override
        public Object perform(WebContext context, SessionStore sessionStore, Config config, SecurityGrantedAccessAdapter securityGrantedAccessAdapter, HttpActionAdapter httpActionAdapter, String clients, String authorizers, String matchers, Object... parameters) {
            this.context = context;
            this.sessionStore = sessionStore;
            this.config = config;
            this.securityGrantedAccessAdapter = securityGrantedAccessAdapter;
            this.httpActionAdapter = httpActionAdapter;
            this.clients = clients;
            this.authorizers = authorizers;
            this.matchers = matchers;

            return result;
        }
    }
}
