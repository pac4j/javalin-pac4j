package org.pac4j.javalin.example;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;

import java.util.Optional;

public class TrivialUserPassAuthenticator implements Authenticator {
    private final String testUsername;
    private final String testPassword;

    public TrivialUserPassAuthenticator(String testUsername, String testPassword) {
        this.testUsername = testUsername;
        this.testPassword = testPassword;
    }

    @Override
    public Optional<Credentials> validate(Credentials creds, WebContext context, SessionStore sessionStore) {
        if (creds instanceof UsernamePasswordCredentials == false) {
            throw new CredentialsException("not a username password credential");
        }
        UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) creds;
        if (testUsername.equals(credentials.getUsername()) && testPassword.equals(credentials.getPassword())) {
            CommonProfile profile = new CommonProfile();
            profile.setId(credentials.getUsername());
            profile.addAttribute("username", credentials.getUsername());
            credentials.setUserProfile(profile);
            return Optional.of(credentials);
        } else {
            throw new CredentialsException("Invalid credentials");
        }
    }
}
