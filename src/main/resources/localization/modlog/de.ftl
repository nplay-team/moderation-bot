## Modlog
mod-log-description = Zeigt den Modlog eines Mitglieds an.
mod-log-options-target-name = { -target }
mod-log-options-target-description = Der User, dessen Modlog abgerufen werden soll.
mod-log-options-page-name = seite
mod-log-options-page-description = Die Seite, die angezeigt werden soll
mod-log-options-count-name = seiten
mod-log-options-count-description = Wie viele Moderationshandlungen pro Seite angezeigt werden sollen (max. 25)
modlog =
    ## NPLAY-Moderation - Datenauskunft
    .header =
        ### { $target }
        { "**Nutzer ID:**" } { RAW($id) }
        { "**Erstellt am:**" } { $createdAt }
        { "**Beigetreten am:**" } { $joinedAt ->
            [empty] Kein Servermitglied
            *[other] { $joinedAt }
        }
    .notes = ## Notizen
    .moderations = ## Moderationshandlungen
    .empty = Keine Eintragungen
    .pages = -# Seite ({ $page }/{ $maxPage })

navigation = Seitenauswahl
    .back = :arrow_left: Zurück
    .next = :arrow_right: Weiter

entry =
    { "**" }#{ $id } | { $type } | { $createdAt }{ "**" }
    { $reason }
    -# Moderator: { $issuer }
    .reverted =
        ~~{ "**" }#{ $id } | { $type } | { $createdAt }{ "**" }~~
        ~~{ $reason }~~
        -# ~~Moderator: { $issuer }~~
        -# Aufgehoben von: { $reverter }