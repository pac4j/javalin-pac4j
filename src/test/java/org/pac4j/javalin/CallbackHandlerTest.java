package org.pac4j.javalin;

import io.javalin.http.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.http.client.indirect.FormClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CallbackHandlerTest {

    private final CallbackLogic callbackLogic = mock(CallbackLogic.class);
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final Context ctx = mock(Context.class);
    private final FormClient formClient = new FormClient();
    private final Config config = new Config(formClient);

    @BeforeEach
    public void setCallbackLogic() {
        config.setCallbackLogic(callbackLogic);
        formClient.setCallbackUrl("http://example.org/callbackUrl");
        when(ctx.res()).thenReturn(res);
        when(ctx.req()).thenReturn(req);
    }

    @Test
    public void testDefaultUrlIsNull() {
        CallbackHandler handler = new CallbackHandler(config);
        handler.handle(ctx);

        verify(callbackLogic).perform(eq(config), isNull(), any(), eq("FormClient"), any());
    }

    @Test
    public void testDefaultUrl() {
        CallbackHandler handler = new CallbackHandler(config, "/my-url");
        handler.handle(ctx);

        verify(callbackLogic).perform(eq(config), eq("/my-url"), any(), any(), any());
    }

    @Test
    public void testRenewSession() {
        CallbackHandler handler = new CallbackHandler(config, "/my-url", true);

        handler.handle(ctx);

        verify(callbackLogic).perform(eq(config), any(), eq(true), any(), any());
    }

    @Test
    public void testRenewSessionFalse() {
        CallbackHandler handler = new CallbackHandler(config, "/my-url", false);

        handler.handle(ctx);

        verify(callbackLogic).perform(eq(config), any(), eq(false), any(), any());
    }
}
