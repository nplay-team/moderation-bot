## Create
notes-create-description = Erstellt eine Notiz über einen Benutzer
notes-create-options-target-name = user
notes-create-options-target-description = Zu welchem Benutzer soll eine Notiz erstellt werden?
limit-reached =
    ### Fehler
    Der Benutzer hat bereits die maximale Anzahl von 25 Notizen erreicht!
modal = Notiz erstellen
    .content = Inhalt der Notiz
created =
    ### Notiz erstellt
    Die Notiz mit der ID **${ $id }** wurde erfolgreich erstellt!
    .content =
        { "**Inhalt**" }
        { $content }
    .target =
        { "**Betroffener**" }
        { $target }
    .creator =
        { "**Ersteller**" }
        { $createdBy }
        { "**Erstellt am**" }
        { $createdAt }
## List
notes-list-description = Listet alle Notizen eines Benutzers auf
notes-list-options-target-name = nutzer
notes-list-options-target-description = Welcher Benutzer soll aufgelistet werden?
list = ### Notizen von { $target }
    .empty = Keine Notizen vorhanden
    .entry =
        { "**" }Notiz ${$id} | {$date}{ "**" }
        { $content }
        -# Moderator: { $createdBy }

## Delete
notes-delete-description = Löscht eine Notiz
notes-delete-options-note_id-name = id
notes-delete-options-note_id-description = Welche Notiz soll gelöscht werden?
not-found =
    ### Fehler
    Die Notiz mit der ID **${ $id }** existiert nicht!
deleted =
    ### Notiz gelöscht
    Die Notiz mit der ID **${ $id }** wurde erfolgreich gelöscht!
