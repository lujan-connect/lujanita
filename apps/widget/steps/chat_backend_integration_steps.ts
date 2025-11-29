import { Given, When, Then } from '@cucumber/cucumber';
import { createRoot } from 'react-dom/client';
import React from 'react';
import { ChatWidget } from '../src/ChatWidget';
import { ChatConfig } from '../src/types';

let container: HTMLElement | null = null;
let fetchCalls: any[] = [];

function mockFetch(url: string, options: any) {
  fetchCalls.push([url, options]);
  return Promise.resolve({
    ok: true,
    json: async () => ({ response: 'Pedido confirmado', correlationId: 'backend-corr-123' })
  });
}

Given('el widget está configurado con apiKey {string}, role {string}, profile {string} y endpoint {string}', function (apiKey: string, role: string, profile: string, endpoint: string) {
  container = document.createElement('div');
  container.id = 'test-root';
  document.body.appendChild(container);
  fetchCalls = [];
  (globalThis as any).fetch = mockFetch as any;
  const config: ChatConfig = { apiKey, role, profile, endpoint } as any;
  const root = createRoot(container);
  root.render(React.createElement(ChatWidget, { config, locale: 'es' }));
});

When('el usuario escribe {string} y presiona enviar en el widget React', async function (texto: string) {
  if (!container) throw new Error('Contenedor no inicializado');
  const getControls = () => {
    const input = container!.querySelector('input[aria-label="chat-input"]') as HTMLInputElement | null;
    const btn = container!.querySelector('button[aria-label="send-button"]') as HTMLButtonElement | null;
    return { input, btn };
  };
  let attempts = 0;
  let ctrl = getControls();
  while ((!ctrl.input || !ctrl.btn) && attempts < 5) {
    await new Promise(r => setTimeout(r, 5));
    ctrl = getControls();
    attempts++;
  }
  const { input, btn } = ctrl;
  if (!input || !btn) throw new Error('Controles no encontrados');
  input.value = texto;
  // Nota: En jsdom el dispatch de Event('input') puede fallar; React ya leerá value al click
  // Si se requiere simular onChange en futuras pruebas, usar window.Event('input') condicional.
  btn.click();
});

Then(/se hace una llamada POST \/api\/chat con headers correctos/, async function () {
  // Esperar microtask + fetch
  await Promise.resolve();
  if (fetchCalls.length === 0) {
    throw new Error('No se realizó la llamada fetch');
  }
  const [url, opts] = fetchCalls[0];
  if (!url.endsWith('/api/chat')) throw new Error('URL incorrecta');
  for (const h of ['apiKey','role','profile','correlationId']) {
    if (!opts.headers[h]) throw new Error(`Header faltante: ${h}`);
  }
});

Then('la UI muestra la respuesta del asistente con correlationId distinto al del usuario', async function () {
  // Esperar a que se añada el mensaje asistente
  await new Promise(r => setTimeout(r, 10));
  const list = container!.querySelector('[data-testid="message-list"]');
  if (!list) throw new Error('Lista de mensajes no encontrada');
  const items = Array.from(list.querySelectorAll('div[data-correlation-id]')) as HTMLElement[];
  if (items.length < 2) throw new Error('No se encontraron ambos mensajes (usuario y asistente)');
  const userCorrelationId = items[0].dataset.correlationId;
  const assistantCorrelationId = items[1].dataset.correlationId;
  if (!userCorrelationId || !assistantCorrelationId) throw new Error('CorrelationIds ausentes');
  if (userCorrelationId === assistantCorrelationId) throw new Error('CorrelationId del asistente debería ser distinto');
  if (!items[1].textContent?.includes('Pedido confirmado')) throw new Error('Mensaje del asistente no mostrado');
});
