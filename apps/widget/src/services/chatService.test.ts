import { describe, it, expect, vi } from 'vitest';
import { createChatService } from './chatService';
import { ChatConfig } from '../types';

// Mock fetch global
vi.mock('node-fetch', () => {
  return {
    default: vi.fn()
  };
});
import fetch from 'node-fetch';

const mockFetch = fetch as unknown as ReturnType<typeof vi.fn>;

describe('chatService', () => {
  const config: ChatConfig = {
    apiKey: 'k123', role: 'guest', profile: 'default', endpoint: 'http://bff.test',
  };

  it('envía POST /api/chat con headers y retorna response/correlationId', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ response: 'Hola', correlationId: 'server-corr-1' })
    } as any);

    const svc = createChatService(config);
    const result = await svc.sendMessage({ message: 'Hola' });
    expect(result.response).toBe('Hola');
    expect(result.correlationId).toBe('server-corr-1');
    expect(mockFetch).toHaveBeenCalled();
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe('http://bff.test/api/chat');
    const options = call[1];
    expect(options.headers.apiKey).toBe('k123');
    expect(options.headers.role).toBe('guest');
    expect(options.headers.profile).toBe('default');
    expect(options.headers.correlationId).toMatch(/-/);
  });

  it('usa correlationId generado si backend no lo retorna', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ response: 'Hola sin id' })
    } as any);
    const svc = createChatService(config);
    const result = await svc.sendMessage({ message: 'Hola' });
    expect(result.response).toBe('Hola sin id');
    expect(result.correlationId).toMatch(/-/);
  });

  it('lanza error si status no OK', async () => {
    mockFetch.mockResolvedValueOnce({ ok: false, status: 403 } as any);
    const svc = createChatService(config);
    await expect(svc.sendMessage({ message: 'Hola' })).rejects.toThrow(/403/);
  });
});

