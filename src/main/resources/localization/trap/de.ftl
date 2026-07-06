## place
trap-place-name = platzieren
trap-place-description = Platziert eine Bot-Falle in dem aktuellen Textkanal.
trap-place-options-channel-description = Der Textkanal indem die Falle platziert werden soll.

placed =
    ### Falle platziert in { $channel }
    Es wurde erfolgreich eine Falle platziert. Sobald nun jemand eine Nachricht in diesem Kanal sendet, wird er automatisch gekickt. Immun dagegen sind alle, die Nachrichten verwalten können.

## info
trap-info-name = info
trap-info-description = Abfrage ob in dem aktuellen Textkanal eine Bot-Falle platziert ist.
trap-info-options-channel-description = Der Textkanal indem abgefragt werden soll.
info =
    ### Bot-Fallen in { $channel }
    { $placed ->
        [true] In diesem Kanal ist derzeit eine Falle **AKTIV**.
        *[false] In diesem Kanal sind **KEINE** Fallen aktiv.
    }

## remove
trap-remove-name = entfernen
trap-remove-description = Entfernt eine Bot-Falle aus dem aktuellen Textkanal.
trap-remove-options-channel-description = Der Textkanal indem die Falle entfernt werden soll.
removed =
    ### Falle aus { $channel } entfernt
    Die Falle wurde erfolgreich entfernt, es werden nun keine Nutzer mehr gekickt.

## exceptions
not-trap-channel =
    ### Fehler
    In dem Kanal { $channel } ist keine Falle platziert die gelöscht werden kann!

already-trap-channel =
    ### Fehler
    In dem Kanal { $channel } ist bereits eine Falle platziert!

## Internals
kick-reason = Verdacht auf Spam-Bot. Wenn du ein echter Mensch bist, kannst du jederzeit unter discord.gg/nplay den Server wieder beitreten.