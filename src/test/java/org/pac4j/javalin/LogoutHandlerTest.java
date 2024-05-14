package org.pac4j.javalin;

import io.javalin.http.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.FrameworkParameters;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.http.client.indirect.FormClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LogoutHandlerTest {

    private final LogoutLogic logoutLogic = mock(LogoutLogic.class);
    private final Config config = new Config(new FormClient());
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final Context ctx = mock(Context.class);

    @BeforeEach
    public void setCallbackLogic() {
        config.setLogoutLogic(logoutLogic);
        when(ctx.res()).thenReturn(res);
        when(ctx.req()).thenReturn(req);
    }

    @Test
    public void testCustomLogoutLogic() {
        LogoutHandler handler = new LogoutHandler(config);
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testDefaultUrl() {
        LogoutHandler handler = new LogoutHandler(config, "https://example.org");
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), eq("https://example.org"), any(), any(), any(), any(), any());
    }

    @Test
    public void testLogoutUrlPattern() {
        LogoutHandler handler = new LogoutHandler(config, "https://example.org", "/logout");
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), eq("https://example.org"), eq("/logout"), any(), any(), any(), any());
    }

    @Test
    public void testLocalLogoutTrue() {
        LogoutHandler handler = new LogoutHandler(config);
        handler.localLogout = true;
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), any(), any(), eq(true), any(), any(), any());
    }

    @Test
    public void testLocalLogoutFalse() {
        LogoutHandler handler = new LogoutHandler(config);
        handler.localLogout = false;
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), any(), any(), eq(false), any(), any(), any());
    }

    @Test
    public void testDestroySession() {
        LogoutHandler handler = new LogoutHandler(config);
        handler.destroySession = true;
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), any(), any(),any(), eq(true), any(), any());
    }

    @Test
    public void testDestroySessionFalse() {
        LogoutHandler handler = new LogoutHandler(config);
        handler.destroySession = false;
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), any(), any(),any(), eq(false), any(), any());
    }

    @Test
    public void testCentralLogout() {
        LogoutHandler handler = new LogoutHandler(config);
        handler.centralLogout = true;
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), any(), any(),any(), any(), eq(true), any());
    }

    @Test
    public void testCentralLogoutFalse() {
        LogoutHandler handler = new LogoutHandler(config);
        handler.centralLogout = false;
        handler.handle(ctx);

        verify(logoutLogic).perform(any(), any(), any(),any(), any(), eq(false), any());
    }

    @Test
    public void testContext() {
        LogoutHandler handler = new LogoutHandler(config);
        handler.handle(ctx);

        ArgumentCaptor<FrameworkParameters> captor = ArgumentCaptor.forClass(FrameworkParameters.class);
        verify(logoutLogic).perform(any(), any(), any(), any(), any(), any(), captor.capture());
        FrameworkParameters parameters = captor.getValue();
        assertThat(parameters).isExactlyInstanceOf(JavalinFrameworkParameters.class);

        JavalinFrameworkParameters javalinFrameworkParameters = (JavalinFrameworkParameters) parameters;
        assertThat(javalinFrameworkParameters.getContext()).isSameAs(ctx);
    }
}
