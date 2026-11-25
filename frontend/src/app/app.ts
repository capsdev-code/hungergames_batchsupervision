import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { EditionCard } from './edition-card/edition-card';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('frontend');
  edition = signal<Edition>({
  id: 74,
  label: '74e Jeux de la Faim',
  status: 'COMPLETED',
  startTime: '2026-11-25T10:00:00Z',
  endTime: '2026-11-25T11:30:00Z',
  tributeCount: 24,
  aliveCount: 1,
  eliminatedCount: 23,
  errorCount: 0
});
}
