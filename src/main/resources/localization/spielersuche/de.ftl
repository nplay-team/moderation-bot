## Ausschluss
spielersuche-ausschluss-description = Schließt einen Benutzer von der Spielersuche aus und verwarnt ihn.
spielersuche-ausschluss-options-target-name = benutzer
spielersuche-ausschluss-options-target-description = Der Benutzer, der ausgeschlossen werden soll.
spielersuche-ausschluss-options-paragraph-name = paragraph
spielersuche-ausschluss-options-paragraph-description = Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?
spielersuche-ausschluss-reason = Du hast erneut gegen die Spielersucheregeln verstoßen **und wurdest von der Spielersuche ausgeschlossen!**

## Freigeben
spielersuche-freigeben-description = Hebt den Ausschluss eines Benutzers von der Spielersuche auf.
spielersuche-freigeben-options-target-name = benutzer
spielersuche-freigeben-options-target-description = Der Benutzer, dessen Ausschluss aufgehoben werden soll.

## Success
block =
    ### Erfolgreich
    Der Benutzer { $target } wurde erfolgreich von der Spielersuche blockiert!
unblock =
    ### Erfolgreich
    Der Benutzer { $target } wurde erfolgreich für die Spielersuche wieder freigegeben!
unblock-target =
    ### Spielersuche-Freigabe
    Du wurdest soeben für die Spielersuche wieder freigegeben! Bitte achte in Zukunft verstärkt auf die kanalspezifischen Regeln!
    .body =
    { "**Moderator**" }
    { $issuer }
    { "**Datum**" }
    { $createdAt }

## Error
role-error =
    ### Fehler
    Die Spielersuche-Ausschluss-Rolle existiert nicht mehr oder wurde noch nicht konfiguriert!
already-blocked =
    ### Fehler
    Der Benutzer { $target } ist bereits blockiert!
not-blocked =
    ### Fehler
    Der Benutzer { $target } ist nicht blockiert!
