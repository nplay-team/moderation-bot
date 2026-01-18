########## Revert Target Info ##########
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
delete-successful =
    ### Erfolgreich
    Die Moderationshandlung mit der ID **#{ $id }** wurde erfolgreich gelöscht!