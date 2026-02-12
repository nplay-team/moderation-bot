## Moderation
moderation =
    ### { $type } | #{ $id }
    { "**Betroffener Nutzer**" }
    { $target }
    { $revert ->
        [true]
            { "**Ursprünglicher Moderator**" }
            { $issuer }
            { "**Revidierender Moderator**" }
            { $revertingModerator }
        *[false]
            { "**Moderator**" }
            { $issuer }
    }
    { "**Begründung**" }
    { $reason }
    { "**Datum**" }
    { $createdAt }
    .until =
    { "**Aktiv bis**" }
    { $until }
delete-reason = Moderationshandlung wurde gelöscht

## Notes
note =
    ### { $type } | ${ $id }
    { "**Betroffener Nutzer**" }
    { $target }
    { "**Moderator**" }
    { $issuer }
    { "**Notiz**" }
    { $note }
    { "**Datum**" }
    { $createdAt }

## Updates (Config, Permissions, Slowmode)
config =
    ### { $type }
    { "**Config Wert**" }
    { $config }
    { "**Moderator**" }
    { $issuer }
    { "**Alter Wert**" }
    { $oldValue }
    { "**Neuer Wert**" }
    { $newValue }
    { "**Datum**" }
    { $createdAt }
permissions =
    ### { $type }
    { "**Ziel**" }
    { $target }
    { "**Moderator**" }
    { $issuer }
    { "**Alte Berechtigungen**" }
    { $oldValue }
    { "**Neue Berechtigungen**" }
    { $newValue }
    { "**Datum**" }
    { $createdAt }
slowmode =
    ### { $type }
    { "**Textkanal**" }
    { $target }
    { "**Moderator**" }
    { $issuer }
    { "**Slowmode**" }
    { $duration }
    { "**Datum**" }
    { $createdAt }