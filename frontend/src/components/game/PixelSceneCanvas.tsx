/**
 * PixelSceneCanvas Component
 * 
 * Renders a pixel-art game location using Canvas API.
 * 
 * Features:
 * - Multi-layer rendering (background, midground, foreground)
 * - Sprite-based furniture with click detection
 * - Character animations
 * - Time-of-day variations
 */

import React, { useRef, useEffect } from 'react';
import { useCanvasRenderer } from '../../hooks/useCanvasRenderer';
import type { LocationConfig } from '../../types/location.types';

interface PixelSceneCanvasProps {
  config: LocationConfig;
  timeOfDay: string;
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  characterAnimations?: Record<string, { animationName: string; frameIndex: number }>;
  onObjectClick: (objectId: string | null) => void;
  onObjectHover: (objectId: string | null) => void;
}

export function PixelSceneCanvas({
  config,
  timeOfDay,
  selectedObjectId,
  hoveredObjectId,
  characterAnimations,
  onObjectClick,
  onObjectHover,
}: PixelSceneCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  // Initialize renderer
  useCanvasRenderer({
    config,
    canvasRef,
    timeOfDay,
    selectedObjectId: selectedObjectId ?? null,
    hoveredObjectId: hoveredObjectId ?? null,
    characterAnimations,
  });

  // Handle clicks
  const handleCanvasClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    // Scale to internal coordinates
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    const scaledX = x * scaleX;
    const scaledY = y * scaleY;

    // Check furniture hit boxes
    const clickedFurniture = config.furniture?.find(f => {
      if (!f.x || !f.y) return false;
      const hitBox = {
        x: f.x,
        y: f.y,
        width: 32, // TODO: Get from atlas metadata
        height: 32,
      };
      return (
        scaledX >= hitBox.x &&
        scaledX <= hitBox.x + hitBox.width &&
        scaledY >= hitBox.y &&
        scaledY <= hitBox.y + hitBox.height
      );
    });

    onObjectClick(clickedFurniture?.id ?? null);
  };

  // Handle hover
  const handleCanvasMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    const scaledX = x * scaleX;
    const scaledY = y * scaleY;

    const hoveredFurniture = config.furniture?.find(f => {
      if (!f.x || !f.y) return false;
      const hitBox = {
        x: f.x,
        y: f.y,
        width: 32,
        height: 32,
      };
      return (
        scaledX >= hitBox.x &&
        scaledX <= hitBox.x + hitBox.width &&
        scaledY >= hitBox.y &&
        scaledY <= hitBox.y + hitBox.height
      );
    });

    onObjectHover(hoveredFurniture?.id ?? null);
  };

  return (
    <canvas
      ref={canvasRef}
      width={config.canvasWidth}
      height={config.canvasHeight}
      onClick={handleCanvasClick}
      onMouseMove={handleCanvasMove}
      onMouseLeave={() => onObjectHover(null)}
      style={{
        width: '100%',
        height: 'auto',
        imageRendering: 'pixelated',
        cursor: hoveredObjectId ? 'pointer' : 'default',
      }}
    />
  );
}
