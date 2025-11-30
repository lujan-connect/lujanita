# data-model.md - 001-front-ui

## Esquema de entidades UI

- ChatMessage: { id, text, sender, timestamp, correlationId }
- UserProfile: { apiKey, role, profile }
- WidgetConfig: { theme, language, streamingEnabled }

## Relaciones
- Un UserProfile puede tener múltiples ChatMessages
- WidgetConfig se asocia a la instancia del widget

