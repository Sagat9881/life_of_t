/**
 * RoomSceneModel - Scene configuration based on RoomPage.xml
 * This is a static model that represents the room layout without image dependencies
 */

import { SceneModel } from '../types/SceneModel';

export const roomSceneModel: SceneModel = {
  id: 'room',
  name: 'Комната Татьяны',
  type: 'room',
  layers: [
    { name: 'background', zIndex: 0, type: 'Static' },
    { name: 'room-back-wall', zIndex: 1, type: 'Isometric' },
    { name: 'room-floor', zIndex: 2, type: 'Isometric' },
    { name: 'interactive-objects', zIndex: 3, type: 'Clickable' },
    { name: 'character', zIndex: 4, type: 'Isometric' },
    { name: 'npcs', zIndex: 5, type: 'Isometric' },
  ],
  objects: [
    {
      id: 'bed',
      type: 'furniture',
      category: 'rest',
      position: { x: 200, y: 150, zIndex: 3 },
      sprite: { size: { width: 128, height: 128 } },
      states: [
        { name: 'neat', default: true },
        { name: 'messy' },
        { name: 'occupied' },
      ],
      actions: [
        {
          id: 'sleep',
          label: 'Спать',
          icon: 'moon',
          requirements: {
            time: { between: '20:00-09:00' },
            energy: { min: 0 },
          },
          effects: {
            energy: { restore: 100 },
            time: { advance: '8h' },
            happiness: { change: 10 },
          },
        },
      ],
      highlight: {
        type: 'glow',
        color: 'var(--color-primary)',
        opacity: 0.5,
      },
    },
    {
      id: 'computer',
      type: 'work-station',
      category: 'work',
      position: { x: 800, y: 200, zIndex: 3 },
      sprite: { size: { width: 128, height: 128 } },
      states: [
        { name: 'off' },
        { name: 'idle' },
        { name: 'working' },
      ],
      actions: [
        {
          id: 'work',
          label: 'Работать',
          icon: 'briefcase',
          requirements: {
            energy: { min: 20 },
            time: { between: '09:00-22:00' },
          },
          effects: {
            energy: { cost: 20 },
            time: { advance: '2h' },
            money: { reward: 100 },
          },
        },
      ],
    },
    {
      id: 'phone',
      type: 'communication',
      category: 'social',
      position: { x: 700, y: 180, zIndex: 3 },
      sprite: { size: { width: 64, height: 64 } },
      states: [{ name: 'idle' }, { name: 'notification' }, { name: 'active' }],
      actions: [
        {
          id: 'call-husband',
          label: 'Позвонить мужу',
          icon: 'phone',
          effects: {
            energy: { cost: 5 },
            time: { advance: '30m' },
            relationship: { target: 'husband', change: 5 },
            happiness: { change: 10 },
          },
        },
      ],
    },
  ],
  npcs: [
    {
      id: 'sam',
      type: 'dog',
      position: { x: 400, y: 300, zIndex: 5 },
      sprite: { size: { width: 48, height: 48 } },
      behavior: {
        type: 'wander',
        bounds: 'room',
        speed: 1,
        favoriteSpots: [{ near: 'bed' }, { near: 'door' }],
      },
      interaction: {
        tap: true,
        action: {
          id: 'pet',
          label: 'Погладить',
          effects: {
            happiness: { change: 10 },
          },
        },
      },
    },
  ],
  atmosphere: {
    timeOfDay: 'day',
    lighting: { bright: true },
    colors: {
      background: 'linear-gradient(180deg, #E0C3FC 0%, #8EC5FC 100%)',
    },
  },
};