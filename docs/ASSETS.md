# Asset Generation & Serving

## Architecture Overview

**Life of T** uses a **data-driven asset pipeline**: all visual assets (PNG sprites, animation configs) are generated from XML specs and served dynamically by the backend.

```
asset-specs/*.xml  →  asset-generator  →  target/generated-assets/  →  Backend serves via REST + static
                                                                          ↓
                                                                      Frontend loads assets from backend
```

---

## Asset Specs → Generated Assets

### Input: `asset-specs/`

```
asset-specs/
├── characters/
│   ├── sam/
│   │   └── visual-specs.xml     ← Pixel art spec (layers, colors, animations)
│   ├── tanya/
│   └── ...
├── locations/
│   ├── kitchen/
│   │   └── visual-specs.xml
│   └── ...
├── furniture/
├── ui/
└── abstract/                    ← Templates (e.g., abstract/entities/human@1.0)
```

### Output: `target/generated-assets/`

```
target/generated-assets/
├── characters/
│   └── sam/
│       ├── idle_atlas.png       ← PNG sprite strip
│       ├── walking_atlas.png
│       └── sprite-atlas.json    ← Animation config (fps, frames, conditions)
├── locations/
│   └── kitchen/
│       ├── background.png
│       ├── midground.png
│       └── sprite-atlas.json
└── ...
```

---

## Backend: Asset Generation & Serving

### 1. Build-Time Generation

**Maven executes `asset-generator` during `generate-resources` phase:**

```xml
<!-- backend/pom.xml -->
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>generate-assets</id>
      <phase>generate-resources</phase>
      <goals><goal>java</goal></goals>
      <configuration>
        <mainClass>ru.lifegame.assets.AssetGeneratorRunner</mainClass>
        <arguments>
          <argument>../asset-specs</argument>
          <argument>target/generated-assets</argument>
        </arguments>
      </configuration>
    </execution>
  </executions>
</plugin>
```

**Build flow:**
```bash
mvn clean package
  1. asset-generator module compiles
  2. backend runs AssetGeneratorRunner:
     - Reads asset-specs/**/*.xml
     - Generates PNG + sprite-atlas.json → target/generated-assets/
  3. maven-resources-plugin copies target/generated-assets/ → classpath:assets/
  4. Assets bundled in backend JAR
```

### 2. Runtime Serving

**Backend serves assets via two mechanisms:**

#### A. Static PNG/atlas files: `/assets/**`

```java
// WebConfig.java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/assets/")
            .setCachePeriod(3600);
}
```

**Example:**
```
GET http://localhost:8080/assets/characters/sam/idle_atlas.png
→ Returns PNG sprite strip from classpath:assets/characters/sam/idle_atlas.png
```

#### B. Animation configs: `/api/v1/assets/configs/{type}/{id}`

```java
// AssetConfigController.java
@GetMapping("/configs/{type}/{id}")
public ResponseEntity<JsonNode> getAtlasConfig(
    @PathVariable String type,  // characters, locations, furniture, ui
    @PathVariable String id     // sam, kitchen, bed, ...
) {
    String path = "assets/" + type + "/" + id + "/sprite-atlas.json";
    JsonNode config = loadFromClasspath(path);
    return ResponseEntity.ok(config);
}
```

**Example:**
```
GET http://localhost:8080/api/v1/assets/configs/characters/sam
→ Returns sprite-atlas.json:
{
  "configVersion": "1.4",
  "entity": "sam",
  "displayScale": 1.5,
  "animations": {
    "idle": {
      "file": "idle_atlas.png",
      "layout": "strip",
      "frameWidth": 128,
      "frameHeight": 192,
      "fps": 8,
      "loop": true
    },
    ...
  }
}
```

---

## Frontend: Asset Loading

### Vite Dev Mode (localhost:5173)

**Vite proxies asset requests to backend:**

```typescript
// vite.config.ts
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',      // API calls
      '/assets': 'http://localhost:8080',   // PNG/atlas files
    },
  },
});
```

**Frontend workflow:**

```typescript
// 1. Load animation config
const config = await fetch('/api/v1/assets/configs/characters/sam');
//   → Vite proxy → http://localhost:8080/api/v1/assets/configs/characters/sam

// 2. Load sprite atlas PNG
const atlas = '/assets/characters/sam/idle_atlas.png';
//   → Vite proxy → http://localhost:8080/assets/characters/sam/idle_atlas.png

// 3. Animation Resolver uses config to pick animation based on context
const animation = animationResolver.resolve({
  activityId: 'idle',
  mood: 'happy',
});
//   → Returns: { key: 'idle', file: 'idle_atlas.png', fps: 8, ... }
```

---

## Development Workflow

### Terminal 1: Backend

```bash
cd backend
mvn clean package    # Generates assets from asset-specs/
mvn spring-boot:run  # Starts backend on http://localhost:8080
```

**Backend serves:**
- `/api/v1/game/**` — Game logic
- `/api/v1/assets/configs/**` — sprite-atlas.json
- `/assets/**` — PNG sprite strips

### Terminal 2: Frontend

```bash
cd frontend
npm install
npm run dev          # Starts Vite on http://localhost:5173
```

**Frontend loads:**
- UI React app from Vite dev server
- Assets (PNG + JSON) proxied from backend

### Open Browser

```
http://localhost:5173
```

---

## Hot Reload: Changing Assets

### 1. Edit Asset Spec

```bash
# Edit asset-specs/characters/sam/visual-specs.xml
# Example: change idle animation fps from 8 to 12
```

### 2. Regenerate Assets

```bash
cd backend
mvn generate-resources  # Re-runs asset-generator
```

### 3. Restart Backend (or use Spring DevTools)

```bash
mvn spring-boot:run
```

### 4. Frontend Auto-Reloads

- Vite watches for changes
- Re-fetches `/api/v1/assets/configs/characters/sam`
- Loads new PNG from `/assets/characters/sam/idle_atlas.png`

---

## Production Build

```bash
mvn clean package -DskipTests
```

**Output:**
```
application/target/life-of-t.jar
  ├── BOOT-INF/classes/assets/        ← Generated PNG + JSON
  ├── BOOT-INF/classes/static/        ← Frontend React app
  └── ...
```

**Run:**
```bash
java -jar application/target/life-of-t.jar
```

**Access:**
```
http://localhost:8080  ← Frontend UI
http://localhost:8080/api/v1/assets/configs/characters/sam  ← Asset config API
http://localhost:8080/assets/characters/sam/idle_atlas.png  ← Sprite PNG
```

---

## Why This Architecture?

✅ **Single Source of Truth**: All visuals defined in `asset-specs/*.xml`  
✅ **No Frontend Duplication**: Frontend doesn't store PNG/configs, loads from backend  
✅ **Version Control**: XML diffs are readable, PNG generated from source  
✅ **Hot Reload**: Edit XML → regenerate → backend serves new assets  
✅ **Consistent**: Backend controls asset versioning (`sprite-atlas.json` revision field)  
✅ **Production Ready**: Assets bundled in JAR, served efficiently  

---

## Troubleshooting

### Frontend shows "Failed to load asset"

**Check:**
1. Backend is running (`http://localhost:8080/actuator/health`)
2. Assets generated: `ls backend/target/generated-assets/characters/sam/`
3. Vite proxy configured: `frontend/vite.config.ts`
4. Browser devtools: check network tab for 404s

### Assets not regenerating

```bash
# Force clean build
cd backend
mvn clean generate-resources
ls target/generated-assets/  # Should show new files
```

### CORS errors

**Check `backend/src/main/java/.../WebConfig.java`:**
```java
registry.addMapping("/assets/**")
        .allowedOrigins("http://localhost:5173");
```
