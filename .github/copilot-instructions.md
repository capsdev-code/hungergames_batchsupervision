# Instructions Copilot : frontend Angular

- Angular récent uniquement : composants standalone, signals (`signal`, `computed`, `effect`), `input()` / `output()`, `inject()`.
- Syntaxe de template : `@if`, `@for` (avec `track`), `@switch`. Interdit : `*ngIf`, `*ngFor`.
- Interdit : NgModules, décorateurs `@Input` / `@Output`, injection par constructeur, `subscribe()` manuel sans nécessité (préférer `async` pipe, signals, `toSignal`).
- HTTP : `HttpClient` via `inject()`, interceptors et guards fonctionnels.
- TypeScript strict, jamais de `any`. Types alignés sur le contrat OpenAPI du backend.
- Explique avant de coder. Propose une approche, n'écris pas une fonctionnalité entière. Donne des indices avant la solution.
- Référence de vérité : https://angular.dev
