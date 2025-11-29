import React, { useState, useRef } from 'react';
import { ChatConfig, ChatMessage } from './types';
import { t } from './i18n/translations';
import { generateCorrelationId } from './utils/correlationId';
import { createChatService } from './services/chatService';

interface Props {
  config: ChatConfig;
  locale?: 'es'|'en';
}

export const ChatWidget: React.FC<Props> = ({ config, locale = 'es' }) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const chatServiceRef = useRef(createChatService(config));

  const send = async () => {
    if (!input.trim()) return;
    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: input,
      timestamp: new Date().toISOString(),
      correlationId: generateCorrelationId()
    } as any;
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    // Defer para permitir que React procese el setState antes del fetch y steps lo capturen
    setTimeout(async () => {
      try {
        const resp = await chatServiceRef.current.sendMessage({ message: userMsg.content });
        const assistantMsg: ChatMessage = {
          id: `${Date.now().toString()}-a`,
          role: 'assistant',
          content: resp.response,
          timestamp: new Date().toISOString(),
          correlationId: resp.correlationId
        } as any;
        setMessages(prev => [...prev, assistantMsg]);
      } catch (e: any) {
        const errorMsg: ChatMessage = {
          id: `${Date.now().toString()}-err`,
          role: 'system',
          content: `Error: ${e.message}`,
          timestamp: new Date().toISOString(),
          correlationId: userMsg.correlationId
        } as any;
        setMessages(prev => [...prev, errorMsg]);
      }
    }, 0);
  };

  const primary = config.primaryColor || '#0066cc';
  const bg = config.backgroundColor || '#ffffff';

  return (
    <div style={{ background: bg, border: '1px solid #ddd', width: 300, fontFamily: 'sans-serif' }}>
      <div style={{ background: primary, color: '#fff', padding: '8px' }} data-testid="chat-title">
        {t(locale, 'chatTitle')}
      </div>
      <div style={{ padding: '8px', minHeight: 120 }} data-testid="message-list">
        {messages.map(m => (
          <div key={m.id} style={{ marginBottom: 4 }} data-correlation-id={(m as any).correlationId || ''}>
            <strong>{m.role === 'user' ? 'Yo:' : 'Asistente:'}</strong> {m.content}
          </div>
        ))}
      </div>
      <div style={{ display: 'flex', gap: 4, padding: '8px' }}>
        <input
          aria-label="chat-input"
          style={{ flex: 1 }}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => { if (e.key === 'Enter') send(); }}
        />
        <button aria-label="send-button" onClick={() => send()} data-testid="send-button" style={{ background: primary, color: '#fff' }}>
          {t(locale, 'sendMessage')}
        </button>
      </div>
    </div>
  );
};
