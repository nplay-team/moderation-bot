# Terms
-channel = kanal
-duration = dauer
-target = user
-paragraph = paragraph
-paragraph-description = Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?

## BulkDelete
mod-purge-messages-description = Löscht eine bestimmte Anzahl an Nachrichten gleichzeitig
mod-purge-messages-options-amount-name = anzahl
mod-purge-messages-options-amount-description = Anzahl der Nachrichten die gelöscht werden sollen
purge-success =
    ### Erfolgreich
    Die letzten **{ $amount }** Nachrichten wurden erfolgreich gelöscht!

## Detail
mod-detail-description = Zeigt mehr Informationen zu einer Moderationshandlung an
mod-detail-options-moderation_act-name = id
mod-detail-options-moderation_act-description = Die ID der Moderationshandlung.

## Modlog
mod-log-description = Zeigt den Modlog eines Mitglieds an.
mod-log-options-target-name = { -target }
mod-log-options-target-description = Der User, dessen Modlog abgerufen werden soll.
mod-log-options-page-name = seite
mod-log-options-page-description = Die Seite, die angezeigt werden soll
mod-log-options-count-name = seiten
mod-log-options-count-description = Wie viele Moderationshandlungen pro Seite angezeigt werden sollen (max. 25)

# Notes
## Create
notes-create-description = Erstellt eine Notiz über einen Benutzer
notes-create-options-target-name = { -target }
notes-create-options-target-description = Zu welchem Benutzer soll eine Notiz erstellt werden?

## List
notes-list-description = Listet alle Notizen eines Benutzers auf
notes-list-options-target-name = { -target }
notes-list-options-target-description = Welcher Benutzer soll aufgelistet werden?

## Delete
notes-delete-description = Löscht eine Notiz
notes-delete-options-note_id-name = id
notes-delete-options-note_id-description = Welche Notiz soll gelöscht werden?


# Slowmode
## Info
slowmode-info-description = Gibt Informationen zu den aktuellen Slowmode-Einstellungen zurück oder setzt diese.
slowmode-info-options-channel-name = { -channel }
slowmode-info-options-channel-description = Der Channel, für den die Informationen angezeigt werden sollen.

## Set
slowmode-set-description = Setzt den Slowmode für diesen oder einen anderen Kanal.
slowmode-set-options-duration-name = { -duration }
slowmode-set-options-duration-description = = Wie lang soll der Slowmode sein?
slowmode-set-options-channel-name = { -channel }
slowmode-set-options-channel-description = Der Channel, für den der Slowmode gesetzt werden soll.

## Remove
slowmode-remove-description = Entfernt den Slowmode für diesen Channel.
slowmode-remove-options-channel-name = { -channel }
slowmode-remove-options-channel-description = Der Channel, für den der Slowmode entfernt werden soll.

# Embeds
## Modlog
reverted-at-inline = *Aufgehoben am: { $revertedAt }*
revoke-at-inline = Aktiv bis: { $revokeAt }
duration-inline = Dauer: { $duration }
reverted-at = Revidiert am
reverted-by = Revidierender Moderator
reverting-reason = Revidierungsgrund
revoke-at-field = Aufhebung
rule = Regel
reference-message = Referenznachricht
id = ID
act-target = Betroffener Nutzer
act-reason = Begründung
active-until = Aktiv bis

## ModerationActType
warn = Verwarnung
kick = Kick
timeout = Timeout
temp-ban = Temporärer Bann
ban = Bann

## Adapters
invalid-duration = Die angegebene Dauer ist ungültig. Bitte gib eine gültige Dauer an!
invalid-link = Der angegebene Link ist nicht gültig!
invalid-act = Die Moderationshandlung mit der ID **#{ $id }** existiert nicht!