auditlog-detail-description = Zeigt einen Auditlog Eintrag an
auditlog-detail-options-id-description = Die ID des Auditlog Eintrags

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