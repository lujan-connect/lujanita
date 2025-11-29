export interface ChatConfig {
  apiKey: string;
  role: string;
  profile: string;
  endpoint: string;
  primaryColor?: string;
  backgroundColor?: string;
  logoUrl?: string;
}

export interface ChatMessage {
  id: string;
  role: 'user'|'assistant'|'system';
  content: string;
  timestamp: string;
  correlationId?: string;
}

export interface ChatRequestDTO {
  message: string;
  sessionId?: string;
}

export interface ChatResponseDTO {
  response: string;
  correlationId: string;
  intent?: string;
  entities?: Record<string,string>;
}
