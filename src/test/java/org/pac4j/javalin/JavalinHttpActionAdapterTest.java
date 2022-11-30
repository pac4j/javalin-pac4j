package org.pac4j.javalin;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpStatus;
import io.javalin.http.RedirectResponse;
import io.javalin.http.UnauthorizedResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.TooManyStaticImports")
class JavalinHttpActionAdapterTest {

    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final Context ctx = mock(Context.class);
    private JavalinWebContext context;

    @BeforeEach
    public void setupMocks() {
        when(ctx.res()).thenReturn(res);
        when(ctx.req()).thenReturn(req);
        context = new JavalinWebContext(ctx);
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
    public void testContextNoJavalinWebContext() {
        final JEEContext jeeContext = new JEEContext(req, res);
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new OkAction(""), jeeContext))
                .hasMessageContaining("not a Javalin web context");
    }

    @Test
    public void testAdapterWithContentAction() {
        JavalinHttpActionAdapter.INSTANCE.adapt(new OkAction("my-content"), context);

        verify(ctx).status(200);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ctx).result(captor.capture());
        assertThat(captor.getValue()).isEqualTo("my-content");
    }

    @Test
    public void testAdapterWithLocationAction() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new FoundAction("/redirect"), context))
                .isExactlyInstanceOf(RedirectResponse.class);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ctx).redirect(captor.capture(), eq(HttpStatus.FOUND));
        assertThat(captor.getValue()).isEqualTo("/redirect");
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

        verify(ctx).status(123);
    }
}