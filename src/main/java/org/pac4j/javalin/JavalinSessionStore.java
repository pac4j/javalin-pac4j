package org.pac4j.javalin;

import io.javalin.http.Context;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of a {@link SessionStore} in the Context of a Javalin Application.
 * The actual code mimics the behavior of JEESessionStore.
 *
 * @author Tim Trense
 * @since 4.1.0
 */
public class JavalinSessionStore implements SessionStore {
    public static final JavalinSessionStore INSTANCE = new JavalinSessionStore();
    private static final Logger LOGGER = LoggerFactory.getLogger( JavalinSessionStore.class );
    protected Context providedContext;

    protected JavalinSessionStore() {
        this( null );
    }

    protected JavalinSessionStore( Context providedContext ) {
        this.providedContext = providedContext;
    }

    protected Optional<Context> getNativeContext( WebContext context ) {
        if ( this.providedContext != null ) {
            LOGGER.debug( "Provided context: {}", this.providedContext );
            return Optional.of( this.providedContext );
        }
        else {
            JavalinWebContext javalinWebContext = (JavalinWebContext)context;
            Context nativeContext = javalinWebContext.getNativeContext();
            LOGGER.debug( "Native context: {}", nativeContext );
            return Optional.ofNullable( nativeContext );
        }
    }

    @Override
    public Optional<String> getSessionId( WebContext context, boolean createSession ) {
        Optional<Context> nativeContext = this.getNativeContext( context );
        if ( nativeContext.isPresent() ) {
            String sessionId = nativeContext.get().req.getSession( createSession ).getId();
            LOGGER.debug( "Get sessionId: {}", sessionId );
            return Optional.of( sessionId );
        }
        else {
            LOGGER.debug( "No sessionId" );
            return Optional.empty();
        }
    }

    @Override
    public Optional<Object> get( WebContext context, String key ) {
        Optional<Context> nativeContext = this.getNativeContext( context );
        if ( nativeContext.isPresent() ) {
            Object value = nativeContext.get().sessionAttribute( key );
            LOGGER.debug( "Get value: {} for key: {}", value, key );
            return Optional.ofNullable( value );
        }
        else {
            LOGGER.debug( "Can't get value for key: {}, no session available", key );
            return Optional.empty();
        }
    }

    @Override
    public void set( WebContext context, String key, Object value ) {
        Optional<Context> nativeContext = this.getNativeContext( context );
        LOGGER.debug( "Set key: {} for value: {}", key, value );
        nativeContext.ifPresent( it -> {
            it.req.getSession( true );
            it.sessionAttribute( key, value );
        } );
    }

    @Override
    public boolean destroySession( WebContext context ) {
        this.getNativeContext( context ).ifPresent( ctx -> {
            try {
                ctx.req.getSession().invalidate();
            }
            catch ( NullPointerException ignored ) {
            }
        } );
        return true;
    }

    @Override
    public Optional<Object> getTrackableSession( WebContext context ) {
        Optional<Context> nativeContext = this.getNativeContext( context );
        if ( nativeContext.isPresent() ) {
            LOGGER.debug( "Return trackable session: {}", nativeContext.get() );
            return Optional.of( nativeContext.get() );
        }
        else {
            LOGGER.debug( "No trackable session" );
            return Optional.empty();
        }
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession( WebContext context, Object trackableSession ) {
        if ( trackableSession != null ) {
            LOGGER.debug( "Rebuild session from trackable session: {}", trackableSession );
            return Optional.of( new JavalinSessionStore( (Context)trackableSession ) );
        }
        else {
            LOGGER.debug( "Unable to build session from trackable session" );
            return Optional.empty();
        }
    }

    @Override
    public boolean renewSession( WebContext context ) {
        Context nativeContext = ( (JavalinWebContext)context ).getNativeContext();

        Map<String, Object> attributesCache = Collections.unmodifiableMap( nativeContext.sessionAttributeMap() );

        try {
            nativeContext.req.getSession().invalidate();
        }
        catch ( NullPointerException ignored ) {
        }

        attributesCache.forEach( nativeContext::sessionAttribute );
        return true;
    }
}
