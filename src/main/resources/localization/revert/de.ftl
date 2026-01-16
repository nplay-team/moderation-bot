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