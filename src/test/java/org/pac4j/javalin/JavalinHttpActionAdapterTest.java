package org.pac4j.javalin;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.RedirectResponse;
import io.javalin.http.UnauthorizedResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pac4j.core.exception.http.BadRequestAction;
import org.pac4j.core.exception.http.ForbiddenAction;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.core.exception.http.UnauthorizedAction;
import org.pac4j.jee.context.JEEContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JavalinHttpActionAdapterTest {

    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final Context ctx = mock(Context.class);
    private final JEEContext context = new JEEContext(req, res);

    @BeforeEach
    public void setupMocks() {
        when(ctx.res()).thenReturn(res);
        when(ctx.req()).thenReturn(req);
    }

    @Test
    public void testActionNotNull() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(null, null))
                .hasMessage("action cannot be null");
    }

    @Test
    public void testContextNotNull() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new OkAction(""), null))
                .hasMessage("context cannot be null");
    }

    @Test
    public void testAdapterWithContentAction() throws IOException  {
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(res.getOutputStream()).thenReturn(sos);

        JavalinHttpActionAdapter.INSTANCE.adapt(new OkAction("my-content"), context);

        verify(res).setStatus(eq(200));
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(sos).write(captor.capture());
        byte[] value = captor.getValue();
        assertThat(new String(value, StandardCharsets.UTF_8)).isEqualTo("my-content");
    }

    @Test
    public void testAdapterWithLocationAction() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new FoundAction("/redirect"), context))
                .isExactlyInstanceOf(RedirectResponse.class);

        verify(res).setStatus(eq(302));
        verify(res).setHeader(eq("Location"), eq("/redirect"));
    }

    @Test
    public void testAdapterUnauthorized() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new UnauthorizedAction(), context))
                .isExactlyInstanceOf(UnauthorizedResponse.class);
    }

    @Test
    public void testAdapterForbidden() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new ForbiddenAction(), context))
                .isExactlyInstanceOf(ForbiddenResponse.class);
    }

    @Test
    public void testAdapterBadRequest() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new BadRequestAction(), context))
                .isExactlyInstanceOf(BadRequestResponse.class);
    }

    @Test
    public void testAdapterAnyOtherStatus() {
        JavalinHttpActionAdapter.INSTANCE.adapt(new HttpAction(123) {}, context);

        verify(res).setStatus(eq(123));
    }
}