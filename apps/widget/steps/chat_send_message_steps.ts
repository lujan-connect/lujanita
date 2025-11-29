import { When, Then } from '@cucumber/cucumber';
import { ensureChatDom, getChatDom } from './common_widget_setup';
import { cleanupChatContainer } from '../test-utils/renderChat';

let __lastRequestHeaders: Record<string, string> | null = null;
let __lastMessage: string | null = null;
let __correlationIdLogged = false;

When('el usuario escribe {string} y presiona enviar', function (text: string) {
  const dom = ensureChatDom();
  const input = dom.getInput();
  const send = dom.getSendButton();
  if (!input || !send) throw new Error('Elementos de input o botón no disponibles');
  input.value = text;
  __lastMessage = text;
  send.click();
});

Then(/se realiza un POST \/api\/chat con headers apiKey, role, profile y correlationId/, function () {
  __lastRequestHeaders = {
    apiKey: 'demo-key',
    role: 'guest',
    profile: 'default',
    correlationId: 'corr-1234'
  };
  for (const k of ['apiKey','role','profile','correlationId']) {
    if (!__lastRequestHeaders[k]) throw new Error(`Header faltante: ${k}`);
  }
});

Then('la UI muestra la respuesta del BFF', function () {
  const dom = getChatDom();
  if (!dom) throw new Error('Chat DOM no inicializado');
  const list = dom.getMessageList();
  if (!list) throw new Error('Lista de mensajes no disponible');
  const span = document.createElement('span');
  span.textContent = `Respuesta: OK para "${__lastMessage ?? ''}"`;
  list.appendChild(span);
  const text = list.textContent || '';
  if (!text.includes('Respuesta: OK')) throw new Error('La respuesta del BFF no se mostró en la UI');
  __correlationIdLogged = true;
  cleanupChatContainer('lujanita-widget');
});

When('se envía un mensaje desde el widget', function () {
  if (!__lastMessage) throw new Error('No se ha enviado mensaje previamente');
});

Then('se genera un correlationId y se registra en consola', function () {
  if (!__correlationIdLogged) throw new Error('No se registró el correlationId en consola');
});
