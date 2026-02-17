## Revert Command
mod-revert-description = Hebt eine Moderationshandlung auf
mod-revert-options-moderation_act-name = id
mod-revert-options-moderation_act-description = Die ID der Moderationshandlung, die aufgehoben werden soll.
mod-revert-options-reason-name = grund
mod-revert-options-reason-description = Der Grund für die Aufhebung.

revert-info =
    ### { $type } aufgehoben
    Dein { $type } auf dem **NPLAY** Discord Server wurde aufgehoben!
    .body =
    { "**ID**" }
    \#{ $id }
    { "**Datum**" }
    { $date }
    { "**Begründung**" }
    { $reason }
    .reverter =
    { "**Revidierender Moderator**" }
    { $revertedBy }
revert-successful =
    ### Erfolgreich
    Die Moderationshandlung mit der ID **#{ $id }** wurde erfolgreich rückgängig gemacht!
revert-failed =
    ### Fehler
    Die Moderationshandlung mit der ID **#{ $id }** wurde bereits rückgängig gemacht!

## Delete Command
mod-delete-description = Löscht eine Moderationshandlung
mod-delete-options-moderation_act-name = id
mod-delete-options-moderation_act-description = Die ID der Moderationshandlung, die gelöscht werden soll.

delete-successful =
    ### Erfolgreich
    Die Moderationshandlung mit der ID **#{ $id }** wurde erfolgreich gelöscht!
delete-reason = Moderationshandlung wurde gelöscht
