# Demande client (fictive)

**Client : le Capitole.** Les Gamemakers lancent chaque édition des Jeux sans visibilité sur son déroulement. Ils demandent une console de supervision.

## Besoins

1. Voir la liste des éditions et leur statut (en cours, terminée, échouée).
2. Ouvrir une édition : phases, tributs vivants et éliminés, durée, erreurs.
3. Voir pour chaque district le nombre de tributs affectés (12 districts, 2 tributs chacun).
4. Suivre un tribut : fil d'événements (nourriture, parachute du Capitole, élimination).
5. Filtrer les tributs par état, district et niveau d'événement.
6. Relancer une édition échouée, après correction des données (opérateurs uniquement).
7. Accès réservé aux Gamemakers authentifiés, deux rôles : lecteur et opérateur.
8. Rafraîchissement automatique de l'édition en cours.

## Hors besoin client : outil de démo

Le lancement d'une édition depuis la console n'est pas demandé par le Capitole (le vrai batch est lancé ailleurs). Il est ajouté au projet pour tester l'IHM : voir une édition progresser, échouer fonctionnellement ou techniquement, puis la relancer. À réserver à l'environnement de développement.

## Règle fonctionnelle

Chaque édition exige 12 districts x 2 tributs = 24 tributs. La Moisson valide cette règle. Un district avec moins de 2 tributs provoque une erreur **fonctionnelle** et l'édition passe en échec dès la Moisson.

## Correspondance avec un vrai batch RH

| Hunger Games | Batch RH |
|---|---|
| Le programme des Jeux | Le job de traitement du personnel |
| Une édition | Un lancement (`JobExecution`) |
| Phase (Moisson, Arène, Couronnement) | Step (lecture, calcul, export) |
| Un tribut | Un salarié traité (item) |
| District | Service ou site de rattachement |
| Règle 12 x 2 | Contrôle de complétude des services |
