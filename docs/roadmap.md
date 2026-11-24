# Roadmap : 6 semaines, 12 sessions de 2 à 3 h

Le backend est fourni et ajouté par incréments avant chaque session qui en dépend. Le travail de l'apprenant est 100 % Angular.

| Sem. | Session | Ticket frontend | Backend préparé en amont |
|---|---|---|---|
| 1 | S1 | Setup Angular, composant `EditionCard`, types TypeScript à la main | Lecture seule (fait) |
| 1 | S2 | Liste d'éditions en dur, `@for` / `@if`, signals, `input()` / `output()` | |
| 2 | S3 | Service + `HttpClient` branché sur `/api/editions`, états chargement/erreur | |
| 2 | S4 | Routing : liste, détail d'une édition (phases, erreurs, 12 districts), fil d'événements d'un tribut | |
| 3 | S5 | Liste des tributs, filtres état / district / niveau (`computed`) | |
| 3 | S6 | Relance d'une édition échouée : Reactive Forms, validation, erreurs renvoyées par l'API | `POST /api/editions/{id}/relaunch` |
| 4 | S7 | Auth : login, JWT, interceptor, guard | Spring Security + JWT |
| 4 | S8 | Rôles lecteur / opérateur, masquage des actions | Rôles sur les endpoints |
| 5 | S9 | Pagination et tri côté serveur, types TS générés depuis OpenAPI | Endpoints paginés |
| 5 | S10 | Rafraîchissement auto de l'édition en cours (RxJS : `timer`, `switchMap`, `catchError`) | Édition 75 qui progresse |
| 6 | S11 | Tests (composant, service, 1 test Playwright), lazy loading | |
| 6 | S12 | Build, déploiement, README, démo à l'équipe | |
