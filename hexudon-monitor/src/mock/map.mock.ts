import type { CellResponse, SpotResponse } from '../models/match';

const WIDTH = 20;
const HEIGHT = 15;




function pickTerrain(x: number, y: number): CellResponse['terrainType'] {
  // Deterministic terrain based on position
  const seed = (x * 7 + y * 13) % 100;
  if (seed < 10) return 'POND';
  if (seed < 25) return 'MOUNTAIN';
  if (seed < 45) return 'ROAD';
  return 'PLAIN';
}

export const MOCK_CELLS: CellResponse[] = [];
for (let y = 0; y < HEIGHT; y++) {
  for (let x = 0; x < WIDTH; x++) {
    MOCK_CELLS.push({
      coordinate: { x, y },
      terrainType: pickTerrain(x, y),
    });
  }
}

export const MOCK_SPOTS: SpotResponse[] = [
  { coordinate: { x: 3, y: 2 }, udonType: { typeName: 'TANUKI' }, amount: 5 },
  { coordinate: { x: 7, y: 4 }, udonType: { typeName: 'KITSUNE' }, amount: 3 },
  { coordinate: { x: 12, y: 6 }, udonType: { typeName: 'TEMPURA' }, amount: 4 },
  { coordinate: { x: 16, y: 8 }, udonType: { typeName: 'BEEF' }, amount: 5 },
  { coordinate: { x: 5, y: 10 }, udonType: { typeName: 'TANUKI' }, amount: 2 },
  { coordinate: { x: 9, y: 12 }, udonType: { typeName: 'KITSUNE' }, amount: 5 },
  { coordinate: { x: 15, y: 3 }, udonType: { typeName: 'TEMPURA' }, amount: 1 },
  { coordinate: { x: 2, y: 7 }, udonType: { typeName: 'BEEF' }, amount: 4 },
];
