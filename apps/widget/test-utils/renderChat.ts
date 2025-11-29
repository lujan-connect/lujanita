// Helper de pruebas para montar el contenedor del widget/app de chat
// Mantiene SDD: no implementa componentes reales; ofrece utilidades de DOM para steps BDD

export type RenderChatResult = {
  container: HTMLElement;
  getInput: () => HTMLInputElement | null;
  getSendButton: () => HTMLButtonElement | null;
  getMessageList: () => HTMLElement | null;
};

export function renderChatContainer(rootId = 'lujanita-widget'): RenderChatResult {
  // Crear contenedor raíz si no existe
  let container = document.getElementById(rootId);
  if (!container) {
    container = document.createElement('div');
    container.id = rootId;
    document.body.appendChild(container);
  }

  // Crear elementos mock mínimos para BDD (se sustituirán por la implementación real)
  const input = document.createElement('input');
  input.setAttribute('aria-label', 'chat-input');
  const send = document.createElement('button');
  send.textContent = 'Enviar';
  send.setAttribute('aria-label', 'send-button');
  const list = document.createElement('div');
  list.setAttribute('aria-label', 'message-list');

  container.appendChild(input);
  container.appendChild(send);
  container.appendChild(list);

  return {
    container,
    getInput: () => container!.querySelector('input[aria-label="chat-input"]'),
    getSendButton: () => container!.querySelector('button[aria-label="send-button"]'),
    getMessageList: () => container!.querySelector('div[aria-label="message-list"]'),
  };
}

// Utilidad para agregar mensajes incrementales (como chunks) al listado
export function appendChunkToList(list: HTMLElement, chunkText: string) {
  const span = document.createElement('span');
  span.textContent = chunkText;
  list.appendChild(span);
}

// Utilidad para limpiar el contenedor entre escenarios
export function cleanupChatContainer(rootId = 'lujanita-widget') {
  const container = document.getElementById(rootId);
  if (container) {
    container.remove();
  }
}

