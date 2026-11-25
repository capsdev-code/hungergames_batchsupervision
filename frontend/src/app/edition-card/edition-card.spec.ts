import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditionCard } from './edition-card';

describe('EditionCard', () => {
  let component: EditionCard;
  let fixture: ComponentFixture<EditionCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditionCard],
    }).compileComponents();

    fixture = TestBed.createComponent(EditionCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
