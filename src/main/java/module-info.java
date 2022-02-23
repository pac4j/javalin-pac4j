module javalin.pac4j {
    requires pac4j.core;
    requires io.javalin;
    requires javax.servlet.api;
    requires org.slf4j;
    exports org.pac4j.javalin;
    opens org.pac4j.javalin;
}