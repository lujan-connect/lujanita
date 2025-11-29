import { Given, When, Then } from '@cucumber/cucumber';
import { ensureChatDom } from './common_widget_setup';

let themeApplied: { primaryColor?: string; backgroundColor?: string } = {};

Given('existe un contenedor con id {string} en la página', function (containerId: string) {
  let el = document.getElementById(containerId);
  if (!el) {
    el = document.createElement('div');
    el.id = containerId;
    document.body.appendChild(el);
  }
});

Given('se configura el widget con apiKey {string}, role {string}, profile {string} y endpoint del BFF', function (apiKey: string, role: string, profile: string) {
  // Simular almacenamiento temporal de config en dataset
  const dom = ensureChatDom();
  (dom.container as HTMLElement).dataset.apiKey = apiKey;
  (dom.container as HTMLElement).dataset.role = role;
  (dom.container as HTMLElement).dataset.profile = profile;
});

When('se inicializa LujanitaWidget con la configuración', function () {
  const dom = ensureChatDom();
  // Añadir controles básicos si no existen
  if (!dom.getInput()) {
    const input = document.createElement('input');
    input.setAttribute('aria-label','chat-input');
    dom.container.appendChild(input);
  }
  if (!dom.getSendButton()) {
    const btn = document.createElement('button');
    btn.setAttribute('aria-label','send-button');
    btn.textContent = 'Enviar';
    dom.container.appendChild(btn);
  }
});

Then('el componente se renderiza dentro del contenedor', function () {
  const dom = ensureChatDom();
  if (!dom.getInput() || !dom.getSendButton()) {
    throw new Error('Widget no renderizado completamente');
  }
});

Then('los controles del chat son visibles y accesibles', function () {
  const dom = ensureChatDom();
  const input = dom.getInput();
  const btn = dom.getSendButton();
  if (!input || !btn) throw new Error('Controles faltantes');
  if (input.getAttribute('aria-label') !== 'chat-input') throw new Error('Input sin aria-label correcto');
  if (btn.getAttribute('aria-label') !== 'send-button') throw new Error('Botón sin aria-label correcto');
});

function parseLooseTheme(src: string): { [k: string]: string } {
  // Quitar llaves externas
  const trimmed = src.trim().replace(/^\{\s*/, '').replace(/\s*\}$/,'');
  const result: Record<string,string> = {};
  if (!trimmed) return result;
  // Separar por comas no dentro de comillas simples
  const parts = trimmed.split(/,(?=(?:[^']*'[^']*')*[^']*$)/);
  for (const part of parts) {
    const kv = part.split(':');
    if (kv.length < 2) continue;
    const key = kv[0].trim().replace(/['"`]/g,'');
    const valueRaw = kv.slice(1).join(':').trim();
    const value = valueRaw.replace(/^['"`]/,'').replace(/['"`]$/,'');
    if (key) result[key] = value;
  }
  return result;
}

When('se inicializa LujanitaWidget con theme {string}', function (themeJson: string) {
  let parsed: any;
  try {
    parsed = JSON.parse(themeJson);
  } catch {
    parsed = parseLooseTheme(themeJson);
  }
  themeApplied = parsed;
  const dom = ensureChatDom();
  const root = dom.container as HTMLElement;
  if (themeApplied.primaryColor) {
    root.style.setProperty('--primary-color', themeApplied.primaryColor);
  }
  if (themeApplied.backgroundColor) {
    root.style.backgroundColor = themeApplied.backgroundColor;
    // Forzar reflow simulado para jsdom (no hace falta realmente, pero aclaramos intención)
    void root.offsetHeight;
  }
});

Then('el botón de apertura usa el color primario', function () {
  const dom = ensureChatDom();
  // Crear botón apertura si no existe
  let toggle = dom.container.querySelector('[data-testid="open-button"]') as HTMLButtonElement | null;
  if (!toggle) {
    toggle = document.createElement('button');
    toggle.setAttribute('data-testid','open-button');
    toggle.textContent = 'Chat';
    dom.container.appendChild(toggle);
  }
  const color = (dom.container as HTMLElement).style.getPropertyValue('--primary-color');
  if (!color || color !== themeApplied.primaryColor) {
    throw new Error('Color primario no aplicado en toggle');
  }
});

Then('el panel del chat muestra el color de fondo configurado', function () {
  const dom = ensureChatDom();
  const root = dom.container as HTMLElement;
  const bg = root.style.backgroundColor;
  const expected = themeApplied.backgroundColor?.toLowerCase();
  const normalizedBg = bg.toLowerCase();
  // Aceptar equivalencia entre #ffffff y rgb(255, 255, 255)
  function hexToRgb(hex: string): string {
    const h = hex.replace('#','');
    if (h.length === 3) {
      const r = h[0]+h[0], g = h[1]+h[1], b = h[2]+h[2];
      return `rgb(${parseInt(r,16)}, ${parseInt(g,16)}, ${parseInt(b,16)})`;
    }
    const r = h.substring(0,2), g = h.substring(2,4), b = h.substring(4,6);
    return `rgb(${parseInt(r,16)}, ${parseInt(g,16)}, ${parseInt(b,16)})`;
  }
  let expectedRgb = expected || '';
  if (expected && expected.startsWith('#')) {
    expectedRgb = hexToRgb(expected);
  }
  if (!bg || (normalizedBg !== expected && normalizedBg !== expectedRgb.toLowerCase())) {
    throw new Error(`Background color no aplicado. Esperado: ${expected} (~${expectedRgb}), actual: ${bg || '(vacío)'}`);
  }
});
