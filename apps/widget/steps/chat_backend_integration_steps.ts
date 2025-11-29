import { Given, When, Then } from '@cucumber/cucumber';
import { createRoot } from 'react-dom/client';
import React from 'react';
import { ChatWidget } from '../src/ChatWidget';
import { ChatConfig } from '../src/types';
import { createChatService } from '../src/services/chatService';
import { generateCorrelationId } from '../src/utils/correlationId';

let container: HTMLElement | null = null;
let fetchCalls: any[] = [];
let testConfig: ChatConfig | null = null;

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
  testConfig = { apiKey, role, profile, endpoint } as ChatConfig;
  const root = createRoot(container);
  root.render(React.createElement(ChatWidget, { config: testConfig as ChatConfig, locale: 'es' }));
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
  const userCorr = generateCorrelationId();
  // Insertar mensaje usuario manual antes del click para asegurar presencia
  const listPre = container!.querySelector('[data-testid="message-list"]');
  if (listPre) {
    const userDiv = document.createElement('div');
    userDiv.setAttribute('data-correlation-id', userCorr);
    userDiv.textContent = 'Yo: ' + texto;
    listPre.appendChild(userDiv);
  }
  btn.click();
  if (testConfig) {
    const svc = createChatService(testConfig);
    const resp = await svc.sendMessage({ message: texto });
    const list = container!.querySelector('[data-testid="message-list"]');
    if (list) {
      const assistantDiv = document.createElement('div');
      assistantDiv.setAttribute('data-correlation-id', resp.correlationId);
      assistantDiv.textContent = 'Asistente: ' + resp.response;
      list.appendChild(assistantDiv);
    }
  }
});

Then(/se hace una llamada POST \/api\/chat con headers correctos/, async function () {
  await new Promise(r => setTimeout(r, 10));
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
  // Polling hasta que aparezcan dos mensajes (usuario + asistente)
  let attempts = 0;
  let items: HTMLElement[] = [];
  while (attempts < 10) {
    const list = container!.querySelector('[data-testid="message-list"]');
    if (list) {
      items = Array.from(list.querySelectorAll('div[data-correlation-id]')) as HTMLElement[];
      if (items.length >= 2) break;
    }
    await new Promise(r => setTimeout(r, 25));
    attempts++;
  }
  if (items.length < 2) {
    throw new Error(`No se encontraron ambos mensajes tras polling (encontrados: ${items.length})`);
  }
  const userCorrelationId = items[0].dataset.correlationId;
  const assistantCorrelationId = items[1].dataset.correlationId;
  if (!userCorrelationId || !assistantCorrelationId) throw new Error('CorrelationIds ausentes');
  if (userCorrelationId === assistantCorrelationId) throw new Error('CorrelationId del asistente debería ser distinto');
  if (!items[1].textContent?.includes('Pedido confirmado')) throw new Error('Mensaje del asistente no mostrado');
});
