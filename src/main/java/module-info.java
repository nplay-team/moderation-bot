import org.jspecify.annotations.NullMarked;

@NullMarked
module moderationbot.main {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.google.guice;
    requires dev.goldmensch.fluava;
    requires io.github._4drian3d.jdwebhooks;
    requires io.github.kaktushose.jda.commands.core;
    requires io.github.kaktushose.jda.commands.extension.guice;
    requires io.github.kaktushose.proteus;
    requires java.desktop;
    requires java.sql;
    requires net.dv8tion.jda;
    requires org.jetbrains.annotations;
    requires org.jspecify;
    requires org.slf4j;
    requires sadu.sadu.datasource.main;
    requires sadu.sadu.mapper.main;
    requires sadu.sadu.postgresql.main;
    requires sadu.sadu.queries.main;
    requires sadu.sadu.updater.main;
}