package org.pac4j.framework.adapter;

import org.pac4j.core.adapter.DefaultFrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.javalin.JavalinContextFactory;
import org.pac4j.javalin.JavalinHttpActionAdapter;
import org.pac4j.jee.context.session.JEESessionStoreFactory;

public class FrameworkAdapterImpl extends DefaultFrameworkAdapter {

    @Override
    public void applyDefaultSettingsIfUndefined(final Config config) {
        super.applyDefaultSettingsIfUndefined(config);

        config.setWebContextFactoryIfUndefined(JavalinContextFactory.INSTANCE);
        config.setSessionStoreFactoryIfUndefined(JEESessionStoreFactory.INSTANCE);
        config.setHttpActionAdapterIfUndefined(JavalinHttpActionAdapter.INSTANCE);
    }

    @Override
    public String toString() {
        return "Javalin";
    }
}
