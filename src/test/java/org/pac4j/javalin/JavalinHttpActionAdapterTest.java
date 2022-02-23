package org.pac4j.javalin;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.RedirectResponse;
import io.javalin.http.UnauthorizedResponse;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.BadRequestAction;
import org.pac4j.core.exception.http.ForbiddenAction;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.core.exception.http.UnauthorizedAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("PMD.TooManyStaticImports")
class JavalinHttpActionAdapterTest {

    private HttpServletRequest req = mock(HttpServletRequest.class);
    private HttpServletResponse res = mock(HttpServletResponse.class);
    private Context ctx = new Context(req, res, Collections.emptyMap());
    private JavalinWebContext context = new JavalinWebContext(ctx);

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
        final WebContext otherContext = mock( WebContext.class );
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new OkAction(""), otherContext))
                .hasMessageContaining("not a Javalin web context");
    }

    @Test
    public void testAdapterWithContentAction() {
        JavalinHttpActionAdapter.INSTANCE.adapt(new OkAction("my-content"), context);

        verify(res).setStatus(200);
        assertThat(ctx.resultString()).isEqualTo("my-content");
    }

    @Test
    public void testAdapterWithLocationAction() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(new FoundAction("/redirect"), context))
                .isExactlyInstanceOf(RedirectResponse.class);

        verify(res).setHeader(eq("Location"), eq("/redirect"));
        verify(res).setStatus(302);
    }

    @Test
    public void testAdapterUnauthorized() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(UnauthorizedAction.INSTANCE, context))
                .isExactlyInstanceOf(UnauthorizedResponse.class);
    }

    @Test
    public void testAdapterForbidden() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(ForbiddenAction.INSTANCE, context))
                .isExactlyInstanceOf(ForbiddenResponse.class);
    }

    @Test
    public void testAdapterBadRequest() {
        assertThatThrownBy(() -> JavalinHttpActionAdapter.INSTANCE.adapt(BadRequestAction.INSTANCE, context))
                .isExactlyInstanceOf(BadRequestResponse.class);
    }

    @Test
    public void testAdapterAnyOtherStatus() {
        JavalinHttpActionAdapter.INSTANCE.adapt(new HttpAction(123) {}, context);

        verify(res).setStatus(123);
    }
}