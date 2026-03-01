# Code-Based Visualization System

## Overview

This visualization approach renders the game world using **code-generated graphics** instead of pre-rendered sprite images. All visual elements are created using CSS, HTML, and TypeScript based on the XML prompt specifications in `docs/prompts/`.

## Architecture

### Data Flow

```
XML Prompts (docs/prompts/) → TypeScript Models → React Components → CSS Rendering
```

### Key Components

1. **SceneModel** (`types/SceneModel.ts`)
   - TypeScript interfaces based on `RoomPage.xml`, `SCREENS_SPECIFICATION.xml`
   - Defines structure of locations, objects, NPCs, atmosphere
   - No image dependencies

2. **Scene Data** (`data/roomSceneModel.ts`)
   - Static configuration parsed from `RoomPage.xml`
   - Object positions, actions, requirements
   - NPC behavior patterns

3. **Rendering Components**
   - `RoomScreen.tsx` - Main scene orchestrator
   - `SceneObject.tsx` - Interactive objects (bed, computer, phone, etc.)
   - `Character.tsx` - Tatyana character (based on `character-visual-specs.xml`)
   - `NPC.tsx` - Sam the dog, Garfield the cat

### Styling System

All CSS follows `DESIGN_SYSTEM.xml` specifications:

- **Colors**: From visual-specs palette (burgundy, rose pink, mint green, etc.)
- **Animations**: CSS keyframes for idle, walk, emotion states
- **Layout**: Isometric perspective using CSS transforms
- **Responsive**: Mobile-first breakpoints per DESIGN_SYSTEM

## Mapping XML Prompts to Code

### RoomPage.xml → SceneModel

```typescript
// RoomPage.xml: <object id="bed" type="furniture" category="rest">
const bedObject: InteractiveObject = {
  id: 'bed',
  type: 'furniture',
  category: 'rest',
  position: { x: 200, y: 150, zIndex: 3 },
  actions: [
    {
      id: 'sleep',
      effects: { energy: { restore: 100 } }
    }
  ]
}
```

### character-visual-specs.xml → CSS

```css
/* From: <hair color="8B1A1A" description="burgundy"> */
.hair {
  background: #8B1A1A;
}

/* From: <skin-tone>F5D5B8</skin-tone> */
.face {
  background: #F5D5B8;
}
```

### tatyana.xml → Animations

```css
/* From: <animation name="idle" fps="6" loop="true"> */
.character.idle {
  animation: idleBreathe 2000ms ease-in-out infinite;
}
```

## Benefits

1. **Zero Asset Loading** - No image files to download
2. **Instant Updates** - Change colors/shapes via CSS
3. **Scalable** - Vector-like quality at any resolution
4. **Prompt-Driven** - Visual specs define the code
5. **Performant** - GPU-accelerated CSS transforms

## Future Extensions

- Parse more objects from `RoomPage.xml` (mirror, wardrobe)
- Add `ParkPage.xml` scene
- Implement action menu from `ActionMenu.xml`
- Canvas/WebGL renderer option for more complex effects
- SVG-based alternative for finer detail

## References

- `docs/prompts/screens/room/RoomPage.txt` - Room specification
- `docs/prompts/characters/tatyana/visual-specs.txt` - Character colors
- `docs/prompts/animations/character/tatyana.txt` - Animation states
- `docs/prompts/design/DESIGN_SYSTEM.txt` - Styling rules
