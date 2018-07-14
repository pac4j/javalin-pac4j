package org.pac4j.javalin.example;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;

public class TrivialUserPassAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    private String testUsername;
    private String testPassword;

    public TrivialUserPassAuthenticator(String testUsername, String testPassword) {
        this.testUsername = testUsername;
        this.testPassword = testPassword;
    }

    @Override
    public void validate(UsernamePasswordCredentials credentials, WebContext context) {
        if (testUsername.equals(credentials.getUsername()) && testPassword.equals(credentials.getPassword())) {
            CommonProfile profile = new CommonProfile();
            profile.setId(credentials.getUsername());
            profile.addAttribute("username", credentials.getUsername());
            credentials.setUserProfile(profile);
        } else {
            throw new CredentialsException("Invalid credentials");
        }
    }

}
