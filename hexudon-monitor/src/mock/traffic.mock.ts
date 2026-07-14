import type { TrafficResponse } from '../models/match';

export const MOCK_TRAFFIC: TrafficResponse[] = [
  { coordinate: { x: 0, y: 0 }, trafficLevel: 'NORMAL' },
  { coordinate: { x: 2, y: 2 }, trafficLevel: 'BUSY' },
  { coordinate: { x: 4, y: 4 }, trafficLevel: 'CONGESTED' },
  { coordinate: { x: 6, y: 6 }, trafficLevel: 'BUSY' },
  { coordinate: { x: 8, y: 8 }, trafficLevel: 'NORMAL' },
  { coordinate: { x: 10, y: 10 }, trafficLevel: 'CONGESTED' },
  { coordinate: { x: 12, y: 2 }, trafficLevel: 'NORMAL' },
  { coordinate: { x: 14, y: 4 }, trafficLevel: 'BUSY' },
  { coordinate: { x: 16, y: 6 }, trafficLevel: 'CONGESTED' },
  { coordinate: { x: 18, y: 8 }, trafficLevel: 'NORMAL' },
  { coordinate: { x: 3, y: 10 }, trafficLevel: 'BUSY' },
  { coordinate: { x: 7, y: 12 }, trafficLevel: 'NORMAL' },
];

export const MOCK_TRAFFIC_HISTORY = Array.from({ length: 12 }, (_, i) => ({
  turn: i + 1,
  NORMAL: Math.floor(Math.random() * 8) + 5,
  BUSY: Math.floor(Math.random() * 5) + 2,
  CONGESTED: Math.floor(Math.random() * 3),
}));
