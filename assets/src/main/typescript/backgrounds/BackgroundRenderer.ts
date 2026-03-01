/**
 * Base class for procedural background rendering
 * All backgrounds are drawn programmatically using Canvas API
 */

export interface BackgroundConfig {
  width: number;
  height: number;
  timeOfDay: 'morning' | 'day' | 'evening' | 'night';
}

export interface Point {
  x: number;
  y: number;
}

export abstract class BackgroundRenderer {
  protected canvas: HTMLCanvasElement;
  protected ctx: CanvasRenderingContext2D;
  protected config: BackgroundConfig;

  constructor(config: BackgroundConfig) {
    this.config = config;
    this.canvas = document.createElement('canvas');
    this.canvas.width = config.width;
    this.canvas.height = config.height;
    this.ctx = this.canvas.getContext('2d')!;
  }

  /**
   * Convert isometric 2D coordinates to screen coordinates
   * Used for isometric projection (45-degree angle)
   */
  protected isoToScreen(x: number, y: number): Point {
    const screenX = (x - y) * (this.config.width / 4);
    const screenY = (x + y) * (this.config.height / 8);
    return { x: screenX + this.config.width / 2, y: screenY };
  }

  /**
   * Draw isometric tile (parallelogram)
   */
  protected drawIsoTile(
    x: number,
    y: number,
    width: number,
    height: number,
    color: string
  ): void {
    const topLeft = this.isoToScreen(x, y);
    const topRight = this.isoToScreen(x + width, y);
    const bottomRight = this.isoToScreen(x + width, y + height);
    const bottomLeft = this.isoToScreen(x, y + height);

    this.ctx.fillStyle = color;
    this.ctx.beginPath();
    this.ctx.moveTo(topLeft.x, topLeft.y);
    this.ctx.lineTo(topRight.x, topRight.y);
    this.ctx.lineTo(bottomRight.x, bottomRight.y);
    this.ctx.lineTo(bottomLeft.x, bottomLeft.y);
    this.ctx.closePath();
    this.ctx.fill();
  }

  /**
   * Get lighting color based on time of day
   */
  protected getLightingColor(): { ambient: string; shadow: string } {
    switch (this.config.timeOfDay) {
      case 'morning':
        return { ambient: 'rgba(255, 250, 220, 0.3)', shadow: 'rgba(100, 100, 150, 0.2)' };
      case 'day':
        return { ambient: 'rgba(255, 255, 255, 0.1)', shadow: 'rgba(50, 50, 100, 0.15)' };
      case 'evening':
        return { ambient: 'rgba(255, 200, 150, 0.4)', shadow: 'rgba(100, 80, 120, 0.3)' };
      case 'night':
        return { ambient: 'rgba(100, 120, 200, 0.5)', shadow: 'rgba(20, 20, 50, 0.5)' };
    }
  }

  /**
   * Apply lighting overlay
   */
  protected applyLighting(): void {
    const lighting = this.getLightingColor();
    this.ctx.fillStyle = lighting.ambient;
    this.ctx.fillRect(0, 0, this.config.width, this.config.height);
  }

  /**
   * Render the background
   */
  abstract render(): HTMLCanvasElement;

  /**
   * Get base64 data URL
   */
  public toDataURL(): string {
    return this.canvas.toDataURL('image/png');
  }
}
