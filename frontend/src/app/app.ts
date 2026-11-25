import { Component, signal } from '@angular/core';
import { EditionCard } from './edition-card/edition-card';
import { Edition } from './edition.model';

@Component({
  selector: 'app-root',
  // Un composant standalone doit déclarer ceux qu'il utilise dans son template.
  imports: [EditionCard],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  // Données en dur (édition 74 de docs/domain-model.md). Elles viendront de l'API en S3.
  // Pour tester : changez `status`, ou mettez `endTime: null`, et regardez la carte.
  protected readonly edition = signal<Edition>({
    id: 74,
    label: '74e Jeux de la Faim',
    status: 'COMPLETED',
    startTime: '2026-10-05T08:00:00Z',
    endTime: '2026-10-05T08:50:00Z',
    tributeCount: 24,
    aliveCount: 1,
    eliminatedCount: 23,
    errorCount: 0,
  });
}
