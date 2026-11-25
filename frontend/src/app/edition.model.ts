export type EditionStatus = 'STARTED' | 'COMPLETED' | 'FAILED';
interface Edition {
    id: number;
    label : string;
    status: EditionStatus;
    startTime : Date;
    endTime : Date | null;
    tributeCount: number;
    aliveCount: number;
    eliminatedCount: number;
    errorCount: number
}