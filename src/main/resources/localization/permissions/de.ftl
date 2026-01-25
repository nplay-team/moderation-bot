## Manage Member
permissions-member-description = Verwaltet die Berechtigungen eines Benutzers.
permissions-member-options-member-name = { -target }
permissions-member-options-member-description = Der Benutzer, dessen Berechtigungen bearbeitet werden sollen.
permissions-select = Wähle eine oder mehrere Berechtigungen aus
member =
    ## Berechtigungen von { $member }
    { "-# kombiniert aus User & Rollen Berechtigungen" }
    .edit =
        ### Berechtigungen bearbeiten
        { "-# Nutze `/permissions role` für die Rollen Berechtigungen" }
    .list =
        ### Aktuelle Berechtigungen
        { $permissions }
    .modify = :tools: Bearbeiten
    .save = :floppy_disk: Speichern
    .remove = :wastebasket: Alle löschen
confirm =
    ## Bestätigen
    Möchtest du wirklich alle Berechtigungen löschen?
    .confirm = :wastebasket: Löschen
    .cancel = :x: Abbrechen

## Manage Role
permissions-manage-role-description = Verwaltet die Berechtigungen einer Rolle.
permissions-manage-role-options-role-name = rolle
permissions-manage-role-options-role-description = Die Rolle, dessen Berechtigungen bearbeitet werden sollen.

## Bot Permissions
admin = Administrator
mod-read = Einsehen von moderativen Handlung
mod-create = Moderieren von Benutzern
mod-revert = Rückgängig machen von moderativen Handlungen
mod-delete = Löschen von moderativen Handlungen
perm-read = Einsehen von Berechtigungen
perm-manage = Vergeben von Berechtigungen
perm-none = Keine Berechtigungen
