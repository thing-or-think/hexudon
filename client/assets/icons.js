/**
 * Premium SVG Assets & Icons for HEXUDON
 *
 * Each function returns an SVG string.
 */

export const Icons = {
  // Agent Icons
  PATROL: (color = "#60A5FA", size = 24) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
      <circle cx="12" cy="11" r="3"/>
      <path d="m15 14 3.5 3.5"/>
    </svg>
  `,

  REFUEL: (color = "#F59E0B", size = 24) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 22h18"/>
      <path d="M4 22V4a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v18"/>
      <path d="M14 13h6c1.1 0 2 .9 2 2v4c0 1.1-.9 2-2 2h-6"/>
      <path d="M9 9h.01"/>
      <path d="M17 5v4"/>
      <path d="M18 2h-2"/>
      <path d="M7 6h4v4H7z"/>
    </svg>
  `,

  // Terrain Symbols
  PLAIN: (color = "#10B981", size = 20) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2">
      <path d="m3 20 3-6 3 6M9 20l2-4 2 4M15 20l3-8 3 8" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `,

  MOUNTAIN: (color = "#9CA3AF", size = 20) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2">
      <path d="m2 20 9-15 4 7 3-4 4 12H2z" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `,

  POND: (color = "#3B82F6", size = 20) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2">
      <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `,

  ROAD: (color = "#FBBF24", size = 20) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2">
      <path d="M4 22V2M20 22V2M12 4v4M12 12v4" stroke-dasharray="4 4" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `,

  // Spot / Udon
  UDON: (color = "#EF4444", size = 24) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 12c0 5 4 9 9 9s9-4 9-9H3z"/>
      <path d="M12 2v10"/>
      <path d="M8 2c0 2 2 4 4 4s4-2 4-4"/>
      <path d="M6 5c1 1 2 2 4 2s3-1 4-2"/>
      <path d="M12 21a9 9 0 0 0 9-9H3a9 9 0 0 0 9 9Z"/>
    </svg>
  `,

  FUEL_STATION: (color = "#10B981", size = 24) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 22h18"/>
      <path d="M5 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18"/>
      <circle cx="11" cy="8" r="2"/>
      <path d="M8 14h6"/>
      <path d="M8 18h6"/>
    </svg>
  `,

  // Interface Buttons & Info
  FUEL: (color = "#EF4444", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 22h18"/>
      <path d="M4 22V4a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v18"/>
      <path d="M14 13h6c1.1 0 2 .9 2 2v4c0 1.1-.9 2-2 2h-6"/>
      <path d="M17 5v4"/>
    </svg>
  `,

  UNDO: (color = "#9CA3AF", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 7v6h6"/>
      <path d="M21 17a9 9 0 0 0-9-9 9 9 0 0 0-6 2.3L3 13"/>
    </svg>
  `,

  REDO: (color = "#9CA3AF", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M21 7v6h-6"/>
      <path d="M3 17a9 9 0 0 1 9-9 9 9 0 0 1 6 2.3l3 2.7"/>
    </svg>
  `,

  TRASH: (color = "#EF4444", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 6h18"/>
      <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
      <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
    </svg>
  `,

  ADD: (color = "#10B981", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <line x1="12" y1="5" x2="12" y2="19"/>
      <line x1="5" y1="12" x2="19" y2="12"/>
    </svg>
  `,

  PLAY: (color = "#10B981", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <polygon points="5 3 19 12 5 21 5 3"/>
    </svg>
  `,

  REFRESH: (color = "#60A5FA", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
    </svg>
  `,

  SETTINGS: (color = "#9CA3AF", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="3"/>
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
    </svg>
  `,

  LOGS: (color = "#9CA3AF", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M12 20h9"/>
      <path d="M3 20v-8a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v8"/>
      <path d="M3 12V6a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v6"/>
      <path d="M14 12V8a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v4"/>
    </svg>
  `,
  
  // Traffic Indicators
  SMOOTH: (color = "#10B981", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <polyline points="20 6 9 17 4 12"/>
    </svg>
  `,

  CONGESTED: (color = "#F59E0B", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <line x1="18" y1="10" x2="6" y2="10"/>
      <line x1="18" y1="14" x2="6" y2="14"/>
    </svg>
  `,

  JAM: (color = "#EF4444", size = 16) => `
    <svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <line x1="18" y1="6" x2="6" y2="18"/>
      <line x1="6" y1="6" x2="18" y2="18"/>
    </svg>
  `
};
