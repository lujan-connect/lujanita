import { ChatConfig, ChatRequestDTO, ChatResponseDTO } from '../types';
import { generateCorrelationId } from '../utils/correlationId';

export interface ChatService {
  sendMessage(req: ChatRequestDTO): Promise<ChatResponseDTO>;
}

export function createChatService(config: ChatConfig): ChatService {
  const baseUrl = config.endpoint.replace(/\/$/, '');
  return {
    async sendMessage(req: ChatRequestDTO): Promise<ChatResponseDTO> {
      // Usar fetch nativo del navegador
      const correlationId = generateCorrelationId();
      const res = await fetch(`${baseUrl}/api/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Api-Key': config.apiKey,
          'X-Role': config.role,
          'X-Profile': config.profile,
          'X-Correlation-Id': correlationId
        },
        body: JSON.stringify({ message: req.message, sessionId: req.sessionId })
      });
      if (!res.ok) {
        throw new Error(`ChatServiceError ${res.status}`);
      }
      const data = await res.json() as ChatResponseDTO;
      return { ...data, correlationId: data.correlationId || correlationId };
    }
  };
}
