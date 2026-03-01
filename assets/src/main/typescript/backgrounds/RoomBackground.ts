/**
 * Isometric room background
 * Procedurally drawn bedroom with floor, walls, window
 */

import { BackgroundRenderer, BackgroundConfig } from './BackgroundRenderer';

export class RoomBackground extends BackgroundRenderer {
  constructor(config: BackgroundConfig) {
    super(config);
  }

  /**
   * Draw wooden floor tiles
   */
  private drawFloor(): void {
    const tileSize = 1;
    const roomSize = 6;

    // Base floor color - warm cream
    const baseColor = '#F5F1E8';
    const tileColor1 = '#EDE9E0';
    const tileColor2 = '#F0EBE3';

    for (let x = -roomSize; x < roomSize; x++) {
      for (let y = -roomSize; y < roomSize; y++) {
        // Alternate tile colors for wooden floor pattern
        const color = (x + y) % 2 === 0 ? tileColor1 : tileColor2;
        this.drawIsoTile(x, y, tileSize, tileSize, color);

        // Draw tile borders (grooves between planks)
        const topLeft = this.isoToScreen(x, y);
        const topRight = this.isoToScreen(x + tileSize, y);
        const bottomRight = this.isoToScreen(x + tileSize, y + tileSize);
        const bottomLeft = this.isoToScreen(x, y + tileSize);

        this.ctx.strokeStyle = '#D8D4CC';
        this.ctx.lineWidth = 0.5;
        this.ctx.beginPath();
        this.ctx.moveTo(topLeft.x, topLeft.y);
        this.ctx.lineTo(topRight.x, topRight.y);
        this.ctx.lineTo(bottomRight.x, bottomRight.y);
        this.ctx.lineTo(bottomLeft.x, bottomLeft.y);
        this.ctx.closePath();
        this.ctx.stroke();
      }
    }
  }

  /**
   * Draw left wall
   */
  private drawLeftWall(): void {
    const wallHeight = 4;
    const roomSize = 6;

    // Wall color - light beige
    const wallColor = '#E8DCC8';
    const shadowColor = '#D8CCB8';

    for (let y = -roomSize; y < roomSize; y++) {
      for (let h = 0; h < wallHeight; h++) {
        const topLeft = this.isoToScreen(-roomSize, y + h / wallHeight);
        const topRight = this.isoToScreen(-roomSize, y + (h + 1) / wallHeight);
        const bottomRight = {
          x: topRight.x,
          y: topRight.y - this.config.height / (wallHeight * 2),
        };
        const bottomLeft = {
          x: topLeft.x,
          y: topLeft.y - this.config.height / (wallHeight * 2),
        };

        // Slight gradient for depth
        const color = h === 0 ? shadowColor : wallColor;
        this.ctx.fillStyle = color;
        this.ctx.beginPath();
        this.ctx.moveTo(topLeft.x, topLeft.y);
        this.ctx.lineTo(topRight.x, topRight.y);
        this.ctx.lineTo(bottomRight.x, bottomRight.y);
        this.ctx.lineTo(bottomLeft.x, bottomLeft.y);
        this.ctx.closePath();
        this.ctx.fill();
      }
    }
  }

  /**
   * Draw back wall with window
   */
  private drawBackWall(): void {
    const wallHeight = 4;
    const roomSize = 6;
    const wallColor = '#E8DCC8';

    // Draw wall segments
    for (let x = -roomSize; x < roomSize; x++) {
      for (let h = 0; h < wallHeight; h++) {
        const topLeft = this.isoToScreen(x, -roomSize + h / wallHeight);
        const topRight = this.isoToScreen(x + 1, -roomSize + h / wallHeight);
        const bottomRight = {
          x: topRight.x,
          y: topRight.y - this.config.height / (wallHeight * 2),
        };
        const bottomLeft = {
          x: topLeft.x,
          y: topLeft.y - this.config.height / (wallHeight * 2),
        };

        this.ctx.fillStyle = wallColor;
        this.ctx.beginPath();
        this.ctx.moveTo(topLeft.x, topLeft.y);
        this.ctx.lineTo(topRight.x, topRight.y);
        this.ctx.lineTo(bottomRight.x, bottomRight.y);
        this.ctx.lineTo(bottomLeft.x, bottomLeft.y);
        this.ctx.closePath();
        this.ctx.fill();
      }
    }

    // Draw window on left part of back wall
    this.drawWindow();
  }

  /**
   * Draw window with curtains and light
   */
  private drawWindow(): void {
    const windowX = -4;
    const windowY = -5.8;
    const windowWidth = 2;
    const windowHeight = 2.5;

    const windowPos = this.isoToScreen(windowX, windowY);

    // Window frame (darker)
    this.ctx.fillStyle = '#A0A0A0';
    this.ctx.fillRect(windowPos.x, windowPos.y - 80, windowWidth * 30, windowHeight * 30);

    // Window glass (light blue with transparency)
    const lighting = this.getLightingColor();
    const windowColor =
      this.config.timeOfDay === 'night' ? '#1a2540' : '#B8D4E8';

    this.ctx.fillStyle = windowColor;
    this.ctx.fillRect(
      windowPos.x + 5,
      windowPos.y - 75,
      windowWidth * 30 - 10,
      windowHeight * 30 - 10
    );

    // Window cross (mullions)
    this.ctx.strokeStyle = '#808080';
    this.ctx.lineWidth = 2;
    // Vertical
    this.ctx.beginPath();
    this.ctx.moveTo(windowPos.x + windowWidth * 15, windowPos.y - 75);
    this.ctx.lineTo(windowPos.x + windowWidth * 15, windowPos.y - 75 + windowHeight * 30 - 10);
    this.ctx.stroke();
    // Horizontal
    this.ctx.beginPath();
    this.ctx.moveTo(windowPos.x + 5, windowPos.y - 75 + (windowHeight * 30 - 10) / 2);
    this.ctx.lineTo(
      windowPos.x + windowWidth * 30 - 5,
      windowPos.y - 75 + (windowHeight * 30 - 10) / 2
    );
    this.ctx.stroke();

    // Light rays from window (if daytime)
    if (this.config.timeOfDay === 'morning' || this.config.timeOfDay === 'day') {
      const gradient = this.ctx.createLinearGradient(
        windowPos.x,
        windowPos.y,
        windowPos.x + 200,
        windowPos.y + 200
      );
      gradient.addColorStop(0, 'rgba(255, 250, 220, 0.3)');
      gradient.addColorStop(1, 'rgba(255, 250, 220, 0)');

      this.ctx.fillStyle = gradient;
      this.ctx.beginPath();
      this.ctx.moveTo(windowPos.x + windowWidth * 15, windowPos.y - 40);
      this.ctx.lineTo(windowPos.x + 150, windowPos.y + 100);
      this.ctx.lineTo(windowPos.x + 100, windowPos.y + 120);
      this.ctx.closePath();
      this.ctx.fill();
    }
  }

  /**
   * Draw small indoor plant near window
   */
  private drawPlant(): void {
    const plantX = -3.5;
    const plantY = -4;
    const plantPos = this.isoToScreen(plantX, plantY);

    // Pot (brown)
    this.ctx.fillStyle = '#8B6F47';
    this.ctx.beginPath();
    this.ctx.ellipse(plantPos.x, plantPos.y, 12, 8, 0, 0, Math.PI * 2);
    this.ctx.fill();

    // Pot rim
    this.ctx.fillStyle = '#A0824F';
    this.ctx.beginPath();
    this.ctx.ellipse(plantPos.x, plantPos.y - 8, 13, 9, 0, 0, Math.PI * 2);
    this.ctx.fill();

    // Plant leaves (simple green circles)
    const leafColor = '#7EB09B';
    this.ctx.fillStyle = leafColor;

    // 5 leaves at different positions
    const leaves = [
      { x: -8, y: -20 },
      { x: 8, y: -18 },
      { x: 0, y: -25 },
      { x: -5, y: -15 },
      { x: 5, y: -22 },
    ];

    leaves.forEach((leaf) => {
      this.ctx.beginPath();
      this.ctx.ellipse(plantPos.x + leaf.x, plantPos.y + leaf.y, 6, 10, Math.random(), 0, Math.PI * 2);
      this.ctx.fill();
    });
  }

  /**
   * Main render method
   */
  public render(): HTMLCanvasElement {
    // Clear canvas
    this.ctx.clearRect(0, 0, this.config.width, this.config.height);

    // Fill background with sky color
    const bgColor = this.config.timeOfDay === 'night' ? '#1a1a2e' : '#B8D8E8';
    this.ctx.fillStyle = bgColor;
    this.ctx.fillRect(0, 0, this.config.width, this.config.height);

    // Draw elements in correct order (back to front)
    this.drawBackWall();
    this.drawLeftWall();
    this.drawFloor();
    this.drawPlant();

    // Apply time-of-day lighting
    this.applyLighting();

    return this.canvas;
  }
}
