## info
slowmode-info-description = Gibt Informationen zu den aktuellen Slowmode-Einstellungen zurück oder setzt diese.
slowmode-info-options-channel-name = kanal
slowmode-info-options-channel-description = Der Channel, für den die Informationen angezeigt werden sollen.
not-set =
    ### Fehler
    Für den Kanal { $channel } wurde kein Slowmode gesetzt!
info =
    ### Slowmode Info { $channel }
    Der Slowmode für diesen Kanal beträgt **{ $duration }**!

## Set
slowmode-set-description = Setzt den Slowmode für diesen oder einen anderen Kanal.
slowmode-set-options-duration-name = dauer
slowmode-set-options-duration-description = = Wie lang soll der Slowmode sein?
slowmode-set-options-channel-name = kanal
slowmode-set-options-channel-description = Der Channel, für den der Slowmode gesetzt werden soll.
set =
    ### Slowmode gesetzt { $channel }
    Der Slowmode für diesen Kanal wurde auf **{ $duration }** gesetzt!

## Remove
slowmode-remove-description = Entfernt den Slowmode für diesen Channel.
slowmode-remove-options-channel-name = kanal
slowmode-remove-options-channel-description = Der Channel, für den der Slowmode entfernt werden soll.
remove =
    ### Slowmode entfernt { $channel }
    Der Slowmode für diesen Kanal wurde entfernt!

## User Info
removed =
    ### Slowmode - Nachricht entfernt
    Deine Nachricht im Kanal { $channel } wurde gelöscht, weil dieser Kanal sich im Slowmode befindet!
    Zwischen den Nachrichten muss eine Wartezeit von **{ $duration }** liegen.
    .next =
    { "**Nächste Nachricht möglich**" }
    { $next }
    .last =
    { "**Letzte Nachricht**" }
    { $last }
