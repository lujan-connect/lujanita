import { JSDOM } from 'jsdom';

const dom = new JSDOM('<!doctype html><html><body></body></html>');
(globalThis as any).window = dom.window;
(globalThis as any).document = dom.window.document;
(globalThis as any).navigator = { userAgent: 'bdd-test' };

// Polyfill mínimo de KeyboardEvent si no existe en jsdom
if (typeof (globalThis as any).KeyboardEvent === 'undefined') {
  (globalThis as any).KeyboardEvent = class extends (dom.window.Event) {
    key: string;
    constructor(type: string, init: any) {
      super(type, init);
      this.key = init?.key || '';
    }
  };
}

// Hook opcional futuro: limpiar DOM After
