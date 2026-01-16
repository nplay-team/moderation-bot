########## Terms ##########
-duration = dauer
-target = user
-paragraph = paragraph
-paragraph-description = Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?
-link = nachrichtenlink
-link-description = Link zu einer Nachricht, die referenziert werden soll.
-del-days = del_days
-del-days-description = Für wie viele Tage in der Vergangenheit sollen Nachrichten dieses Users gelöscht werden?

########## Ban ##########
mod-ban-description = Bannt einen Benutzer vom Server
mod-ban-options-target-name = { -target }
mod-ban-options-target-description = Der Benutzer, der gebannt werden soll.
mod-ban-options-until-name = { -duration }
mod-ban-options-until-description = Für wie lange der Ban andauern soll.
mod-ban-options-del_days-name = { -del-days }
mod-ban-options-del_days-description = { -del-days-description }
mod-ban-options-paragraph-name = { -paragraph }
mod-ban-options-paragraph-description = { -paragraph-description }
mod-ban-options-message_link-name = { -link }
mod-ban-options-message_link-description = { -link-description }
already-banned =
    ### Fehler
    Der angegebene Nutzer ist bereits gebannt!

########## Kick ##########
mod-kick-description = Kickt einen Benutzer vom Server
mod-kick-options-target-name = { -target }
mod-kick-options-target-description = Der Benutzer, der gekickt werden soll.
mod-kick-options-paragraph-name  = { -paragraph }
mod-kick-options-paragraph-description = { -paragraph-description }
mod-kick-options-del_days-name = { -del-days }
mod-kick-options-del_days-description = { -del-days-description }
mod-kick-options-message_link-name = { -link }
mod-kick-options-message_link-description = { -link-description }

########## Timeout ##########
mod-timeout-description = Versetzt einen Benutzer in den Timeout
mod-timeout-options-target-name = { -target }
mod-timeout-options-target-description = Der Benutzer, den in den Timeout versetzt werden soll.
mod-timeout-options-until-name = { -duration }
mod-timeout-options-until-description = Für wie lange der Timeout andauern soll (max. 28 Tage).
mod-timeout-options-paragraph-name = { -paragraph }
mod-timeout-options-paragraph-description = { -paragraph-description }
mod-timeout-options-message_link-name = { -link }
mod-timeout-options-message_link-description = { -link-description }
already-timeout =
    ### Fehler
    Der angegebene Nutzer hat bereits ein aktives Timeout!

########## Warn ##########
mod-warn-description = Verwarnt einen Benutzer
mod-warn-options-target-name = { -target }
mod-warn-options-target-description = Der Benutzer, der verwarnt werden soll.
mod-warn-options-paragraph-name = { -paragraph }
mod-warn-options-paragraph-description = { -paragraph-description }
mod-warn-options-message_link-name = { -link }
mod-warn-options-message_link-description = { -link-description }

########## LockMiddleware ##########
target-locked =
    ### Fehler
    Der angegebene Nutzer { $target } wird bereits durch { $moderator } moderiert!

########## Modal ##########
reason-title = Begründung angeben ({ $type })
reason-label = Begründung der Moderationshandlung

########## Moderator Reply ##########
executed =
    ### { $type } erfolgreich ausgeführt
    { "**ID**" }
    \#{ $id }
    { "**Betroffener Nutzer**" }
    { $target }
    { "**Begründung**" }
    { $reason }
    .until =
    { "**Aktiv bis**" }
    { $until }
    .paragraph =
    { "**Paragraph**" }
    { $paragraph }
    .reference =
    { "**Referenznachricht**" }
    > { $message }

########## Act Target Info ##########
act-info =
    ### { $type }
    { $description ->
        *[warn] Dir wurde eine Verwarnung auf dem **NPLAY** Discord Server ausgesprochen!
        [timeout] Dir wurde ein Timeout auf dem **NPLAY** Discord Server auferlegt!
        [kick] Du wurdest vom **NPLAY** Discord Server gekickt!
        [temp-ban] Du wurdest temporär vom **NPLAY** Discord Server gebannt!
        [ban] Du wurdest vom **NPLAY** Discord Server gebannt!
    }
    .reason =
    { "**ID**" }
    \#{ $id }
    { "**Datum**" }
    { $date }
    { "**Begründung**" }
    { $reason }
    .until =
    { "**Aktiv bis**" }
    { $until }
    .paragraph =
    { "**Regel, gegen die du verstoßen hast**" }
    { $paragraph }
    .reference =
    { "**Referenznachricht**" }
    > { $message }
    .footer = -# Bei Fragen oder Problemen wende dich bitte an den Ticket-Support!