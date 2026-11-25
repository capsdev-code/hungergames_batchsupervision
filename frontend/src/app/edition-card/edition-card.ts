import { Component, computed, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Edition, EditionStatus } from '../edition.model';

/** Texte affiché pour chaque statut. Record<K, V> = un objet qui a exactement une entrée par clé K. */
const STATUS_LABELS: Record<EditionStatus, string> = {
  STARTED: 'En cours',
  COMPLETED: 'Terminée',
  FAILED: 'Échouée',
};

@Component({
  selector: 'app-edition-card',
  // DatePipe fournit le filtre `| date` utilisé dans le template.
  imports: [DatePipe],
  templateUrl: './edition-card.html',
  styleUrl: './edition-card.css',
})
export class EditionCard {
  // input.required : le parent DOIT fournir cette valeur. C'est un signal : on le lit avec edition().
  readonly edition = input.required<Edition>();

  // computed : valeur recalculée automatiquement quand le signal `edition` change.
  protected readonly statusLabel = computed(() => STATUS_LABELS[this.edition().status]);
  protected readonly statusClass = computed(() => `status-${this.edition().status.toLowerCase()}`);
}
