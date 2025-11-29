// Utilitario de pruebas para mockear WebSocket en tests del widget
// Simula apertura, recepción de mensajes, errores y cierre

export type WSMessage = { data: string };

export class MockWebSocket {
  public onmessage: ((evt: MessageEvent) => void) | null = null;
  public onerror: ((evt: Event) => void) | null = null;
  public onopen: ((evt: Event) => void) | null = null;
  public onclose: ((evt: CloseEvent) => void) | null = null;
  public readyState: number = 0; // 0 CONNECTING, 1 OPEN, 2 CLOSING, 3 CLOSED

  constructor(public url: string, public protocols?: string | string[]) {}

  open() {
    this.readyState = 1;
    this.onopen?.(new Event('open'));
  }

  send(_data: string) {
    // En tests no se envía realmente; se puede interceptar si hace falta
  }

  emitMessage(msg: WSMessage) {
    if (this.onmessage) {
      const evt = new MessageEvent('message', { data: msg.data });
      this.onmessage(evt);
    }
  }

  error() {
    this.onerror?.(new Event('error'));
  }

  close(code: number = 1000, reason: string = 'normal closure') {
    this.readyState = 3;
    this.onclose?.(new CloseEvent('close', { code, reason }));
  }
}

// Reemplaza temporalmente WebSocket global durante un test y expone la instancia
export function withMockWebSocket<T>(fn: (WS: typeof WebSocket & { __instance?: MockWebSocket }) => T): T {
  const original = (globalThis as any).WebSocket;
  const MockWS: any = function(url: string, protocols?: string | string[]) {
    const instance = new MockWebSocket(url, protocols);
    (MockWS as any).__instance = instance;
    return instance as unknown as WebSocket;
  };
  (globalThis as any).WebSocket = MockWS;
  try {
    return fn(MockWS);
  } finally {
    (globalThis as any).WebSocket = original;
  }
}

// Ejemplo de uso:
// withMockWebSocket((WS) => {
//   const ws = new WS('ws://bff/chat') as any;
//   (WS as any).__instance.open();
//   (WS as any).__instance.emitMessage({ data: 'chunk1' });
//   (WS as any).__instance.close();
// });

