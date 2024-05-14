package org.pac4j.javalin;

import io.javalin.http.servlet.JavalinServletContext;
import io.javalin.http.servlet.Task;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.http.client.indirect.FormClient;

import java.util.Deque;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SecurityHandlerTest {

    private final SecurityLogic securityLogic = mock(SecurityLogic.class);
    private final Config config = new Config(new FormClient());
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final JavalinServletContext ctx = mock(JavalinServletContext.class);
    private final Deque<Task> deque = mock(Deque.class);

    @BeforeEach
    public void setCallbackLogic() {
        config.setSecurityLogic(securityLogic);
        when(ctx.res()).thenReturn(res);
        when(ctx.req()).thenReturn(req);
        when(ctx.getTasks()).thenReturn(deque);
        when(securityLogic.perform(eq(config), any(), any(), any(), any(), any())).thenReturn("AUTH_GRANTED");
    }

    @Test
    public void testCustomSecurityLogic() {
        SecurityHandler handler = new SecurityHandler(config, "my-clients");
        handler.handle(ctx);

        verify(securityLogic).perform(eq(config), any(), eq("my-clients"), any(), any(), any());
    }

    @Test
    public void testCustomAuthorizers() {
        SecurityHandler handler = new SecurityHandler(config, "my-clients", "my-authorizers");
        handler.handle(ctx);

        verify(securityLogic).perform(eq(config), any(), any(), eq("my-authorizers"), any(), any());
    }

    @Test
    public void testCustomMatchers() {
        SecurityHandler handler = new SecurityHandler(config, "my-clients", "my-authorizers", "my-matchers");
        handler.handle(ctx);

        verify(securityLogic).perform(eq(config), any(), any(), eq("my-authorizers"), eq("my-matchers"), any());
    }

    @Test
    public void clearTasksWhenAuthNotGranted() {
        when(securityLogic.perform(eq(config), any(), any(), any(), any(), any())).thenReturn("AUTH_DENIED");

        SecurityHandler handler = new SecurityHandler(config, "my-clients");
        handler.handle(ctx);

        verify(ctx).getTasks();
        verify(deque).removeIf(any());
    }
}
