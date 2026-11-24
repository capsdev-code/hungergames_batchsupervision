# Supervision des Jeux de la Faim

Projet fil rouge d'apprentissage Angular + Spring Boot. Une console de supervision d'un batch de gestion de personnel, habillée Hunger Games.

- Contexte client : [docs/client-brief.md](docs/client-brief.md)
- Modèle de données : [docs/domain-model.md](docs/domain-model.md)
- Roadmap sur 6 semaines : [docs/roadmap.md](docs/roadmap.md)
- Déroulé d'une session : [docs/workflow.md](docs/workflow.md)

## Prérequis

- Java 21, Maven 3.9+
- Node 22+, Angular CLI (`npm i -g @angular/cli`)

## Lancer le backend

```bash
cd backend
mvn spring-boot:run
```

- API : http://localhost:8080/api/editions
- Swagger UI : http://localhost:8080/swagger-ui.html
- Contrat OpenAPI : http://localhost:8080/v3/api-docs

## Lancer le frontend

```bash
cd frontend
npm install
ng serve
```

Le dossier `frontend/` est créé à la session 1 (voir [docs/tickets/S1.md](docs/tickets/S1.md)).
