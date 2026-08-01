import { useState, useEffect, useCallback } from 'react';

// Cycle order: light → dark → system → light ...
const THEMES = ['light', 'dark', 'system'];

function getSystemPreference() {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function applyTheme(mode) {
  const effective = mode === 'system' ? getSystemPreference() : mode;
  document.documentElement.setAttribute('data-theme', effective);
}

export function useTheme() {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'system';
  });

  useEffect(() => {
    applyTheme(theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  // Listen for OS-level preference changes when in system mode
  useEffect(() => {
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = () => {
      if (theme === 'system') applyTheme('system');
    };
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, [theme]);

  const cycleTheme = useCallback(() => {
    setTheme((prev) => {
      const idx = THEMES.indexOf(prev);
      return THEMES[(idx + 1) % THEMES.length];
    });
  }, []);

  return { theme, cycleTheme };
}
