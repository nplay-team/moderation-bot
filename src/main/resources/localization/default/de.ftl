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

## Adapters / Validators
invalid-duration = Die angegebene Dauer ist ungültig. Bitte gib eine gültige Dauer an!
duration-too-long = Die angegebene Dauer ist zu lang! Die maximale Dauer beträgt { $duration }!
invalid-link = Der angegebene Link ist nicht gültig!
invalid-act = Die Moderationshandlung mit der ID **#{ $id }** existiert nicht!