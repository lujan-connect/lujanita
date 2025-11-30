import React from 'react';
import { createRoot } from 'react-dom/client';
import { ChatWidget } from './ChatWidget';

const rootEl = document.getElementById('lujanita-widget-root');
if (rootEl) {
  const root = createRoot(rootEl);
  root.render(
    <ChatWidget
      config={{ apiKey: 'demo', role: 'user', profile: 'default', endpoint: 'http://localhost:9000', primaryColor: '#0066cc', backgroundColor: '#ffffff' }}
      locale="es"
    />
  );
}
