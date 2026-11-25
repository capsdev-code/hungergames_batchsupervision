import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditionCard } from './edition-card';
import { Edition } from '../edition.model';

const SAMPLE: Edition = {
  id: 74,
  label: '74e Jeux de la Faim',
  status: 'COMPLETED',
  startTime: '2026-10-05T08:00:00Z',
  endTime: '2026-10-05T08:50:00Z',
  tributeCount: 24,
  aliveCount: 1,
  eliminatedCount: 23,
  errorCount: 0,
};

describe('EditionCard', () => {
  let fixture: ComponentFixture<EditionCard>;

  // Crée la carte avec une édition, en remplaçant les champs voulus.
  async function render(overrides: Partial<Edition> = {}): Promise<HTMLElement> {
    fixture = TestBed.createComponent(EditionCard);
    // Un input() ne se remplit pas par affectation : on passe par setInput.
    fixture.componentRef.setInput('edition', { ...SAMPLE, ...overrides });
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [EditionCard] }).compileComponents();
  });

  it('affiche le label', async () => {
    const el = await render();
    expect(el.querySelector('h2')?.textContent).toContain('74e Jeux de la Faim');
  });

  it('colore le statut selon sa valeur', async () => {
    const el = await render({ status: 'FAILED' });
    expect(el.querySelector('.status')?.classList).toContain('status-failed');
  });

  it('affiche "En cours" quand endTime est null', async () => {
    const el = await render({ status: 'STARTED', endTime: null });
    expect(el.textContent).toContain('Fin : En cours');
  });

  it("affiche les erreurs seulement s'il y en a", async () => {
    expect((await render()).querySelector('.errors')).toBeNull();
    expect((await render({ errorCount: 2 })).querySelector('.errors')?.textContent).toContain('2');
  });
});
