# LPC Character Generator Integration

## Overview

This document describes the integration of Universal LPC Spritesheet character sprites into Life of T.

## What is LPC?

The **Liberated Pixel Cup (LPC)** is a community-driven collection of 64x64 pixel art character sprites with modular clothing, hair, and accessories. The Universal LPC Spritesheet Generator allows creating custom characters by mixing and matching layers.

- **Generator URL**: https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/
- **GitHub**: https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator
- **License**: CC-BY-SA 3.0 / GPL 3.0 (requires attribution)

## Generating Tatyana's Spritesheet

### Step 1: Open the Generator

Visit [LPC Generator](https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/)

### Step 2: Configure Character

Based on `docs/prompts/character-visual-specs.txt`:

#### Body
- **Body**: Light (warm beige skin tone #F5D5B8)
- **Age**: Female adult

#### Hair
- **Style**: Shoulder length, wavy
- **Color**: Dark red / burgundy (#8B1A1A)
- Select "Shoulder" or "Shouldercurly" hair
- Use color picker to set burgundy tone

#### Clothes (home-work outfit)
- **Top**: White or beige sweater/shirt
  - Look for: "Longsleeve", "Turtleneck", or "Sweater"
  - Color: Beige (#F5E6D3) or White
- **Bottom**: Gray-blue jeans
  - Select "Pants" layer
  - Color: Gray-blue (#6B7280)
- **Shoes**: White sneakers with pink/rose accent
  - Select "Shoes" → "Slippers" or "Boots"
  - Colors: White (#FFF9E9) + Rose accent (#FFB6C1)

#### Accessories
- **Necklace**: Gold heart necklace
  - Select "Necklace" → Heart pendant
  - Color: Gold (#FFD700)

### Step 3: Preview Animations

Check the following animations work correctly:
- Walk (South, West, East, North)
- Spellcast (can be used for emotion)
- Hurt (can be used for tired state)

### Step 4: Download Spritesheet

1. Click "Download" button
2. Select format: **PNG** (not WebP, for better compatibility)
3. Save as `tatyana-lpc-base.png`
4. Place in `frontend/public/assets/characters/tatyana/`

### Step 5: Save Attribution

Create `frontend/public/assets/characters/tatyana/CREDITS.txt` with:

```
Character sprite generated using Universal LPC Spritesheet Character Generator
https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/

License: CC-BY-SA 3.0 / GPL 3.0
Contributors: [See generator page for full credits]

Base character layers used:
- Body: Light skin tone
- Hair: Shoulder wavy burgundy
- Top: Beige sweater
- Bottom: Gray-blue jeans
- Shoes: White sneakers
- Accessories: Gold heart necklace
```

## Using in Code

### Import Component

```typescript
import { LPCCharacter } from './components/scene/LPCCharacter';
```

### Example Usage

```typescript
// Idle state
<LPCCharacter
  position={{ x: 100, y: 200, zIndex: 4 }}
  spritesheet="/assets/characters/tatyana/tatyana-lpc-base.png"
  state="idle"
  direction="south"
/>

// Walking
<LPCCharacter
  position={{ x: 100, y: 200, zIndex: 4 }}
  spritesheet="/assets/characters/tatyana/tatyana-lpc-base.png"
  state="walk"
  direction="south"
  emotion="happy"
/>
```

## Spritesheet Layout

LPC spritesheets follow this standard layout (13 rows × variable columns):

| Row | Animation | Frames | Usage in Life of T |
|-----|-----------|--------|--------------------|
| 0   | Spellcast | 7      | Emotion (joy, surprise) |
| 1   | Thrust    | 8      | Not used |
| 8   | Walk Up   | 9      | walk direction="north" |
| 9   | Walk Left | 9      | walk direction="west" |
| 10  | Walk Down | 9      | walk direction="south" |
| 11  | Walk Right| 9      | walk direction="east" |
| 12  | Slash     | 6      | Not used |
| 13  | Shoot     | 13     | Interaction actions |
| 20  | Hurt      | 6      | Tired/stressed state |

## Mapping LPC to Life of T States

### Character States

| Life of T State | LPC Animation | Notes |
|-----------------|---------------|-------|
| `idle-neutral`  | Walk frame 0  | Use first frame of walk-south |
| `idle-happy`    | Walk frame 0  | Same, will add emotion overlay later |
| `idle-tired`    | Hurt frame 0  | Slumped posture |
| `walk`          | Walk (4 dirs) | Full 9-frame animation |
| `work-computer` | Walk frame 0  | Static, add laptop prop separately |
| `sleep`         | Custom        | Will need custom horizontal sprite |
| `emotion-joy`   | Spellcast     | Arms raised effect |
| `interaction`   | Shoot         | Picking up objects |

### Limitations

**Not included in LPC:**
- ✗ Isometric perspective (LPC is top-down)
- ✗ Work/sitting animations
- ✗ Sleep/lying down animations
- ✗ Grooming/mirror animations

**Workarounds:**
1. Use LPC for **prototype v0.1.0**
2. Commission custom isometric sprites later
3. Use static poses + animated props for work/sleep

## Next Steps

1. [ ] Generate Tatyana's spritesheet
2. [ ] Save to `frontend/public/assets/characters/tatyana/`
3. [ ] Update `RoomScreen.tsx` to use `LPCCharacter`
4. [ ] Test walk animations in all directions
5. [ ] Add emotion overlay system
6. [ ] Create Sam (husband) spritesheet
7. [ ] Create Garfield (cat) spritesheet (if available in LPC)

## Resources

- [LPC Generator](https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/)
- [LPC Forum](https://opengameart.org/content/lpc-character-generator)
- [Asset Credits](https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator/blob/master/CREDITS.TXT)
- [License Info](https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator/blob/master/COPYING.txt)

## Attribution Requirements

Per CC-BY-SA 3.0 license, must include:

1. Credits file with contributor list
2. Link to original generator
3. License notice in game credits/about screen
4. If modified, note modifications made

See `frontend/public/assets/characters/tatyana/CREDITS.txt` for details.