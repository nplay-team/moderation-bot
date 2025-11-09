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

    requires sadu.sadu.datasource.main;
    requires sadu.sadu.mapper.main;
    requires sadu.sadu.postgresql.main;
    requires sadu.sadu.queries.main;
    requires sadu.sadu.updater.main;

    requires org.jspecify;

    requires java.desktop;
    requires java.sql;
}
