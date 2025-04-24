package de.nplay.moderationbot.slowmode;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Optional;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;

import java.time.Duration;

@Interaction
public class SlowmodeCommands {

    @Command(value = "slowmode", desc = "Gibt Informationen zu den aktuellen Slowmode-Einstellungen zurück oder setzt diese.")
    public void slowmodeCommand(
            CommandEvent event,
            @Optional @Param("Wie lang soll der Slowmode sein?") Duration duration
    ) {
        if (duration == null) {
            var slowmode = SlowmodeService.getSlowmode(event.getGuildChannel());
            if (slowmode.isPresent()) {
                event.reply("Der aktuelle Slowmode für diesen Channel beträgt " + slowmode.get().duration() + " Sekunden.");
            } else {
                event.reply("Es ist kein Slowmode für diesen Channel gesetzt.");
            }
        } else {
            SlowmodeService.setSlowmode(event.getGuildChannel(), (int) duration.toSeconds());
            event.reply("Der Slowmode für diesen Channel wurde auf " + duration.toSeconds() + " Sekunden gesetzt.");
        }
    }

    @Command(value = "slowmode remove", desc = "Entfernt den Slowmode für diesen Channel.")
    public void slowmodeRemoveCommand(
            CommandEvent event
    ) {
        SlowmodeService.removeSlowmode(event.getGuildChannel());
        event.reply("Der Slowmode für diesen Channel wurde entfernt.");
    }

}
