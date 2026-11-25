/** Les trois statuts possibles d'une édition (type "union" : seules ces 3 valeurs sont acceptées). */
export type EditionStatus = 'STARTED' | 'COMPLETED' | 'FAILED';

/**
 * Une édition telle que la renvoie GET /api/editions.
 * `export` : sans ce mot, l'interface resterait privée à ce fichier.
 */
export interface Edition {
  id: number;
  label: string;
  status: EditionStatus;
  /** Date ISO 8601 en texte, ex. '2026-10-05T08:00:00Z'. Le pipe `date` la formate à l'affichage. */
  startTime: string;
  /** `null` tant que l'édition est en cours. */
  endTime: string | null;
  tributeCount: number;
  aliveCount: number;
  eliminatedCount: number;
  errorCount: number;
}
