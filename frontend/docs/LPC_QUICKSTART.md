# LPC Integration Quickstart

## 🚀 Fast Track: Get Tatyana Sprite in 5 Minutes

### Step 1: Generate Sprite (2 minutes)

1. Open https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/
2. **Body Tab**:
   - Click "Body" → Select "Light" skin tone
   - Gender: Female
3. **Hair Tab**:
   - Style: "Shoulder" or "Shouldercurly"
   - Color picker: Enter `#8B1A1A` (burgundy)
4. **Clothes Tab**:
   - Top: "Longsleeve" or "Turtleneck" → Color `#F5E6D3` (beige)
   - Bottom: "Pants" → Color `#6B7280` (gray-blue)
   - Shoes: "Slippers" or "Boots" → White `#FFFFFF`
5. **Accessories** (optional):
   - Necklace: "Heart" pendant → Gold `#FFD700`

6. **Download**:
   - Click "Download" button (top-right)
   - Format: PNG
   - Save as `tatyana-lpc-base.png`

### Step 2: Place File (30 seconds)

```bash
# Create directory
mkdir -p frontend/public/assets/characters/tatyana

# Move downloaded file
mv ~/Downloads/tatyana-lpc-base.png frontend/public/assets/characters/tatyana/
```

### Step 3: Update Code (2 minutes)

Edit `frontend/src/components/scene/RoomScreen.tsx`:

```typescript
// OLD (CSS-based character)
import { Character } from './Character';

// NEW (LPC sprite character)
import { LPCCharacter } from './LPCCharacter';

// ...

// REPLACE this:
<Character
  position={{ x: 200, y: 150, zIndex: 4 }}
  state="idle"
  emotion="neutral"
/>

// WITH this:
<LPCCharacter
  position={{ x: 200, y: 150, zIndex: 4 }}
  spritesheet="/assets/characters/tatyana/tatyana-lpc-base.png"
  state="idle"
  direction="south"
  emotion="neutral"
/>
```

### Step 4: Test (30 seconds)

```bash
cd frontend
npm run dev
```

Open http://localhost:5173 → RoomScreen should show LPC sprite!

---

## 🎯 Testing Animations

### Walk Animation

Update state to test walking:

```typescript
<LPCCharacter
  spritesheet="/assets/characters/tatyana/tatyana-lpc-base.png"
  state="walk"  // Changed from 'idle'
  direction="south"  // Try: north, south, east, west
/>
```

### Different Directions

Test all 4 directions:
- `direction="south"` — walking down
- `direction="north"` — walking up
- `direction="west"` — walking left
- `direction="east"` — walking right

---

## 🛠️ Troubleshooting

### Sprite not loading?

1. Check file path: `frontend/public/assets/characters/tatyana/tatyana-lpc-base.png`
2. Check browser console for errors
3. Hard refresh: Ctrl+Shift+R (Windows) / Cmd+Shift+R (Mac)

### Sprite looks weird?

- LPC is **top-down**, not isometric → expected for v0.1.0 prototype
- Use for testing gameplay, will replace with isometric sprites later

### Animation not playing?

- Check `state="walk"` (not "idle")
- Check `direction` prop is set
- Open DevTools → check console logs from `[LPCCharacter]`

---

## 🎮 Next: Make Character Interactive

Add click-to-walk:

```typescript
const [charPos, setCharPos] = useState({ x: 200, y: 150 });
const [isWalking, setIsWalking] = useState(false);

const handleSceneClick = (e: React.MouseEvent) => {
  const newX = e.clientX;
  const newY = e.clientY;
  
  setCharPos({ x: newX, y: newY });
  setIsWalking(true);
  
  setTimeout(() => setIsWalking(false), 1000);
};

return (
  <div onClick={handleSceneClick}>
    <LPCCharacter
      position={{ ...charPos, zIndex: 4 }}
      state={isWalking ? "walk" : "idle"}
      direction="south"
    />
  </div>
);
```

---

## 📚 Full Documentation

See [LPC_INTEGRATION.md](./LPC_INTEGRATION.md) for complete details.

---

## ✅ Done!

You now have a working LPC sprite character in Life of T!

**What's next?**
- Add Sam (husband) sprite
- Add emotion overlays
- Connect to GameState for real-time state changes
- Plan isometric sprite replacement for v2