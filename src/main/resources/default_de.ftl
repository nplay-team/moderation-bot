# Terms
-channel = kanal
-duration = dauer
-target = user
-paragraph = paragraph
-paragraph-description = Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?

# Config
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
-link = Nachrichtenlink
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
mod-timeout-options-until-name = { -dauer }
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
mod-ban-options-until-name = { -dauer }
mod-ban-options-until-description = Für wie lange der Ban andauern soll.
mod-ban-options-del_days-name = { -del-days }
mod-ban-options-del_days-description = { -del-days-description }
mod-ban-options-paragraph-name = { -paragraph }
mod-ban-options-paragraph-description = { -paragraph-description }
mod-ban-options-message_link-name = { -link }
mod-ban-options-message_link-description = { -link-description }

## Revert
revert-act = Die ID der Moderationshandlung, die aufgehoben werden soll.
revert-reason = Der Grund für die Aufhebung.

mod-revert-name
mod-revert-description
mod-revert-options-moderation_act-name
mod-revert-options-moderation_act-description
mod-revert-options-reason-name
mod-revert-options-reason-description

## Detail
detail-act = Die ID der Moderationshandlung.

mod-detail-name
mod-detail-description
mod-detail-options-moderation_act-name
mod-detail-options-moderation_act-description

## Delete
delete-act = Die ID der Moderationshandlung, die gelöscht werden soll.

mod-delete-name
mod-delete-description
mod-delete-options-moderation_act-name
mod-delete-options-moderation_act-description

## Notes
notes-name
notes-description
notes-list-name
notes-list-description
notes-list-options-target-name
notes-list-options-target-description
notes-delete-name
notes-delete-description
notes-delete-options-note_id-name
notes-delete-options-note_id-description
notes-create-name
notes-create-description
notes-create-options-target-name
notes-create-options-target-description

## Purge
mod-purge-name
mod-purge-description
mod-purge-messages-name
mod-purge-messages-description
mod-purge-messages-options-amount-name
mod-purge-messages-options-amount-description

## Log
mod-log-name
mod-log-description
mod-log-options-user-name
mod-log-options-user-description
mod-log-options-page-name
mod-log-options-page-description
mod-log-options-count-name
mod-log-options-count-description

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
