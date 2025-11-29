import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createChatService } from './chatService';
import { ChatConfig } from '../types';

let fetchSpy: any;

beforeEach(() => {
  fetchSpy = vi.fn();
  (globalThis as any).fetch = fetchSpy;
});

describe('chatService', () => {
  const config: ChatConfig = {
    apiKey: 'k123', role: 'guest', profile: 'default', endpoint: 'http://bff.test',
  } as any;

  it('envía POST /api/chat con headers y retorna response/correlationId', async () => {
    fetchSpy.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ response: 'Hola', correlationId: 'server-corr-1' })
    });
    const svc = createChatService(config);
    const result = await svc.sendMessage({ message: 'Hola' });
    expect(result.response).toBe('Hola');
    expect(result.correlationId).toBe('server-corr-1');
    expect(fetchSpy).toHaveBeenCalledTimes(1);
    const [url, options] = fetchSpy.mock.calls[0];
    expect(url).toBe('http://bff.test/api/chat');
    expect(options.headers.apiKey).toBe('k123');
    expect(options.headers.role).toBe('guest');
    expect(options.headers.profile).toBe('default');
    expect(options.headers.correlationId).toMatch(/-/);
  });

  it('usa correlationId generado si backend no lo retorna', async () => {
    fetchSpy.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ response: 'Hola sin id' })
    });
    const svc = createChatService(config);
    const result = await svc.sendMessage({ message: 'Hola' });
    expect(result.response).toBe('Hola sin id');
    expect(result.correlationId).toMatch(/-/);
  });

  it('lanza error si status no OK', async () => {
    fetchSpy.mockResolvedValueOnce({ ok: false, status: 403 });
    const svc = createChatService(config);
    await expect(svc.sendMessage({ message: 'Hola' })).rejects.toThrow(/403/);
  });
});
