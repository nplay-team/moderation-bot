# Error Messages
## Type Adapting Failed
adapting-failed-title =
    ## Falsche Eingabe
    ### Command
    { $command }
adapting-failed-details =
    ### Details
    { "**Erwarteter Typ**" }
    `{ $expected }`
    { "**Tatsächlicher Typ**" }
    `{ $actual }`
    { "**Eingabe**" }
    `{ $raw }`
adapting-failed-message =
    ### Fehlermeldung
    { $message }
## Insufficient Permissions
insufficient-permissions =
    ## Fehlende Berechtigungen
    `{ $interaction }` benötigt bestimmte Berechtigungen, um ausgeführt zu werden
    ### Benötigte Berechtigungen
    `{ $permissions }`
## Constraint Failed
constraint-failed =
    ### Falsche Eingabe
    { $ message }
## Interaction Execution Failed
execution-failed-title =
    ## Command Ausführung fehlgeschlagen
    Bei der Ausführung ist ein plötzlicher Fehler aufgetreten. Bitte leite die Fehlermeldung an <@395908417879015424> weiter.
execution-failed-message =
    ### Fehlermeldung
    Nutzer: { $ user }
    Typ: `{ $interaction }`
    Timestamp: { $timestamp }

## Unknown Interaction
unknown-interaction =
    ### Unbekannte Interaktion
    Diese Interaktion ist abgelaufen und steht nicht mehr zur Verfügung

# Constraints
member-missing-permission = Dem Nutzer fehlt mindestens eine erforderliche Berechtigung.
member-has-unallowed-permission = Der Nutzer hat mindestens eine Berechtigung, die niocht erlaubt ist.
# Type Adapter
member-required-got-user = Ein Server Mitglied ist erforderlich, aber es wurde nur ein Discord Nutzer angegeben.
# Command Building
no-description = keine beschreibung