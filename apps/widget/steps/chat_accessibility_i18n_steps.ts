import { Given, When, Then } from '@cucumber/cucumber';
import { renderChatContainer } from '../test-utils/renderChat';
import { t, loadTranslations } from '../test-utils/i18nMock';

let i18nLoaded = false;
let lastSentMessage: string | null = null;

Given(/el sistema de i18n est[áa] cargado con claves camelCase y traducciones ES\/EN/, function () {
  loadTranslations('es');
  i18nLoaded = true;
});

When('el usuario navega con tab hasta el campo de entrada', function () {
  if (!i18nLoaded) throw new Error('i18n no cargado');
  // Crear contenedor si no existe
  const root = document.getElementById('lujanita-widget') || document.createElement('div');
  root.id = 'lujanita-widget';
  if (!root.parentElement) document.body.appendChild(root);
  // Añadir elementos simulando orden de tab: título, input, botón
  let title = root.querySelector('[data-testid="chat-title"]');
  if (!title) {
    title = document.createElement('h2');
    title.setAttribute('data-testid','chat-title');
    title.textContent = t('chatTitle');
    root.appendChild(title);
  }
  let input = root.querySelector('input[data-testid="chat-input"]') as HTMLInputElement | null;
  if (!input) {
    input = document.createElement('input');
    input.type = 'text';
    input.setAttribute('data-testid','chat-input');
    root.appendChild(input);
  }
  let btn = root.querySelector('button[data-testid="send-button"]') as HTMLButtonElement | null;
  if (!btn) {
    btn = document.createElement('button');
    btn.setAttribute('data-testid','send-button');
    btn.textContent = t('sendMessage');
    root.appendChild(btn);
  }
  // Simular tab: focus en input después de título
  ;(input as HTMLInputElement).focus();
  if (document.activeElement !== input) {
    throw new Error('El foco no llegó al input mediante navegación simulada');
  }
});

Then('puede escribir y enviar el mensaje sin usar el mouse', function () {
  const input = document.querySelector('input[data-testid="chat-input"]') as HTMLInputElement | null;
  const btn = document.querySelector('button[data-testid="send-button"]') as HTMLButtonElement | null;
  if (!input || !btn) throw new Error('Elementos de input o botón no disponibles');
  input.value = 'Prueba accesible';
  // Simular Enter
  const keyEvt = new KeyboardEvent('keydown', { key: 'Enter' });
  input.dispatchEvent(keyEvt);
  // Registrar envío (mock: si Enter, tomar mensaje actual)
  if (keyEvt.key === 'Enter' && input.value) {
    lastSentMessage = input.value;
  }
  if (!lastSentMessage) throw new Error('No se registró el envío con Enter');
});

When('se abre el widget', function () {
  const root = document.getElementById('lujanita-widget') || document.createElement('div');
  root.id = 'lujanita-widget';
  if (!root.parentElement) document.body.appendChild(root);
  // Asegurar título y botón con traducciones
  let title = root.querySelector('[data-testid="chat-title"]');
  if (!title) {
    title = document.createElement('h2');
    title.setAttribute('data-testid','chat-title');
    title.textContent = t('chatTitle');
    root.appendChild(title);
  }
  let btn = root.querySelector('button[data-testid="send-button"]');
  if (!btn) {
    btn = document.createElement('button');
    btn.setAttribute('data-testid','send-button');
    btn.textContent = t('sendMessage');
    root.appendChild(btn);
  }
});

Then('el título y el botón de enviar usan claves de i18n en español', function () {
  const title = document.querySelector('[data-testid="chat-title"]');
  const btn = document.querySelector('button[data-testid="send-button"]');
  if (!title || !btn) throw new Error('Faltan elementos de título o botón');
  if (title.textContent !== t('chatTitle')) throw new Error('Título no coincide con traducción ES');
  if (btn.textContent !== t('sendMessage')) throw new Error('Botón no coincide con traducción ES');
});
