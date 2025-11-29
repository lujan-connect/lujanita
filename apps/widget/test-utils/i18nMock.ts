// Mock simple de i18n para pruebas BDD
export type Translations = Record<string, string>;

const es: Translations = {
  chatTitle: 'Chat Lujanita',
  sendMessage: 'Enviar',
};

const en: Translations = {
  chatTitle: 'Lujanita Chat',
  sendMessage: 'Send',
};

let current: Translations = es;

export function loadTranslations(locale: 'es'|'en' = 'es') {
  current = locale === 'es' ? es : en;
}

export function t(key: string): string {
  return current[key] || key;
}

