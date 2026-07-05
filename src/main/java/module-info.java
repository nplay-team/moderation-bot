import org.jspecify.annotations.NullMarked;

@NullMarked
module moderationbot.main {
    requires net.dv8tion.jda;
    requires io.github._4drian3d.jdwebhooks;

    requires io.github.kaktushose.jdac.core;
    requires io.github.kaktushose.jdac.guice;
    requires dev.goldmensch.fluava;
    requires io.github.kaktushose.proteus;
    requires com.google.guice;

    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    requires de.chojo.sadu.core;
    requires de.chojo.sadu.datasource;
    requires de.chojo.sadu.mapper;
    requires de.chojo.sadu.postgresql;
    requires de.chojo.sadu.queries;
    requires de.chojo.sadu.updater;

    requires org.jspecify;

    requires java.desktop;
    requires java.sql;
    requires dev.goldmensch.propane;

    // required for logback
    exports de.nplay.moderationbot.logging;
}
