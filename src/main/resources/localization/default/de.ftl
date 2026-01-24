# Terms
-channel = kanal
-duration = dauer
-target = user
-paragraph = paragraph
-paragraph-description = Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?

# Permissions
permissions-none = Keine Berechtigungen (löscht automatisch alle)
permissions-select = Wähle eine oder mehrere Berechtigungen aus
## List
permissions-list-description = Zeigt die Berechtigungen eines Benutzers an
permissions-list-options-member-name = { -target }
permissions-list-options-member-description = Der Benutzer, dessen Berechtigungen abgerufen werden sollen.
## Member
permissions-manage-member-description = Verwaltet die Berechtigungen eines Benutzers.
permissions-manage-member-options-member-name = { -target }
permissions-manage-member-options-member-description = Der Benutzer, dessen Berechtigungen bearbeitet werden sollen.
## Role
permissions-manage-role-description = Verwaltet die Berechtigungen einer Rolle.
permissions-manage-role-options-role-name = rolle
permissions-manage-role-options-role-description = Die Rolle, dessen Berechtigungen bearbeitet werden sollen.

## BulkDelete
mod-purge-messages-description = Löscht eine bestimmte Anzahl an Nachrichten gleichzeitig
mod-purge-messages-options-amount-name = anzahl
mod-purge-messages-options-amount-description = Anzahl der Nachrichten die gelöscht werden sollen

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

# Spielersuche
## Ausschluss
spielersuche-ausschluss-description = Schließt einen User von der Spielersuche aus und verwarnt ihn
spielersuche-ausschluss-options-target-name = { -target }
spielersuche-ausschluss-options-target-description = Der User, der ausgeschlossen werden soll
spielersuche-ausschluss-options-paragraph-name = { -paragraph }
spielersuche-ausschluss-options-paragraph-description = { -paragraph-description }
spielersuche-ausschluss-reason = Du hast erneut gegen die Spielersucheregeln verstoßen **und wurdest von der Spielersuche ausgeschlossen!**
## Freigeben
spielersuche-freigeben-description = Hebt den Ausschluss eines Users von der Spielersuche auf
spielersuche-freigeben-options-target-name = { -target }
spielersuche-freigeben-options-target-description = Der User, dessen Ausschluss aufgehoben werden soll

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