package org.pac4j.javalin.example;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.rendering.template.JavalinVelocity;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.javalin.CallbackHandler;
import org.pac4j.javalin.JavalinHttpActionAdapter;
import org.pac4j.javalin.LogoutHandler;
import org.pac4j.javalin.SecurityHandler;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.JEEFrameworkParameters;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.rendering.template.TemplateUtil.model;

public class JavalinPac4jExample {
    private static final String JWT_SALT = "12345678901234567890123456789012";
    private static final Logger logger = LoggerFactory.getLogger(JavalinPac4jExample.class);

    public static void main(String[] args) {

        final Config config = new ExampleConfigFactory(JWT_SALT).build();
        CallbackHandler callback = new CallbackHandler(config, null, true);
        SecurityHandler facebookSecurityHandler = new SecurityHandler(config, "FacebookClient", "", "excludedPath");

        JavalinVelocity.init();
        Javalin.create()
                .routes(() -> {

                    get("/", ctx -> index(ctx, config));
                    get("/callback", callback);
                    post("/callback", callback);

                    before("/facebook", facebookSecurityHandler);
                    get("/facebook", ctx -> protectedPage(ctx, config));

                    before("/facebook/*", facebookSecurityHandler);
                    get("/facebook/notprotected", ctx -> protectedPage(ctx, config)); // excluded in ExampleConfigFactory

                    before("/facebookadmin", new SecurityHandler(config, "FacebookClient", "admin"));
                    get("/facebookadmin", ctx -> protectedPage(ctx, config));

                    before("/facebookcustom", new SecurityHandler(config, "FacebookClient", "custom"));
                    get("/facebookcustom", ctx -> protectedPage(ctx, config));

                    before("/twitter", new SecurityHandler(config, "TwitterClient,FacebookClient"));
                    get("/twitter", ctx -> protectedPage(ctx, config));

                    before("/form", new SecurityHandler(config, "FormClient"));
                    get("/form", ctx -> protectedPage(ctx, config));

                    before("/basicauth", new SecurityHandler(config, "IndirectBasicAuthClient"));
                    get("/basicauth", ctx -> protectedPage(ctx, config));

                    before("/cas", new SecurityHandler(config, "CasClient"));
                    get("/cas", ctx -> protectedPage(ctx, config));

                    before("/saml2", new SecurityHandler(config, "SAML2Client"));
                    get("/saml2", ctx -> protectedPage(ctx, config));

                    before("/oidc", new SecurityHandler(config, "OidcClient"));
                    get("/oidc", ctx -> protectedPage(ctx, config));

                    before("/protected", new SecurityHandler(config, null));
                    get("/protected", ctx -> protectedPage(ctx, config));

                    before("/dba", new SecurityHandler(config, "DirectBasicAuthClient,ParameterClient"));
                    get("/dba", ctx -> protectedPage(ctx, config));

                    before("/rest-jwt", new SecurityHandler(config, "ParameterClient"));
                    get("/rest-jwt", ctx -> protectedPage(ctx, config));

                    get("/jwt", JavalinPac4jExample::jwt);

                    get("/login-form", ctx -> form(ctx, config));
                    get("/logout", localLogoutHandler(config));
                    get("/central-logout", centralLogoutHandler(config));
                    get("/force-login", ctx -> forceLogin(ctx, config));
                    before("/body", new SecurityHandler(config, "HeaderClient"));
                    post("/body", ctx -> {
                        logger.debug("Body: " + ctx.body());
                        ctx.result("done: " + getProfiles(ctx, config));
                    });

                }).exception(Exception.class, (e, ctx) -> {
            logger.error("Unexpected exception", e);
            ctx.result(e.toString());
        }).start(8080);
    }

    private static LogoutHandler centralLogoutHandler(Config config) {
        LogoutHandler centralLogout = new LogoutHandler(config);
        centralLogout.defaultUrl = "http://localhost:8080/?defaulturlafterlogoutafteridp";
        centralLogout.logoutUrlPattern = "http://localhost:8080/.*";
        centralLogout.localLogout = false;
        centralLogout.centralLogout = true;
        centralLogout.destroySession = true;
        return centralLogout;
    }

    private static LogoutHandler localLogoutHandler(Config config) {
        LogoutHandler localLogout = new LogoutHandler(config, "/?defaulturlafterlogout");
        localLogout.destroySession = true;
        return localLogout;
    }

    private static void index(Context ctx, Config config) {
        ctx.render("/templates/index.vm", model("profiles", getProfiles(ctx, config)));
    }

    private static void jwt(Context ctx) {
        ProfileManager manager = new ProfileManager(new JEEContext(ctx.req(), ctx.res()), JEESessionStore.INSTANCE);
        Optional<CommonProfile> profile = manager.getProfile(CommonProfile.class);
        String token = "";
        if (profile.isPresent()) {
            JwtGenerator generator = new JwtGenerator(new SecretSignatureConfiguration(JWT_SALT));
            token = generator.generate(profile.get());
        }
        ctx.render("/templates/jwt.vm", model("token", token));
    }

    private static void form(Context ctx, Config config) {
        Client client = config.getClients().findClient("FormClient").orElse(null);
        if(client == null) throw new IllegalStateException("Client not found");
        FormClient formClient = (FormClient) client;

        ctx.render("/templates/loginForm.vm", model("callbackUrl", formClient.getCallbackUrl() + "?client_name=FormClient"));
    }

    private static void protectedPage(Context ctx, Config config) {
        ctx.render("/templates/protectedPage.vm", model("profiles", getProfiles(ctx, config)));
    }

    private static List<UserProfile> getProfiles(Context ctx, Config config) {
        JEEFrameworkParameters parameters = new JEEFrameworkParameters(ctx.req(), ctx.res());
        return config.getProfileManagerFactory().apply(
                config.getWebContextFactory().newContext(parameters),
                config.getSessionStoreFactory().newSessionStore(parameters)
        ).getProfiles();
    }

    private static void forceLogin(Context ctx, Config config) {
        WebContext context = config.getWebContextFactory().newContext(new JEEFrameworkParameters(ctx.req(), ctx.res()));
        String clientName = context.getRequestParameter("FormClient").orElse(null);
        if(clientName == null) throw new IllegalStateException("Client name not found");

        Client client = config.getClients().findClient(clientName).orElse(null);
        if(client == null) throw new IllegalStateException("Client not found");

        HttpAction action;
        try {
            action = client.getRedirectionAction(new CallContext(context, JEESessionStore.INSTANCE)).get();
        } catch (HttpAction e) {
            action = e;
        }
        JavalinHttpActionAdapter.INSTANCE.adapt(action, context);
    }
}
