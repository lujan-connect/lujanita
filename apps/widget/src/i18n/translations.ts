export const translations = {
  es: {
    chatTitle: 'Chat Lujanita',
    sendMessage: 'Enviar'
  },
  en: {
    chatTitle: 'Lujanita Chat',
    sendMessage: 'Send'
  }
};

export type Locale = 'es'|'en';

export function t(locale: Locale, key: string): string {
  return (translations as any)[locale]?.[key] || key;
}

