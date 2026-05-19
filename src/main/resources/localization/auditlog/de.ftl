auditlog-detail-description = Zeigt einen Auditlog Eintrag an
auditlog-detail-options-id-description = Die ID des Auditlog Eintrags
auditlog-query-description = Durchsucht den Auditlog nach Einträgen

## adapter
invalid-auditlog-type = Unbekanntes Auditlog Event

## detail
not-found =
    ### Fehler
    Ein Auditlog Eintrag mit der ID **{ $id }** existiert nicht
entry =
    ### { $type } | { $createdAt }
    { "**Issuer:**" }
    { $issuer }
    { "**Target:**" }
    { $target }
    { "**Payload:**" }
    ```
    { $payload }
    ```

## query
pagination = ## Auditlog Einträge
    .first = Erste Seite
    .back = Zurück
    .forth = Weiter
    .last = Letzte Seite
    .pages = -# Seite { $current }/{ $max }

no-entries =
    ### Fehler
    Keine Auditlog Einträge vorhanden!

type-entry =
    - { "**" }#{ $id } | { $type } | { $createdAt }{ "**" }
    Moderator: { $issuer }
    Ziel: { $target }
