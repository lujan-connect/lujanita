// Utilitario de pruebas para mockear SSE (EventSource)
// Permite simular apertura de conexión, emisión de chunks y cierre

export type SSEChunk = { data: string };

export class MockEventSource {
  public onmessage: ((evt: MessageEvent) => void) | null = null;
  public onerror: ((evt: Event) => void) | null = null;
  public onopen: ((evt: Event) => void) | null = null;
  public readyState: number = 0; // 0: CONNECTING, 1: OPEN, 2: CLOSED

  constructor(public url: string, public options?: EventSourceInit) {}

  open() {
    this.readyState = 1;
    this.onopen?.(new Event('open'));
  }

  emitChunk(chunk: SSEChunk) {
    if (this.onmessage) {
      const evt = new MessageEvent('message', { data: chunk.data });
      this.onmessage(evt);
    }
  }

  error(errMsg = 'SSE error') {
    this.onerror?.(new Event('error'));
  }

  close() {
    this.readyState = 2;
  }
}

// Hook de ayuda para reemplazar temporalmente EventSource global en tests
export function withMockEventSource<T>(fn: (mock: typeof EventSource & { __instance?: MockEventSource }) => T): T {
  const original = (globalThis as any).EventSource;
  const mockFactory: any = function(url: string, options?: EventSourceInit) {
    const instance = new MockEventSource(url, options);
    (mockFactory as any).__instance = instance;
    // Devolver objeto compatible con EventSource (métodos y callbacks)
    return instance as unknown as EventSource;
  };
  (globalThis as any).EventSource = mockFactory;
  try {
    return fn(mockFactory);
  } finally {
    (globalThis as any).EventSource = original;
  }
}

// Ejemplo de uso en steps:
// withMockEventSource((MockES) => {
//   const es = new MockES('http://bff/sse') as any;
//   (MockES as any).__instance.open();
//   (MockES as any).__instance.emitChunk({ data: 'Hola' });
//   (MockES as any).__instance.close();
// });

