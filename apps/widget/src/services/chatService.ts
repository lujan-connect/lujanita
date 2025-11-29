import fetchImport from 'node-fetch';
import { ChatConfig, ChatRequestDTO, ChatResponseDTO } from '../types';
import { generateCorrelationId } from '../utils/correlationId';

const runtimeFetch: typeof fetchImport = (globalThis as any).fetch || (fetchImport as any);

export interface ChatService {
  sendMessage(req: ChatRequestDTO): Promise<ChatResponseDTO>;
}

export function createChatService(config: ChatConfig): ChatService {
  const baseUrl = config.endpoint.replace(/\/$/, '');
  return {
    async sendMessage(req: ChatRequestDTO): Promise<ChatResponseDTO> {
      const correlationId = generateCorrelationId();
      const res = await runtimeFetch(`${baseUrl}/api/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'apiKey': config.apiKey,
          'role': config.role,
          'profile': config.profile,
          'correlationId': correlationId
        },
        body: JSON.stringify({ message: req.message, sessionId: req.sessionId })
      });
      if (!res.ok) {
        throw new Error(`ChatServiceError ${res.status}`);
      }
      const data = await (res as any).json() as ChatResponseDTO;
      return { ...data, correlationId: data.correlationId || correlationId };
    }
  };
}
