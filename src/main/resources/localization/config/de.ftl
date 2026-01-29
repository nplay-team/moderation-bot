## Config Set
config-update =
    ### Einstellung { $key } gesetzt
    { $value }

## Set Spielersuche
config-set-spielersuche-ausschluss-rolle-description = Setzt die Rolle für die Spielersuche Auschluss Funktion.
config-set-spielersuche-ausschluss-rolle-options-role-name = rolle
config-set-spielersuche-ausschluss-rolle-options-role-description = Die Rolle, welche der User erhalten soll, wenn er von der Spielersuche ausgeschlossen wird.

## Set Serverlog
config-set-serverlog-kanal-description = Setzt den Textkanal für den Serverlog.
config-set-serverlog-kanal-options-channel-name = { -channel }
config-set-serverlog-kanal-options-channel-description = Der Text-Kanal in denen die Bot-Logs gesendet werden sollen.

## Config List
no-value-set = Nicht gesetzt
config-list-description = Listet alle Konfigurationen auf
config-list =
    ## Einstellungen
    Spielersuche-Ausschluss-Rolle
    <@&{ $role }> ({ $role })

    Serverlog-Kanal
    <#{ $serverlog }> ({ $serverlog })
