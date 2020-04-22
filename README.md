<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-javalin.png" width="300" />
</p>

The `javalin-pac4j` project is an easy and powerful security library for [Javalin](https://javalin.io) web applications which supports 
authentication and authorization, but also logout and advanced features like session fixation and CSRF protection.

It's based on Java 8 and the **[pac4j security engine](https://github.com/pac4j/pac4j) v4**. 
It's available under the Apache 2 license.

[**Main concepts and components:**](http://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](http://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. 
An indirect client is for UI authentication while a direct client is for web services authentication:

&#9656; OAuth - SAML - CAS - OpenID Connect - HTTP - OpenID - Google App Engine - LDAP - SQL - JWT - MongoDB - Stormpath - IP address

2) An [**authorizer**](http://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles / permissions - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) The `SecurityHandler` protects an url by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

4) The `CallbackHandler` finishes the login process for an indirect client

5) The `LogoutHandler` handles the logout process.

Just follow these easy steps to secure your Javalin application:

### 1) Add the required dependencies (`javalin-pac4j` and `pac4j-*` libraries)

You need to add a dependency for:
 
- the `javalin-pac4j` library (<em>groupId</em>: **org.pac4j**, *version*: **3.0.0**)
- the appropriate `pac4j` [submodules](http://www.pac4j.org/docs/clients.html) (<em>groupId</em>: **org.pac4j**, *version*: **4.0.0**): `pac4j-oauth` for OAuth support (Facebook, Twitter...), `pac4j-cas` for CAS support, `pac4j-ldap` for LDAP authentication, etc.

All released artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).

### 2) Define the configuration

The configuration (`org.pac4j.core.config.Config`) contains all the clients and authorizers required by the application to handle security.

* [ConfigFactory example](https://github.com/pac4j/javalin-pac4j/blob/master/src/test/java/org/pac4j/javalin/example/ExampleConfigFactory.java)
* [Authorizer example](https://github.com/pac4j/javalin-pac4j/blob/master/src/test/java/org/pac4j/javalin/example/CustomAuthorizer.java)
* [Authenticator example](https://github.com/pac4j/javalin-pac4j/blob/master/src/test/java/org/pac4j/javalin/example/TrivialUserPassAuthenticator.java)

### 3) Protect urls 

Create an implementation of `SecurityHandler` and attach it to a `before` handler that covers the URLs you want to protect.
The [example app](https://github.com/pac4j/javalin-pac4j/blob/master/src/test/java/org/pac4j/javalin/example/JavalinPac4jExample.java) shows an implementation for every client.

### 4) Define the callback endpoint only for indirect clients (`CallbackHandler`)

For indirect clients (like Facebook), the user is redirected to an external identity provider for login and then back to the application.
The [example app](https://github.com/pac4j/javalin-pac4j/blob/master/src/test/java/org/pac4j/javalin/example/JavalinPac4jExample.java) shows an implementation.

### 5) Get the user profile (via `HttpServletRequest` or `ProfileManager`)

The [example app](https://github.com/pac4j/javalin-pac4j/blob/master/src/test/java/org/pac4j/javalin/example/JavalinPac4jExample.java) shows an implementation.

### 6) Logout (`LogoutHandler`)

You can have a local logout or a global logout. 
The [example app](https://github.com/pac4j/javalin-pac4j/blob/master/src/test/java/org/pac4j/javalin/example/JavalinPac4jExample.java) shows both implementations.

## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)


## Development

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/javalin-pac4j.png?branch=master)](https://travis-ci.org/pac4j/javalin-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the Maven *pom.xml* file for example:

```xml
<repositories>
  <repository>
    <id>sonatype-nexus-snapshots</id>
    <name>Sonatype Nexus Snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```
