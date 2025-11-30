export const translations = {
  es: {
    chatTitle: 'Chat Lujanita',
    sendMessage: 'Enviar',
    typeMessage: 'Escribe un mensaje...'
  },
  en: {
    chatTitle: 'Lujanita Chat',
    sendMessage: 'Send',
    typeMessage: 'Type a message...'
  }
};

export type Locale = 'es'|'en';

export function t(locale: Locale, key: string): string {
  return (translations as any)[locale]?.[key] || key;
}
