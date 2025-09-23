# Terms
-channel = kanal
-duration = dauer
-target = user
-paragraph = paragraph
-paragraph-description = Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?

# Config
## General
no-value-set = Nicht gesetzt
## Set Rolle
config-set-spielersuche-ausschluss-rolle-description = Setzt die Rolle für die Spielersuche Auschluss Funktion.
config-set-spielersuche-ausschluss-rolle-options-role-name = rolle
config-set-spielersuche-ausschluss-rolle-options-role-description = Die Rolle, welche der User erhalten soll, wenn er von der Spielersuche ausgeschlossen wird.
## Set Serverlog
config-set-serverlog-kanal-description = Setzt den Textkanal für den Serverlog.
config-set-serverlog-kanal-options-channel-name = { -channel }
config-set-serverlog-kanal-options-channel-description = Der Text-Kanal in denen die Bot-Logs gesendet werden sollen.
## List
config-list-description = Listet alle Konfigurationen auf


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


# Moderation
## General
-link = nachrichtenlink
-link-description = Link zu einer Nachricht, die referenziert werden soll.
-prune-duration = Für wie viele Tage in der Vergangenheit sollen Nachrichten dieses Users gelöscht werden?.
-del-days = del_days
-del-days-description = Für wie viele Tage in der Vergangenheit sollen Nachrichten dieses Users gelöscht werden?

reason-title = Begründung angeben
reason-field = Begründung der Moderationshandlung
reason-duration-title = Begründung und Dauer angeben
duration-field = Dauer der Moderationshandlung
invalid-duration = Die angegebene Dauer ist ungültig. Bitte gib eine gültige Dauer an.
invalid-duration-limit = Die angegebene Dauer ist zu lang. Bitte gib eine Dauer von maximal 28 Tagen an.

## Warn
mod-warn-description = Verwarnt einen Benutzer
mod-warn-options-target-name = { -target }
mod-warn-options-target-description = Der Benutzer, der verwarnt werden soll.
mod-warn-options-paragraph-name = { -paragraph }
mod-warn-options-paragraph-description = { -paragraph-description }
mod-warn-options-message_link-name = { -link }
mod-warn-options-message_link-description = { -link-description }

## Timeout
mod-timeout-description = Versetzt einen Benutzer in den Timeout
mod-timeout-options-target-name = { -target }
mod-timeout-options-target-description = Der Benutzer, den in den Timeout versetzt werden soll.
mod-timeout-options-until-name = { -duration }
mod-timeout-options-until-description = Für wie lange der Timeout andauern soll (max. 28 Tage).
mod-timeout-options-paragraph-name = { -paragraph }
mod-timeout-options-paragraph-description = { -paragraph-description }
mod-timeout-options-message_link-name = { -link }
mod-timeout-options-message_link-description = { -link-description }

## Kick
mod-kick-description = Kickt einen Benutzer vom Server
mod-kick-options-target-name = { -target }
mod-kick-options-target-description = Der Benutzer, der gekickt werden soll.
mod-kick-options-paragraph-name  = { -paragraph }
mod-kick-options-paragraph-description = { -paragraph-description }
mod-kick-options-del_days-name = { -del-days }
mod-kick-options-del_days-description = { -del-days-description }
mod-kick-options-message_link-name = { -link }
mod-kick-options-message_link-description = { -link-description }

## Ban
mod-ban-description = Bannt einen Benutzer vom Server
mod-ban-options-target-name = { -target }
mod-ban-options-target-description = Der Benutzer, der gebannt werden soll.
mod-ban-options-until-name = { -duration }
mod-ban-options-until-description = Für wie lange der Ban andauern soll.
mod-ban-options-del_days-name = { -del-days }
mod-ban-options-del_days-description = { -del-days-description }
mod-ban-options-paragraph-name = { -paragraph }
mod-ban-options-paragraph-description = { -paragraph-description }
mod-ban-options-message_link-name = { -link }
mod-ban-options-message_link-description = { -link-description }

## BulkDelete
mod-purge-messages-description = Löscht eine bestimmte Anzahl an Nachrichten gleichzeitig
mod-purge-messages-options-amount-name = anzahl
mod-purge-messages-options-amount-description = Anzahl der Nachrichten die gelöscht werden sollen

## Delete
mod-delete-description = Löscht eine Moderationshandlung
mod-delete-options-moderation_act-name = id
mod-delete-options-moderation_act-description = Die ID der Moderationshandlung, die gelöscht werden soll.

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

## Revert
mod-revert-description = Hebt eine Moderationshandlung auf
mod-revert-options-moderation_act-name = id
mod-revert-options-moderation_act-description = Die ID der Moderationshandlung, die aufgehoben werden soll.
mod-revert-options-reason-name = grund
mod-revert-options-reason-description = Der Grund für die Aufhebung.


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
duration-field = Dauer
revoke-at-field = Aufhebung
rule = Regel
reference-message = Referenznachricht
## ModeartionAct
warn-description = Dir wurde eine Verwarnung auf dem **NPLAY** Discord Server ausgesprochen!
timeout-description = Dir wurde ein Timeout auf dem **NPLAY** Discord Server auferlegt!
kick-description = Du wurdest vom **NPLAY** Discord Server gekickt!
temp-ban-description = Du wurdest temporär vom **NPLAY** Discord Server gebannt!
ban-description = Du wurdest vom **NPLAY** Discord Server gebannt!

# ModerationActType
warn = Verwarnung
kick = Kick
timeout = Timeout
temp-ban = Temporärer Bann
ban = Bann