/**
 * Outdoor park background
 * Procedurally drawn park scene with trees, path, pond
 */

import { BackgroundRenderer, BackgroundConfig } from './BackgroundRenderer';

export class ParkBackground extends BackgroundRenderer {
  constructor(config: BackgroundConfig) {
    super(config);
  }

  /**
   * Draw grass ground
   */
  private drawGrass(): void {
    const grassColors = ['#7CB342', '#8BC34A', '#9CCC65'];

    // Base grass layer
    const baseColor = '#689F38';
    this.ctx.fillStyle = baseColor;
    this.ctx.fillRect(0, this.config.height * 0.4, this.config.width, this.config.height * 0.6);

    // Grass texture (random dots)
    for (let i = 0; i < 500; i++) {
      const x = Math.random() * this.config.width;
      const y = this.config.height * 0.4 + Math.random() * this.config.height * 0.6;
      const color = grassColors[Math.floor(Math.random() * grassColors.length)];

      this.ctx.fillStyle = color;
      this.ctx.beginPath();
      this.ctx.arc(x, y, Math.random() * 2 + 1, 0, Math.PI * 2);
      this.ctx.fill();
    }
  }

  /**
   * Draw sky with gradient
   */
  private drawSky(): void {
    let skyGradient;

    switch (this.config.timeOfDay) {
      case 'morning':
        skyGradient = this.ctx.createLinearGradient(0, 0, 0, this.config.height * 0.4);
        skyGradient.addColorStop(0, '#FFE5B4');
        skyGradient.addColorStop(1, '#87CEEB');
        break;
      case 'day':
        skyGradient = this.ctx.createLinearGradient(0, 0, 0, this.config.height * 0.4);
        skyGradient.addColorStop(0, '#87CEEB');
        skyGradient.addColorStop(1, '#B0E0E6');
        break;
      case 'evening':
        skyGradient = this.ctx.createLinearGradient(0, 0, 0, this.config.height * 0.4);
        skyGradient.addColorStop(0, '#FF6B6B');
        skyGradient.addColorStop(0.5, '#FFA07A');
        skyGradient.addColorStop(1, '#87CEEB');
        break;
      case 'night':
        skyGradient = this.ctx.createLinearGradient(0, 0, 0, this.config.height * 0.4);
        skyGradient.addColorStop(0, '#0F0F1E');
        skyGradient.addColorStop(1, '#1a1a2e');
        break;
    }

    this.ctx.fillStyle = skyGradient;
    this.ctx.fillRect(0, 0, this.config.width, this.config.height * 0.4);
  }

  /**
   * Draw simple tree
   */
  private drawTree(x: number, y: number, size: number): void {
    // Trunk
    const trunkWidth = size * 0.2;
    const trunkHeight = size * 0.4;

    this.ctx.fillStyle = '#5D4037';
    this.ctx.fillRect(x - trunkWidth / 2, y, trunkWidth, trunkHeight);

    // Foliage (3 circles)
    const foliageColor = this.config.timeOfDay === 'night' ? '#2E7D32' : '#4CAF50';
    this.ctx.fillStyle = foliageColor;

    // Bottom circle
    this.ctx.beginPath();
    this.ctx.arc(x, y - size * 0.1, size * 0.5, 0, Math.PI * 2);
    this.ctx.fill();

    // Middle circle
    this.ctx.beginPath();
    this.ctx.arc(x - size * 0.3, y - size * 0.4, size * 0.4, 0, Math.PI * 2);
    this.ctx.fill();

    // Top circle
    this.ctx.beginPath();
    this.ctx.arc(x + size * 0.3, y - size * 0.3, size * 0.35, 0, Math.PI * 2);
    this.ctx.fill();
  }

  /**
   * Draw walking path
   */
  private drawPath(): void {
    const pathColor = '#D7CCC8';

    // Curved path
    this.ctx.fillStyle = pathColor;
    this.ctx.beginPath();
    this.ctx.moveTo(0, this.config.height * 0.7);
    this.ctx.quadraticCurveTo(
      this.config.width * 0.5,
      this.config.height * 0.6,
      this.config.width,
      this.config.height * 0.75
    );
    this.ctx.lineTo(this.config.width, this.config.height * 0.85);
    this.ctx.quadraticCurveTo(
      this.config.width * 0.5,
      this.config.height * 0.7,
      0,
      this.config.height * 0.8
    );
    this.ctx.closePath();
    this.ctx.fill();

    // Path border
    this.ctx.strokeStyle = '#BCAAA4';
    this.ctx.lineWidth = 2;
    this.ctx.beginPath();
    this.ctx.moveTo(0, this.config.height * 0.7);
    this.ctx.quadraticCurveTo(
      this.config.width * 0.5,
      this.config.height * 0.6,
      this.config.width,
      this.config.height * 0.75
    );
    this.ctx.stroke();
  }

  /**
   * Draw pond
   */
  private drawPond(): void {
    const pondX = this.config.width * 0.7;
    const pondY = this.config.height * 0.55;
    const pondWidth = 120;
    const pondHeight = 80;

    // Water
    const waterColor = this.config.timeOfDay === 'night' ? '#1565C0' : '#42A5F5';
    this.ctx.fillStyle = waterColor;
    this.ctx.beginPath();
    this.ctx.ellipse(pondX, pondY, pondWidth, pondHeight, 0, 0, Math.PI * 2);
    this.ctx.fill();

    // Water reflection (lighter)
    const reflectionColor = this.config.timeOfDay === 'night' ? '#1976D2' : '#64B5F6';
    this.ctx.fillStyle = reflectionColor;
    this.ctx.beginPath();
    this.ctx.ellipse(pondX - 20, pondY - 20, pondWidth * 0.4, pondHeight * 0.3, 0, 0, Math.PI * 2);
    this.ctx.fill();

    // Pond border (darker water)
    this.ctx.strokeStyle = this.config.timeOfDay === 'night' ? '#0D47A1' : '#1E88E5';
    this.ctx.lineWidth = 3;
    this.ctx.beginPath();
    this.ctx.ellipse(pondX, pondY, pondWidth, pondHeight, 0, 0, Math.PI * 2);
    this.ctx.stroke();
  }

  /**
   * Draw stars (night only)
   */
  private drawStars(): void {
    if (this.config.timeOfDay !== 'night') return;

    this.ctx.fillStyle = '#FFFFFF';
    for (let i = 0; i < 50; i++) {
      const x = Math.random() * this.config.width;
      const y = Math.random() * this.config.height * 0.3;
      const size = Math.random() * 2;

      this.ctx.beginPath();
      this.ctx.arc(x, y, size, 0, Math.PI * 2);
      this.ctx.fill();
    }
  }

  /**
   * Main render method
   */
  public render(): HTMLCanvasElement {
    // Clear canvas
    this.ctx.clearRect(0, 0, this.config.width, this.config.height);

    // Draw elements in correct order (back to front)
    this.drawSky();
    this.drawStars();
    this.drawGrass();
    this.drawPath();
    this.drawPond();

    // Trees (background)
    this.drawTree(150, this.config.height * 0.5, 80);
    this.drawTree(this.config.width - 180, this.config.height * 0.48, 100);
    this.drawTree(300, this.config.height * 0.52, 70);

    // Apply time-of-day lighting
    this.applyLighting();

    return this.canvas;
  }
}
