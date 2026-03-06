/**
 * Audio Manifest — реестр всех звуков игры.
 * Пути относительно /assets/audio/ (frontend/public/assets/audio/).
 * При сборке Vite копирует public/ → dist/ автоматически.
 */

export type AudioCategory = 'ambient' | 'sfx' | 'music';

export interface AudioEntry {
  id: string;
  category: AudioCategory;
  file: string;
  volume: number;
  loop: boolean;
  /** Локации где играет (для ambient) */
  locations?: string[];
  /** Время суток (для ambient/music) */
  timeOfDay?: ('morning' | 'day' | 'evening' | 'night')[];
  /** Подгружать сразу или лениво */
  preload: boolean;
}

const BASE_PATH = '/assets/audio';

export const AUDIO_MANIFEST: AudioEntry[] = [
  // === AMBIENT ===
  { id: 'amb_home', category: 'ambient', file: `${BASE_PATH}/ambient/home.ogg`, volume: 0.3, loop: true, locations: ['home_room', 'kitchen'], timeOfDay: ['morning', 'day', 'evening'], preload: true },
  { id: 'amb_home_night', category: 'ambient', file: `${BASE_PATH}/ambient/home-night.ogg`, volume: 0.2, loop: true, locations: ['home_room', 'kitchen'], timeOfDay: ['night'], preload: false },
  { id: 'amb_workplace', category: 'ambient', file: `${BASE_PATH}/ambient/workplace.ogg`, volume: 0.25, loop: true, locations: ['workplace'], preload: false },
  { id: 'amb_gym', category: 'ambient', file: `${BASE_PATH}/ambient/gym.ogg`, volume: 0.3, loop: true, locations: ['gym'], preload: false },
  { id: 'amb_park', category: 'ambient', file: `${BASE_PATH}/ambient/park.ogg`, volume: 0.35, loop: true, locations: ['park'], timeOfDay: ['morning', 'day', 'evening'], preload: false },
  { id: 'amb_park_night', category: 'ambient', file: `${BASE_PATH}/ambient/park-night.ogg`, volume: 0.25, loop: true, locations: ['park'], timeOfDay: ['night'], preload: false },
  { id: 'amb_street', category: 'ambient', file: `${BASE_PATH}/ambient/street.ogg`, volume: 0.3, loop: true, locations: ['city_street'], preload: false },
  { id: 'amb_vet', category: 'ambient', file: `${BASE_PATH}/ambient/vet-clinic.ogg`, volume: 0.2, loop: true, locations: ['vet_clinic'], preload: false },
  { id: 'amb_dacha', category: 'ambient', file: `${BASE_PATH}/ambient/dacha.ogg`, volume: 0.35, loop: true, locations: ['dacha_yard'], preload: false },
  { id: 'amb_highway', category: 'ambient', file: `${BASE_PATH}/ambient/highway.ogg`, volume: 0.3, loop: true, locations: ['night_highway'], preload: false },
  { id: 'amb_camp', category: 'ambient', file: `${BASE_PATH}/ambient/camp.ogg`, volume: 0.35, loop: true, locations: ['summer_camp'], preload: false },

  // === SFX ===
  { id: 'sfx_purr', category: 'sfx', file: `${BASE_PATH}/sfx/purr.ogg`, volume: 0.5, loop: false, preload: true },
  { id: 'sfx_meow', category: 'sfx', file: `${BASE_PATH}/sfx/meow.ogg`, volume: 0.5, loop: false, preload: true },
  { id: 'sfx_hiss', category: 'sfx', file: `${BASE_PATH}/sfx/hiss.ogg`, volume: 0.6, loop: false, preload: true },
  { id: 'sfx_bark', category: 'sfx', file: `${BASE_PATH}/sfx/bark.ogg`, volume: 0.5, loop: false, preload: true },
  { id: 'sfx_bark_angry', category: 'sfx', file: `${BASE_PATH}/sfx/bark-angry.ogg`, volume: 0.6, loop: false, preload: false },
  { id: 'sfx_whine', category: 'sfx', file: `${BASE_PATH}/sfx/whine.ogg`, volume: 0.4, loop: false, preload: false },
  { id: 'sfx_eat_pet', category: 'sfx', file: `${BASE_PATH}/sfx/eat-pet.ogg`, volume: 0.4, loop: false, preload: true },
  { id: 'sfx_cat_fight', category: 'sfx', file: `${BASE_PATH}/sfx/cat-fight.ogg`, volume: 0.6, loop: false, preload: false },
  { id: 'sfx_cooking', category: 'sfx', file: `${BASE_PATH}/sfx/cooking.ogg`, volume: 0.4, loop: false, preload: false },
  { id: 'sfx_fridge', category: 'sfx', file: `${BASE_PATH}/sfx/fridge-open.ogg`, volume: 0.3, loop: false, preload: false },
  { id: 'sfx_kettle', category: 'sfx', file: `${BASE_PATH}/sfx/kettle.ogg`, volume: 0.3, loop: false, preload: false },
  { id: 'sfx_keyboard', category: 'sfx', file: `${BASE_PATH}/sfx/keyboard.ogg`, volume: 0.3, loop: true, preload: false },
  { id: 'sfx_phone_ring', category: 'sfx', file: `${BASE_PATH}/sfx/phone-ring.ogg`, volume: 0.5, loop: false, preload: false },
  { id: 'sfx_alarm', category: 'sfx', file: `${BASE_PATH}/sfx/alarm.ogg`, volume: 0.5, loop: false, preload: false },
  { id: 'sfx_door', category: 'sfx', file: `${BASE_PATH}/sfx/door.ogg`, volume: 0.4, loop: false, preload: false },
  { id: 'sfx_click', category: 'sfx', file: `${BASE_PATH}/sfx/ui-click.ogg`, volume: 0.3, loop: false, preload: true },
  { id: 'sfx_success', category: 'sfx', file: `${BASE_PATH}/sfx/ui-success.ogg`, volume: 0.4, loop: false, preload: true },
  { id: 'sfx_fail', category: 'sfx', file: `${BASE_PATH}/sfx/ui-fail.ogg`, volume: 0.4, loop: false, preload: false },
  { id: 'sfx_notification', category: 'sfx', file: `${BASE_PATH}/sfx/ui-notification.ogg`, volume: 0.3, loop: false, preload: true },
  { id: 'sfx_quest_start', category: 'sfx', file: `${BASE_PATH}/sfx/quest-start.ogg`, volume: 0.5, loop: false, preload: false },
  { id: 'sfx_level_up', category: 'sfx', file: `${BASE_PATH}/sfx/level-up.ogg`, volume: 0.5, loop: false, preload: false },
  { id: 'sfx_heartbeat', category: 'sfx', file: `${BASE_PATH}/sfx/heartbeat.ogg`, volume: 0.4, loop: true, preload: false },
  { id: 'sfx_sigh', category: 'sfx', file: `${BASE_PATH}/sfx/sigh.ogg`, volume: 0.3, loop: false, preload: false },

  // === MUSIC ===
  { id: 'mus_morning', category: 'music', file: `${BASE_PATH}/music/morning.ogg`, volume: 0.2, loop: true, timeOfDay: ['morning'], preload: false },
  { id: 'mus_day', category: 'music', file: `${BASE_PATH}/music/day.ogg`, volume: 0.15, loop: true, timeOfDay: ['day'], preload: false },
  { id: 'mus_evening', category: 'music', file: `${BASE_PATH}/music/evening.ogg`, volume: 0.2, loop: true, timeOfDay: ['evening'], preload: false },
  { id: 'mus_night', category: 'music', file: `${BASE_PATH}/music/night.ogg`, volume: 0.15, loop: true, timeOfDay: ['night'], preload: false },
  { id: 'mus_tension', category: 'music', file: `${BASE_PATH}/music/tension.ogg`, volume: 0.25, loop: true, preload: false },
  { id: 'mus_ending_happy', category: 'music', file: `${BASE_PATH}/music/ending-happy.ogg`, volume: 0.3, loop: false, preload: false },
  { id: 'mus_ending_sad', category: 'music', file: `${BASE_PATH}/music/ending-sad.ogg`, volume: 0.25, loop: false, preload: false },
];

export function getAudioById(id: string): AudioEntry | undefined {
  return AUDIO_MANIFEST.find(a => a.id === id);
}

export function getAmbientForLocation(location: string, timeOfDay: string): AudioEntry | undefined {
  return AUDIO_MANIFEST.find(
    a => a.category === 'ambient'
      && a.locations?.includes(location)
      && (!a.timeOfDay || a.timeOfDay.includes(timeOfDay as any))
  );
}

export function getMusicForTime(timeOfDay: string): AudioEntry | undefined {
  return AUDIO_MANIFEST.find(
    a => a.category === 'music'
      && a.timeOfDay?.includes(timeOfDay as any)
  );
}

export function getPreloadList(): AudioEntry[] {
  return AUDIO_MANIFEST.filter(a => a.preload);
}
