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
