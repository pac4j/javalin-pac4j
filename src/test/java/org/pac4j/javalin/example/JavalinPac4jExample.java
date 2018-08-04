package org.pac4j.javalin.example;

import io.javalin.Context;
import io.javalin.Javalin;
import java.util.List;
import java.util.Optional;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.javalin.CallbackHandler;
import org.pac4j.javalin.LogoutHandler;
import org.pac4j.javalin.Pac4jContext;
import org.pac4j.javalin.SecurityHandler;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.rendering.template.TemplateUtil.model;

public class JavalinPac4jExample {

    private static String JWT_SALT = "12345678901234567890123456789012";

    private static Logger logger = LoggerFactory.getLogger(JavalinPac4jExample.class);

    public static void main(String[] args) {

        Config config = new ExampleConfigFactory(JWT_SALT).build();
        CallbackHandler callback = new CallbackHandler(config, null, true);
        SecurityHandler facebookSecurityHandler = new SecurityHandler(config, "FacebookClient", "", "excludedPath");

        Javalin.create()
            .port(8080)
            .routes(() -> {

            get("/", JavalinPac4jExample::index);
            get("/callback", callback);
            post("/callback", callback);

            before("/facebook", facebookSecurityHandler);
            get("/facebook", JavalinPac4jExample::protectedPage);

            before("/facebook/*", facebookSecurityHandler);
            get("/facebook/notprotected", JavalinPac4jExample::protectedPage); // excluded in ExampleConfigFactory

            before("/facebookadmin", new SecurityHandler(config, "FacebookClient", "admin"));
            get("/facebookadmin", JavalinPac4jExample::protectedPage);

            before("/facebookcustom", new SecurityHandler(config, "FacebookClient", "custom"));
            get("/facebookcustom", JavalinPac4jExample::protectedPage);

            before("/twitter", new SecurityHandler(config, "TwitterClient,FacebookClient"));
            get("/twitter", JavalinPac4jExample::protectedPage);

            before("/form", new SecurityHandler(config, "FormClient"));
            get("/form", JavalinPac4jExample::protectedPage);

            before("/basicauth", new SecurityHandler(config, "IndirectBasicAuthClient"));
            get("/basicauth", JavalinPac4jExample::protectedPage);

            before("/cas", new SecurityHandler(config, "CasClient"));
            get("/cas", JavalinPac4jExample::protectedPage);

            before("/saml2", new SecurityHandler(config, "SAML2Client"));
            get("/saml2", JavalinPac4jExample::protectedPage);

            before("/oidc", new SecurityHandler(config, "OidcClient"));
            get("/oidc", JavalinPac4jExample::protectedPage);

            before("/protected", new SecurityHandler(config, null));
            get("/protected", JavalinPac4jExample::protectedPage);

            before("/dba", new SecurityHandler(config, "DirectBasicAuthClient,ParameterClient"));
            get("/dba", JavalinPac4jExample::protectedPage);

            before("/rest-jwt", new SecurityHandler(config, "ParameterClient"));
            get("/rest-jwt", JavalinPac4jExample::protectedPage);

            get("/jwt", JavalinPac4jExample::jwt);

            get("/login-form", ctx -> form(ctx, config));
            get("/logout", localLogoutHandler(config));
            get("/central-logout", centralLogoutHandler(config));
            get("/force-login", ctx -> forceLogin(ctx, config));
            before("/body", new SecurityHandler(config, "HeaderClient"));
            post("/body", ctx -> {
                logger.debug("Body: " + ctx.body());
                ctx.result("done: " + getProfiles(ctx));
            });

        }).exception(Exception.class, (e, ctx) -> {
            logger.error("Unexpected exception", e);
            ctx.result(e.toString());
        }).start();

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

    private static void index(Context ctx) {
        ctx.render("/templates/index.vm", model("profiles", getProfiles(ctx)));
    }

    private static void jwt(Context ctx) {
        Pac4jContext context = new Pac4jContext(ctx);
        ProfileManager manager = new ProfileManager(context);
        Optional<CommonProfile> profile = manager.get(true);
        String token = "";
        if (profile.isPresent()) {
            JwtGenerator generator = new JwtGenerator(new SecretSignatureConfiguration(JWT_SALT));
            token = generator.generate(profile.get());
        }
        ctx.render("/templates/jwt.vm", model("token", token));
    }

    private static void form(Context ctx, Config config) {
        FormClient formClient = config.getClients().findClient(FormClient.class);
        ctx.render("/templates/loginForm.vm", model("callbackUrl", formClient.getCallbackUrl()));
    }

    private static void protectedPage(Context ctx) {
        ctx.render("/templates/protectedPage.vm", model("profiles", getProfiles(ctx)));
    }

    private static List<CommonProfile> getProfiles(Context ctx) {
        return new ProfileManager(new Pac4jContext(ctx)).getAll(true);
    }

    private static void forceLogin(Context ctx, Config config) {
        Pac4jContext context = new Pac4jContext(ctx);
        String clientName = context.getRequestParameter("FormClient");
        Client client = config.getClients().findClient(clientName);
        HttpAction action;
        try {
            action = client.redirect(context);
        } catch (HttpAction e) {
            action = e;
        }
        config.getHttpActionAdapter().adapt(action.getCode(), context);
    }
}
