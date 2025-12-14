import React, { useState, useRef, useEffect } from 'react';
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
  const [history, setHistory] = useState<string[]>(() => {
    try {
      const raw = localStorage.getItem('lujanita.chat.history');
      if (!raw) return [];
      const arr = JSON.parse(raw);
      return Array.isArray(arr) ? arr.filter(x => typeof x === 'string') : [];
    } catch { return []; }
  });
  const [historyIndex, setHistoryIndex] = useState<number>(-1);
  const chatServiceRef = useRef(createChatService(config));
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  // Mostrar welcome message al inicializar
  useEffect(() => {
    const botName = config.chatbotName || 'Lujanita';
    const welcome = config.welcomeMessage ? config.welcomeMessage.replace('%s', botName) : `Hola — soy ${botName}, asistente de Expreso Luján de Cuyo. ¿En qué te puedo ayudar?`;
    const welcomeMsg: ChatMessage = {
      id: `welcome-${Date.now()}`,
      role: 'assistant',
      content: welcome,
      timestamp: new Date().toISOString()
    } as any;
    setMessages(prev => [welcomeMsg, ...prev]);
  }, [config.chatbotName, config.welcomeMessage]);

  const send = async () => {
    if (!input.trim()) return;
    // update history
    setHistory(prev => {
      const next = [...prev, input];
      const bounded = next.slice(-100);
      try { localStorage.setItem('lujanita.chat.history', JSON.stringify(bounded)); } catch {}
      return bounded;
    });
    setHistoryIndex(-1);
    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: input,
      timestamp: new Date().toISOString(),
      correlationId: generateCorrelationId()
    } as any;
    setMessages(prev => [...prev, userMsg]);
    setInput('');
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
  };

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const primary = config.primaryColor || '#0066cc';
  const bg = config.backgroundColor || '#ffffff';

  return (
    <div style={{ background: bg, border: '1px solid #ddd', width: 400, height: 600, fontFamily: 'sans-serif', display: 'flex', flexDirection: 'column' }}>
      <div style={{ background: primary, color: '#fff', padding: '12px', fontWeight: 'bold' }} data-testid="chat-title">
        {t(locale, 'chatTitle')} - Lujanita
      </div>
      <div style={{ flex: 1, padding: '12px', overflowY: 'auto', background: '#f0f0f0' }} data-testid="message-list">
        {messages.map(m => (
          <div key={m.id} style={{
            marginBottom: 8,
            display: 'flex',
            justifyContent: m.role === 'user' ? 'flex-end' : 'flex-start'
          }} data-correlation-id={(m as any).correlationId || ''}>
            <div style={{
              maxWidth: '70%',
              padding: '8px 12px',
              borderRadius: 18,
              background: m.role === 'user' ? primary : '#fff',
              color: m.role === 'user' ? '#fff' : '#000',
              border: m.role === 'assistant' ? '1px solid #ddd' : 'none',
              wordWrap: 'break-word'
            }}>
              {m.content}
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      <div style={{ display: 'flex', gap: 8, padding: '12px', background: '#fff', borderTop: '1px solid #ddd' }}>
        <input
          aria-label="chat-input"
          placeholder={t(locale, 'typeMessage')}
          style={{
            flex: 1,
            padding: '10px 12px',
            border: '1px solid #ddd',
            borderRadius: 20,
            outline: 'none',
            fontSize: '14px'
          }}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => {
            if (e.key === 'Enter') {
              send();
            } else if (e.key === 'ArrowUp') {
              // navigate history backwards
              e.preventDefault();
              if (history.length === 0) return;
              const nextIndex = historyIndex < 0 ? history.length - 1 : Math.max(0, historyIndex - 1);
              setHistoryIndex(nextIndex);
              setInput(history[nextIndex] || '');
            } else if (e.key === 'ArrowDown') {
              // navigate history forwards
              e.preventDefault();
              if (history.length === 0) return;
              if (historyIndex < 0) return; // nothing to go forward from
              const nextIndex = historyIndex + 1;
              if (nextIndex >= history.length) {
                setHistoryIndex(-1);
                setInput('');
              } else {
                setHistoryIndex(nextIndex);
                setInput(history[nextIndex] || '');
              }
            }
          }}
        />
        <button
          aria-label="send-button"
          onClick={() => send()}
          data-testid="send-button"
          style={{
            padding: '10px 16px',
            background: primary,
            color: '#fff',
            border: 'none',
            borderRadius: 20,
            cursor: 'pointer',
            fontSize: '14px'
          }}
          disabled={!input.trim()}
        >
          {t(locale, 'sendMessage')}
        </button>
      </div>
    </div>
  );
};
