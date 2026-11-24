# Projet d'apprentissage Angular : supervision des Jeux de la Faim

Monorepo : `backend/` (Spring Boot 3, Java 21, généré par Claude) et `frontend/` (Angular, écrit à la main par l'apprenant).

## Règles de collaboration

- L'apprenant écrit le code du `frontend/`. Par défaut, ne PAS l'écrire à sa place : donner indices, revue de code, explications.
  N'écrire du frontend que si l'apprenant le demande explicitement pour une partie précise.
- Le backend est géré par Claude : ajouter les endpoints au rythme des tickets (`docs/roadmap.md`), toujours avant la session qui en dépend.
- Revue de code au format PR : `[bloquant]`, `[suggestion]`, `[nit]`, puis correction commentée.
- Réponses en français, ton direct, sans remplissage.
- Un ticket par session dans `docs/tickets/`. Branche `feature/sN-...`.

## Références

- Contexte client fictif : `docs/client-brief.md`
- Modèle de données : `docs/domain-model.md`
- Roadmap : `docs/roadmap.md`
- Déroulé d'une session : `docs/workflow.md`

## Commandes

- Simuler un batch (démo) : `curl -X POST localhost:8080/api/editions -H 'Content-Type: application/json' -d '{"scenario":"TECHNICAL_FAILURE"}'`
- Backend : `cd backend && mvn spring-boot:run` (port 8080), tests : `mvn test`
- OpenAPI : http://localhost:8080/v3/api-docs, Swagger UI : http://localhost:8080/swagger-ui.html
- Frontend : `cd frontend && ng serve` (port 4200)
