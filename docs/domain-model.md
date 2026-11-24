# Modèle de données

## Édition (`Edition`)

| Champ | Type | Note |
|---|---|---|
| id | number | |
| label | string | ex. « 74e Jeux de la Faim » |
| status | `STARTED` \| `COMPLETED` \| `FAILED` | |
| startTime / endTime | ISO 8601 | `endTime` null si en cours |
| phases | `Phase[]` | |
| errors | `EditionError[]` | |

## Phase

`name` : `MOISSON` (validation + lecture), `ARENE` (traitement), `COURONNEMENT` (export).
`status` : `PENDING` \| `STARTED` \| `COMPLETED` \| `FAILED`. Les dates sont null tant que la phase n'a pas démarré.

## Erreur d'édition (`EditionError`)

- `category` : `FUNCTIONAL` (règle métier violée) ou `TECHNICAL` (panne).
- `phase` : phase concernée.
- `district` : numéro du district concerné, null pour une erreur technique.
- `message`.

## Tribut (`Tribute`)

`id`, `name`, `district` (1 à 12), `state` : `ALIVE` \| `ELIMINATED`.

## Événement de tribut (`TributeEvent`)

| type | niveau | sens |
|---|---|---|
| `FOOD` | `INFO` | a trouvé de la nourriture |
| `SPONSOR_GIFT` | `INFO` | aide du Capitole (parachute) |
| `KILL` | `WARN` | a éliminé un autre tribut |

Le niveau `ERROR` est réservé aux erreurs d'édition.

## Endpoints (lecture)

| Méthode | URL | Réponse |
|---|---|---|
| GET | `/api/editions` | `EditionSummary[]`, plus récentes d'abord |
| GET | `/api/editions/{id}` | `EditionDetail` (résumé, phases, erreurs, état des 12 districts) |
| GET | `/api/editions/{id}/tributes` | `Tribute[]` |
| GET | `/api/editions/{id}/tributes/{tributeId}/events` | `TributeEvent[]` |

## Endpoints d'écriture (outil de démo)

Le vrai batch est lancé hors de la console. Ces endpoints simulent un batch pour tester l'IHM (progression, échecs fonctionnels et techniques).

| Méthode | URL | Effet |
|---|---|---|
| POST | `/api/editions` | Lance une nouvelle édition. Réponse 201 + `EditionSummary` (statut `STARTED`) |
| POST | `/api/editions/{id}/relaunch` | Relance une édition `FAILED`. 409 si elle n'est pas en échec |

Corps optionnel (les deux endpoints) : `{"scenario": "NORMAL" | "MISSING_DISTRICT" | "TECHNICAL_FAILURE", "missingDistrict": 1..12}`.
Sans corps : scénario `NORMAL`. `missingDistrict` vaut 2 par défaut.

| Scénario | Résultat |
|---|---|
| `NORMAL` | Moisson, Arène, Couronnement, statut `COMPLETED`, 1 survivant |
| `MISSING_DISTRICT` | Échec **fonctionnel** à la Moisson : le district choisi n'a aucun tribut |
| `TECHNICAL_FAILURE` | Échec **technique** en Arène après 8 éliminations (timeout base de données) |

Règles : une seule édition simulée à la fois (409 sinon). L'édition progresse en direct : environ 1 seconde par élimination, une édition normale dure environ 30 secondes (réglable avec `app.batch.tick-ms`). Les éditions simulées sont perdues au redémarrage du backend.

## Jeux de données

| Édition | Cas |
|---|---|
| 74 | Terminée, 24 tributs, 1 vainqueur |
| 75 | En cours, phase Arène, 9 éliminés |
| 76 | Échec fonctionnel : district 2 sans tribut (22 tributs) |
| 77 | Échec technique : timeout base de données pendant l'Arène |
