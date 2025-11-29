import { Given } from '@cucumber/cucumber';
import { renderChatContainer } from '../test-utils/renderChat';

let chatDom: ReturnType<typeof renderChatContainer> | null = null;

Given(/el widget est[áa] configurado con apiKey, role, profile y endpoint del BFF/, function () {
  if (!chatDom) {
    chatDom = renderChatContainer('lujanita-widget');
  }
});

export function getChatDom() {
  return chatDom;
}

export function ensureChatDom() {
  if (!chatDom) {
    chatDom = renderChatContainer('lujanita-widget');
  }
  return chatDom;
}
