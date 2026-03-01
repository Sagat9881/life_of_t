import React, { useEffect, useRef, useState } from 'react';
import styles from './BackgroundTest.module.css';

// Import will be added after assets module is compiled
// For now, we'll inline the renderer logic for testing

type TimeOfDay = 'morning' | 'day' | 'evening' | 'night';

interface BackgroundConfig {
  width: number;
  height: number;
  timeOfDay: TimeOfDay;
}

// Inline BackgroundRenderer for testing
class BackgroundRenderer {
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

  protected isoToScreen(x: number, y: number): { x: number; y: number } {
    const screenX = (x - y) * (this.config.width / 4);
    const screenY = (x + y) * (this.config.height / 8);
    return { x: screenX + this.config.width / 2, y: screenY };
  }

  protected drawIsoTile(x: number, y: number, width: number, height: number, color: string): void {
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

  protected applyLighting(): void {
    const lighting = this.getLightingColor();
    this.ctx.fillStyle = lighting.ambient;
    this.ctx.fillRect(0, 0, this.config.width, this.config.height);
  }

  render(): HTMLCanvasElement {
    return this.canvas;
  }
}

// Inline RoomBackground for testing
class RoomBackground extends BackgroundRenderer {
  private drawFloor(): void {
    const tileSize = 1;
    const roomSize = 6;
    const tileColor1 = '#EDE9E0';
    const tileColor2 = '#F0EBE3';

    for (let x = -roomSize; x < roomSize; x++) {
      for (let y = -roomSize; y < roomSize; y++) {
        const color = (x + y) % 2 === 0 ? tileColor1 : tileColor2;
        this.drawIsoTile(x, y, tileSize, tileSize, color);

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

  render(): HTMLCanvasElement {
    this.ctx.clearRect(0, 0, this.config.width, this.config.height);
    const bgColor = this.config.timeOfDay === 'night' ? '#1a1a2e' : '#B8D8E8';
    this.ctx.fillStyle = bgColor;
    this.ctx.fillRect(0, 0, this.config.width, this.config.height);
    this.drawFloor();
    this.applyLighting();
    return this.canvas;
  }
}

export const BackgroundTest: React.FC = () => {
  const roomCanvasRef = useRef<HTMLDivElement>(null);
  const [timeOfDay, setTimeOfDay] = useState<TimeOfDay>('day');
  const [roomBg, setRoomBg] = useState<RoomBackground | null>(null);

  useEffect(() => {
    if (!roomCanvasRef.current) return;

    const bg = new RoomBackground({
      width: 800,
      height: 600,
      timeOfDay,
    });

    const canvas = bg.render();
    canvas.style.width = '100%';
    canvas.style.height = 'auto';
    canvas.style.border = '2px solid #333';

    roomCanvasRef.current.innerHTML = '';
    roomCanvasRef.current.appendChild(canvas);

    setRoomBg(bg);
  }, [timeOfDay]);

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>🎨 Background Test</h1>
        <p>Процедурная генерация фонов через Canvas API</p>
      </div>

      <div className={styles.controls}>
        <h3>Время суток:</h3>
        <div className={styles.buttons}>
          <button
            className={timeOfDay === 'morning' ? styles.active : ''}
            onClick={() => setTimeOfDay('morning')}
          >
            🌅 Утро
          </button>
          <button
            className={timeOfDay === 'day' ? styles.active : ''}
            onClick={() => setTimeOfDay('day')}
          >
            ☀️ День
          </button>
          <button
            className={timeOfDay === 'evening' ? styles.active : ''}
            onClick={() => setTimeOfDay('evening')}
          >
            🌆 Вечер
          </button>
          <button
            className={timeOfDay === 'night' ? styles.active : ''}
            onClick={() => setTimeOfDay('night')}
          >
            🌙 Ночь
          </button>
        </div>
      </div>

      <div className={styles.backgrounds}>
        <div className={styles.background}>
          <h2>RoomBackground</h2>
          <div ref={roomCanvasRef} className={styles.canvas}></div>
          <div className={styles.info}>
            <p>✅ Изометрический пол (тайлы)</p>
            <p>✅ Динамическое освещение</p>
            <p>✅ Процедурная генерация</p>
            <p>🔧 Размер: 800x600px</p>
          </div>
        </div>
      </div>

      <div className={styles.footer}>
        <p>
          <strong>Status:</strong> {roomBg ? '✅ Loaded' : '⏳ Loading...'}
        </p>
        <p>
          <strong>Method:</strong> Canvas API + TypeScript
        </p>
        <p>
          <strong>Assets:</strong> 0 bytes (all code-based)
        </p>
      </div>
    </div>
  );
};
