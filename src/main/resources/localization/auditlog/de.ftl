auditlog-detail-description = Zeigt einen Auditlog Eintrag an
auditlog-detail-options-id-description = Die ID des Auditlog Eintrags
auditlog-query-description = Durchsucht den Auditlog nach Einträgen

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

## detail
pagination = Auditlog Query
    .back = Zurück
    .forth = Weiter