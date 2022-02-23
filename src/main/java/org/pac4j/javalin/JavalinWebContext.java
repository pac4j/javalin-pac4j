package org.pac4j.javalin;

import io.javalin.http.Context;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.WebContextHelper;
import org.pac4j.core.exception.TechnicalException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of a {@link WebContext} in the Context of a Javalin Application.
 * The actual code mimics the behavior of JEEWebContext.
 *
 * @author Maximilian Hippler
 * @author Tim Trense
 * @since 3.0.0
 */
public class JavalinWebContext implements WebContext {
    private final Context javalinCtx;
    private String body;

    public JavalinWebContext( Context javalinCtx ) {
        this.javalinCtx = javalinCtx;
    }

    public Context getNativeContext() { return javalinCtx; }

    public Context getJavalinCtx() {
        return javalinCtx;
    }

    @Override
    public Optional<String> getRequestParameter( String name ) {
        return Optional.ofNullable( this.javalinCtx.req.getParameter( name ) );
    }

    @Override
    public Optional<Object> getRequestAttribute( String name ) {
        return Optional.ofNullable( this.javalinCtx.req.getAttribute( name ) );
    }

    @Override
    public void setRequestAttribute( String name, Object value ) {
        this.javalinCtx.req.setAttribute( name, value );
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return this.javalinCtx.req.getParameterMap();
    }

    @Override
    public Optional<String> getRequestHeader( String name ) {
        Enumeration<String> names = this.javalinCtx.req.getHeaderNames();
        if ( names != null ) {
            while ( names.hasMoreElements() ) {
                String headerName = (String)names.nextElement();
                if ( headerName != null && headerName.equalsIgnoreCase( name ) ) {
                    return Optional.ofNullable( this.javalinCtx.req.getHeader( headerName ) );
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public String getRequestMethod() {
        return this.javalinCtx.req.getMethod();
    }

    @Override
    public String getRemoteAddr() {
        return this.javalinCtx.req.getRemoteAddr();
    }

    @Override
    public void setResponseHeader( String name, String value ) {
        this.javalinCtx.res.setHeader( name, value );
    }

    @Override
    public Optional<String> getResponseHeader( String name ) {
        return Optional.ofNullable( this.javalinCtx.res.getHeader( name ) );
    }

    @Override
    public void setResponseContentType( String content ) {
        this.javalinCtx.res.setContentType( content );
    }

    @Override
    public String getServerName() {
        return this.javalinCtx.req.getServerName();
    }

    @Override
    public int getServerPort() {
        return this.javalinCtx.req.getServerPort();
    }

    @Override
    public String getScheme() {
        return this.javalinCtx.req.getScheme();
    }

    @Override
    public boolean isSecure() {
        return this.javalinCtx.req.isSecure();
    }

    @Override
    public String getRequestURL() {
        String url = this.javalinCtx.req.getRequestURL().toString();
        int idx = url.indexOf( 63 );
        return idx != -1 ? url.substring( 0, idx ) : url;
    }

    @Override
    public String getFullRequestURL() {
        StringBuffer requestURL = this.javalinCtx.req.getRequestURL();
        String queryString = this.javalinCtx.req.getQueryString();
        return queryString == null ? requestURL.toString() : requestURL.append( '?' ).append( queryString ).toString();
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        Collection<Cookie> pac4jCookies = new LinkedHashSet<>();

        Arrays.stream( this.javalinCtx.req.getCookies() )
                .map( c -> {
                    Cookie cookie = new Cookie( c.getName(), c.getValue() );
                    cookie.setDomain( c.getDomain() );
                    cookie.setHttpOnly( c.isHttpOnly() );
                    cookie.setMaxAge( c.getMaxAge() );
                    cookie.setPath( c.getPath() );
                    cookie.setSecure( c.getSecure() );
                    return cookie;
                } )
                .forEach( pac4jCookies::add );
        return pac4jCookies;
    }

    @Override
    public void addResponseCookie( Cookie cookie ) {
        this.javalinCtx.res.addHeader( "Set-Cookie", WebContextHelper.createCookieHeader( cookie ) );
    }

    @Override
    public String getPath() {
        String fullPath = this.javalinCtx.req.getRequestURI();
        if ( fullPath == null ) {
            return "";
        }
        else {
            if ( fullPath.startsWith( "//" ) ) {
                fullPath = fullPath.substring( 1 );
            }

            String context = this.javalinCtx.req.getContextPath();
            return context != null ? fullPath.substring( context.length() ) : fullPath;
        }
    }

    @Override
    public String getRequestContent() {
        if ( this.body == null ) {
            try {
                this.body = this.javalinCtx.req.getReader().lines().reduce( "", String::concat );
            }
            catch ( IOException ex ) {
                throw new TechnicalException( ex );
            }
        }

        return this.body;
    }

    @Override
    public String getProtocol() {
        return this.javalinCtx.req.getProtocol();
    }
}
