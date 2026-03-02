# LPC Generator Library

## Overview

`lpc-generator` is a standalone Maven module/library for generating LPC (Liberated Pixel Cup) character sprites programmatically.

### Features

- ✅ **URL Generation**: Build LPC Generator URLs with hash parameters
- ✅ **Character Configuration**: Type-safe API for character appearance
- ✅ **Sprite Management**: Check existence, generate on-demand
- 🚧 **API Integration**: Download sprites via API (coming soon)
- 🚧 **Prompt Integration**: Auto-scan prompts and generate missing sprites

## Installation

### As Maven Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>ru.lifegame</groupId>
    <artifactId>lpc-generator</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### As Standalone Module

```bash
cd lpc-generator
mvn clean install
```

## Usage

### 1. Basic URL Generation

```java
import ru.lifegame.lpc.LpcGeneratorService;
import ru.lifegame.lpc.model.LpcCharacterConfig;

// Create service
LpcGeneratorService lpcService = new LpcGeneratorService();

// Build character config
LpcCharacterConfig tatyana = LpcCharacterConfig.builder()
    .characterId("tatyana")
    .sex("female")
    .body("Body_Color_light")
    .head("Human_Female_light")
    .expression("Neutral_light")
    .hair(List.of("Shoulder_burgundy"))
    .build();

// Add clothing
tatyana.addClothing("tops", "Longsleeve_beige");
tatyana.addClothing("bottoms", "Pants_gray_blue");
tatyana.addClothing("shoes", "Slippers_white");

// Add accessories
tatyana.addAccessory("necklaces", "Heart_gold");

// Generate URL
String url = lpcService.generateUrl(tatyana);
// Result: https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/#sex=female&body=Body_Color_light&head=Human_Female_light&expression=Neutral_light&hair=Shoulder_burgundy&torso=Longsleeve_beige&legs=Pants_gray_blue&feet=Slippers_white&necklace=Heart_gold
```

### 2. Check Sprite Existence

```java
import ru.lifegame.lpc.model.LpcSpriteRequest;

LpcSpriteRequest request = LpcSpriteRequest.builder()
    .characterId("tatyana")
    .animationName("idle-neutral")
    .config(tatyana)
    .build();

boolean exists = lpcService.spriteExists(request);
if (!exists) {
    System.out.println("Sprite not found, need to generate!");
}
```

### 3. Generate Sprite (Future)

```java
// This will be implemented when API integration is ready
Path spritePath = lpcService.generateSprite(request);
System.out.println("Sprite saved to: " + spritePath);
```

## Architecture

```
lpc-generator/
├── src/main/java/ru/lifegame/lpc/
│   ├── LpcGeneratorService.java      # Main API
│   ├── model/
│   │   ├── LpcCharacterConfig.java   # Character appearance config
│   │   └── LpcSpriteRequest.java     # Sprite generation request
│   ├── url/
│   │   └── LpcUrlBuilder.java        # URL parameter builder
│   └── api/
│       └── LpcApiClient.java         # API integration (TODO)
└── pom.xml
```

## LPC URL Parameter Format

### Example URL

```
https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/#sex=female&body=Body_Color_light&head=Human_Female_light&expression=Neutral_light&hair=Shoulder_burgundy
```

### Parameter Types

| Parameter | Description | Example |
|-----------|-------------|----------|
| `sex` | Character sex | `male`, `female` |
| `body` | Body type & skin | `Body_Color_light`, `Body_Color_dark` |
| `head` | Head shape | `Human_Male_light`, `Human_Female_light` |
| `expression` | Facial expression | `Neutral_light`, `Happy_light` |
| `hair` | Hair layer(s) | `Shoulder_burgundy`, `Bangs_black` |
| `torso` | Top clothing | `Longsleeve_beige`, `Turtleneck_white` |
| `legs` | Bottom clothing | `Pants_gray_blue`, `Jeans_blue` |
| `feet` | Footwear | `Slippers_white`, `Boots_brown` |
| `necklace` | Necklace | `Heart_gold`, `Chain_silver` |
| `bracers` | Arm accessories | `Bracers_gold` |

## Integration with Prompts

### Prompt Directory Structure

```
docs/prompts/
└── characters/
    └── tatyana/
        ├── base-sprite.txt          # Base character appearance
        └── animations/
            ├── idle-neutral.txt     # Animation prompt
            ├── walk-south.txt
            └── work-computer.txt
```

### Assets Directory Structure (Mirror)

```
assets/characters/
└── tatyana/
    └── animations/
        ├── idle-neutral.png         # Generated sprite
        ├── walk-south.png
        └── work-computer.png
```

### Naming Convention

**Prompts**: `{animation-name}.txt` → **Sprites**: `{animation-name}.png`

Examples:
- `idle-neutral.txt` → `idle-neutral.png`
- `walk-south.txt` → `walk-south.png`
- `emotion-joy.txt` → `emotion-joy.png`

## API Integration (TODO)

### Option 1: Headless Browser (Selenium/Playwright)

```java
// Use headless Chrome to render LPC Generator and extract canvas
WebDriver driver = new ChromeDriver(options);
driver.get(lpcUrl);
// ... wait for render, extract canvas as PNG
```

### Option 2: Server-Side Composer

```java
// Implement sprite composition using LPC asset library
// Download individual layer PNGs and composite them
```

### Option 3: External API Service

```java
// Call external service that provides LPC sprite generation API
RestTemplate client = new RestTemplate();
byte[] sprite = client.postForObject(API_URL, request, byte[].class);
```

## Configuration

### application.properties

```properties
# LPC Generator settings
lpc.generator.base-url=https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/
lpc.generator.api-endpoint=https://api.example.com/lpc/generate
lpc.generator.output-directory=assets/characters
lpc.generator.default-format=png
```

## Testing

### Run Tests

```bash
mvn test
```

### Example Test

```java
@Test
void testUrlGeneration() {
    LpcCharacterConfig config = LpcCharacterConfig.builder()
        .sex("female")
        .body("Body_Color_light")
        .head("Human_Female_light")
        .build();

    String url = urlBuilder.build(config);
    
    assertThat(url).contains("sex=female");
    assertThat(url).contains("body=Body_Color_light");
}
```

## Roadmap

### Phase 1: URL Generation ✅
- [x] LpcCharacterConfig model
- [x] LpcUrlBuilder
- [x] LpcGeneratorService basic API

### Phase 2: API Integration 🚧
- [ ] Choose implementation approach (Selenium vs Server-side)
- [ ] Implement sprite download
- [ ] Add caching layer
- [ ] Error handling & retries

### Phase 3: Prompt Integration 📋
- [ ] Prompt scanner
- [ ] Auto-generate missing sprites
- [ ] Config extractor from prompts
- [ ] Batch generation

### Phase 4: Optimization 📋
- [ ] Sprite compression (WebP)
- [ ] Parallel generation
- [ ] CDN integration
- [ ] Version management

## License

This library generates URLs for and optionally downloads sprites from the Universal LPC Spritesheet Character Generator, which is licensed under CC-BY-SA 3.0 / GPL 3.0.

**Attribution Required**: Any sprites generated must include proper attribution to LPC contributors.

## Links

- [LPC Generator](https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/)
- [LPC GitHub](https://github.com/LiberatedPixelCup/Universal-LPC-Spritesheet-Character-Generator)
- [LPC Credits](https://github.com/LiberatedPixelCup/Universal-LPC-Spritesheet-Character-Generator/blob/master/CREDITS.md)
