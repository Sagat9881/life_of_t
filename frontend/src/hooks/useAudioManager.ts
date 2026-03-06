import { useCallback, useEffect, useRef } from 'react';
import {
  AudioEntry,
  getAmbientForLocation,
  getAudioById,
  getMusicForTime,
  getPreloadList,
} from '@/config/audio-manifest';

const CROSSFADE_MS = 500;
const MUSIC_CROSSFADE_MS = 1000;
const STORAGE_KEY = 'life_of_t_audio';

interface AudioState {
  enabled: boolean;
  masterVolume: number;
  ambientVolume: number;
  sfxVolume: number;
  musicVolume: number;
}

function loadSettings(): AudioState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch { /* ignore */ }
  return { enabled: false, masterVolume: 0.7, ambientVolume: 1, sfxVolume: 1, musicVolume: 1 };
}

function saveSettings(state: AudioState) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch { /* ignore */ }
}

/**
 * useAudioManager — центральный хук управления звуком.
 *
 * ## Как загружаются звуки:
 *
 * 1. OGG-файлы лежат в `frontend/public/assets/audio/{ambient,sfx,music}/`
 * 2. При `npm run build` Vite копирует `public/` → `dist/` без изменений
 * 3. В рантайме файлы доступны по `/assets/audio/...`
 * 4. Preload-звуки (UI clicks, базовые SFX) загружаются при монтировании
 * 5. Остальные — lazy load при первом запросе, кешируются в audioCache
 *
 * ## Использование:
 *
 * ```tsx
 * const audio = useAudioManager();
 *
 * // Включить звук (по клику пользователя — требование браузера)
 * audio.enable();
 *
 * // Сменить локацию — ambient переключится автоматически
 * audio.setLocation('park', 'evening');
 *
 * // Проиграть SFX
 * audio.playSfx('sfx_meow');
 *
 * // Сменить время суток — музыка переключится
 * audio.setTimeOfDay('night');
 * ```
 */
export function useAudioManager() {
  const stateRef = useRef<AudioState>(loadSettings());
  const audioCacheRef = useRef<Map<string, HTMLAudioElement>>(new Map());
  const currentAmbientRef = useRef<HTMLAudioElement | null>(null);
  const currentMusicRef = useRef<HTMLAudioElement | null>(null);
  const currentLocationRef = useRef<string>('home_room');
  const currentTimeRef = useRef<string>('day');

  // --- Internal: load or get from cache ---
  const getAudio = useCallback(async (entry: AudioEntry): Promise<HTMLAudioElement> => {
    const cached = audioCacheRef.current.get(entry.id);
    if (cached) return cached;

    return new Promise((resolve, reject) => {
      const el = new Audio(entry.file);
      el.loop = entry.loop;
      el.volume = 0;
      el.preload = 'auto';

      el.addEventListener('canplaythrough', () => {
        audioCacheRef.current.set(entry.id, el);
        resolve(el);
      }, { once: true });

      el.addEventListener('error', () => {
        // Fallback: try .mp3
        const mp3 = entry.file.replace('.ogg', '.mp3');
        const fallback = new Audio(mp3);
        fallback.loop = entry.loop;
        fallback.volume = 0;
        fallback.preload = 'auto';

        fallback.addEventListener('canplaythrough', () => {
          audioCacheRef.current.set(entry.id, fallback);
          resolve(fallback);
        }, { once: true });

        fallback.addEventListener('error', () => {
          console.warn(`[AudioManager] Failed to load: ${entry.id}`);
          reject(new Error(`Cannot load audio: ${entry.id}`));
        }, { once: true });
      }, { once: true });
    });
  }, []);

  // --- Internal: crossfade ---
  const crossfade = useCallback((
    from: HTMLAudioElement | null,
    to: HTMLAudioElement,
    targetVolume: number,
    durationMs: number,
  ) => {
    const steps = 20;
    const stepMs = durationMs / steps;
    const volStep = targetVolume / steps;

    to.volume = 0;
    to.play().catch(() => {});

    let step = 0;
    const interval = setInterval(() => {
      step++;
      to.volume = Math.min(volStep * step, targetVolume);
      if (from) {
        from.volume = Math.max(from.volume - volStep, 0);
      }
      if (step >= steps) {
        clearInterval(interval);
        if (from) {
          from.pause();
          from.currentTime = 0;
        }
      }
    }, stepMs);
  }, []);

  // --- Preload on mount ---
  useEffect(() => {
    const preloadEntries = getPreloadList();
    preloadEntries.forEach(entry => {
      const el = new Audio();
      el.src = entry.file;
      el.preload = 'auto';
      el.volume = 0;
      el.addEventListener('canplaythrough', () => {
        audioCacheRef.current.set(entry.id, el);
      }, { once: true });
    });
  }, []);

  // --- Public API ---

  const enable = useCallback(() => {
    stateRef.current.enabled = true;
    saveSettings(stateRef.current);
    // Start ambient + music for current location
    setLocation(currentLocationRef.current, currentTimeRef.current);
  }, []);

  const disable = useCallback(() => {
    stateRef.current.enabled = false;
    saveSettings(stateRef.current);
    currentAmbientRef.current?.pause();
    currentMusicRef.current?.pause();
  }, []);

  const setMasterVolume = useCallback((vol: number) => {
    stateRef.current.masterVolume = Math.max(0, Math.min(1, vol));
    saveSettings(stateRef.current);
  }, []);

  const playSfx = useCallback(async (id: string) => {
    if (!stateRef.current.enabled) return;
    const entry = getAudioById(id);
    if (!entry || entry.category !== 'sfx') return;

    try {
      const el = await getAudio(entry);
      const vol = entry.volume * stateRef.current.sfxVolume * stateRef.current.masterVolume;
      el.volume = vol;
      el.currentTime = 0;
      el.play().catch(() => {});
    } catch { /* silent fail */ }
  }, [getAudio]);

  const setLocation = useCallback(async (location: string, timeOfDay?: string) => {
    currentLocationRef.current = location;
    if (timeOfDay) currentTimeRef.current = timeOfDay;
    if (!stateRef.current.enabled) return;

    const tod = timeOfDay || currentTimeRef.current;

    // Switch ambient
    const ambEntry = getAmbientForLocation(location, tod);
    if (ambEntry) {
      try {
        const el = await getAudio(ambEntry);
        const vol = ambEntry.volume * stateRef.current.ambientVolume * stateRef.current.masterVolume;
        crossfade(currentAmbientRef.current, el, vol, CROSSFADE_MS);
        currentAmbientRef.current = el;
      } catch { /* silent */ }
    }

    // Switch music
    const musEntry = getMusicForTime(tod);
    if (musEntry) {
      try {
        const el = await getAudio(musEntry);
        const vol = musEntry.volume * stateRef.current.musicVolume * stateRef.current.masterVolume;
        crossfade(currentMusicRef.current, el, vol, MUSIC_CROSSFADE_MS);
        currentMusicRef.current = el;
      } catch { /* silent */ }
    }
  }, [getAudio, crossfade]);

  const setTimeOfDay = useCallback(async (timeOfDay: string) => {
    currentTimeRef.current = timeOfDay;
    if (!stateRef.current.enabled) return;
    await setLocation(currentLocationRef.current, timeOfDay);
  }, [setLocation]);

  const playMusic = useCallback(async (id: string) => {
    if (!stateRef.current.enabled) return;
    const entry = getAudioById(id);
    if (!entry || entry.category !== 'music') return;

    try {
      const el = await getAudio(entry);
      const vol = entry.volume * stateRef.current.musicVolume * stateRef.current.masterVolume;
      crossfade(currentMusicRef.current, el, vol, MUSIC_CROSSFADE_MS);
      currentMusicRef.current = el;
    } catch { /* silent */ }
  }, [getAudio, crossfade]);

  const stopAll = useCallback(() => {
    currentAmbientRef.current?.pause();
    currentMusicRef.current?.pause();
  }, []);

  return {
    enable,
    disable,
    playSfx,
    playMusic,
    setLocation,
    setTimeOfDay,
    setMasterVolume,
    stopAll,
    isEnabled: () => stateRef.current.enabled,
  };
}
