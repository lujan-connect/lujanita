# MCP Connection Troubleshooting

## Overview

This document describes the connection timeout and premature close issues that can occur when the BFF (Backend for Frontend) communicates with the Odoo MCP server, and the solutions implemented to address them.

## Problem Description

### Symptoms

When the BFF attempts to connect to the Odoo MCP server at `https://odoo.dev.expresolujan.com/mcp`, the following errors may occur:

1. **PrematureCloseException**: Connection prematurely closed BEFORE response
   ```
   reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response
   ```

2. **Connection Timeout**: Server fails to respond within the configured timeout
   ```
   I/O error on POST request for "https://odoo.dev.expresolujan.com/mcp": 
   odoo.dev.expresolujan.com:443 failed to respond
   ```

### Root Causes

1. **Missing Timeout Configuration**: The WebClient and RestTemplate were not properly configured with connection timeouts, response timeouts, or socket timeouts
2. **Inadequate Retry Strategy**: The retry mechanism only handled `PrematureCloseException` and used very short delays (300ms)
3. **Connection Pool Issues**: No connection pooling or connection lifecycle management
4. **Short Timeout Values**: The default 10-second timeout was insufficient for slow Odoo server responses

## Solutions Implemented

### 1. Comprehensive Timeout Configuration

**File**: `apps/bff/src/main/java/com/lujanita/bff/config/HttpClientConfig.java`

#### WebClient Configuration

- **Connection Timeout**: `ChannelOption.CONNECT_TIMEOUT_MILLIS` set to `timeoutMs` from properties
- **Response Timeout**: Set via `responseTimeout(Duration.ofMillis(timeoutMs))`
- **Read Timeout**: Added `ReadTimeoutHandler` to the Netty pipeline
- **Write Timeout**: Added `WriteTimeoutHandler` to the Netty pipeline
- **Connection Provider**: Configured with:
  - Max 100 connections
  - Max idle time: 30 seconds
  - Max lifetime: 5 minutes
  - Pending acquire timeout: Based on `timeoutMs`
  - Background eviction: Every 120 seconds

```java
ConnectionProvider connectionProvider = ConnectionProvider.builder("mcp-connection-pool")
    .maxConnections(100)
    .maxIdleTime(Duration.ofSeconds(30))
    .maxLifeTime(Duration.ofMinutes(5))
    .pendingAcquireTimeout(Duration.ofMillis(timeoutMs))
    .evictInBackground(Duration.ofSeconds(120))
    .build();

HttpClient httpClient = HttpClient.create(connectionProvider)
    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
    .responseTimeout(Duration.ofMillis(timeoutMs))
    .doOnConnected(conn -> conn
        .addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
        .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)));
```

#### RestTemplate Configuration

- **Connection Request Timeout**: `RequestConfig.setConnectionRequestTimeout()`
- **Response Timeout**: `RequestConfig.setResponseTimeout()`
- **Socket Timeout**: `SocketConfig.setSoTimeout()`
- **Connection Pool**: Max 100 total connections, 20 per route
- **Connection Eviction**: Evicts expired and idle (30s) connections

```java
RequestConfig requestConfig = RequestConfig.custom()
    .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeoutMs))
    .setResponseTimeout(Timeout.ofMilliseconds(timeoutMs))
    .build();

SocketConfig socketConfig = SocketConfig.custom()
    .setSoTimeout(Timeout.ofMilliseconds(timeoutMs))
    .build();

var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
    .setDefaultSocketConfig(socketConfig)
    .setMaxConnTotal(100)
    .setMaxConnPerRoute(20)
    .build();
```

### 2. Improved Retry Strategy

**File**: `apps/bff/src/main/java/com/lujanita/bff/mcp/McpClientWebClientService.java`

#### Changes

- **Retry Count**: Reduced from 3 to 2 retries (total 3 attempts including the initial request)
- **Retry Delay**: Increased from 300ms to 1 second
- **Exception Handling**: Now retries on:
  - `PrematureCloseException`
  - `ReadTimeoutException`
  - `WriteTimeoutException`
  - `IOException` with messages containing "Connection reset", "Broken pipe", or "Connection refused"
- **Logging**: Added before-retry logging to track retry attempts

```java
.retryWhen(
    Retry.fixedDelay(2, Duration.ofSeconds(1))
        .filter(e -> isRetryableException(e))
        .doBeforeRetry(signal -> log.warn("[MCP][WebClient] Reintentando tras error (intento {}): {}", 
                signal.totalRetries() + 1, signal.failure().getMessage()))
        .onRetryExhaustedThrow((spec, signal) -> signal.failure())
)
```

#### Retryable Exception Detection

```java
private boolean isRetryableException(Throwable e) {
    if (e == null) return false;
    
    Throwable current = e;
    while (current != null) {
        // Retry on connection issues
        if (current instanceof reactor.netty.http.client.PrematureCloseException) {
            return true;
        }
        // Retry on timeout exceptions
        if (current instanceof io.netty.handler.timeout.ReadTimeoutException) {
            return true;
        }
        if (current instanceof io.netty.handler.timeout.WriteTimeoutException) {
            return true;
        }
        // Retry on connection reset
        if (current instanceof java.io.IOException) {
            String msg = current.getMessage();
            if (msg != null && (msg.contains("Connection reset") || 
                               msg.contains("Broken pipe") ||
                               msg.contains("Connection refused"))) {
                return true;
            }
        }
        current = current.getCause();
    }
    return false;
}
```

### 3. Increased Timeout Values

**File**: `apps/bff/src/main/resources/application.yml`

Changed the MCP timeout from 10 seconds to 30 seconds to accommodate slower Odoo server responses:

```yaml
bff:
  mcp:
    endpoint: https://odoo.dev.expresolujan.com/mcp
    timeoutMs: 30000  # Changed from 10000
    enabled: true
```

## Configuration Properties

The following properties control MCP connection behavior:

- `bff.mcp.timeoutMs`: Overall timeout for MCP requests (default: 30000ms)
- `bff.mcp.insecureSkipTlsVerify`: Whether to skip TLS verification (default: true for dev)
- `bff.mcp.endpoint`: The Odoo MCP server endpoint

## Monitoring and Debugging

### Logs to Monitor

Look for these log messages to understand connection behavior:

```
[MCP][WebClient] Llamando MCP endpoint=... method=...
[MCP][WebClient] Reintentando tras error (intento 1): ...
[MCP][WebClient] Respuesta HTTP body=...
[MCP][WebClient] Error llamando MCP: ...
[MCP][RestTemplate-fallback] Respuesta HTTP body=...
```

### Metrics

The application exposes Prometheus metrics for MCP calls via the `/actuator/prometheus` endpoint:

- `mcp_call` - Duration of MCP calls (tagged by method and response code)
- `mcp_tools_list` - Duration of MCP tooling list calls

## Common Issues and Solutions

### Issue 1: Connection Still Timing Out

**Symptoms**: Even with the increased timeout, connections still fail

**Solutions**:
1. Check network connectivity to `odoo.dev.expresolujan.com:443`
2. Verify the Odoo MCP server is running and healthy
3. Check if there's a firewall or proxy blocking the connection
4. Verify the API key is valid: `AUTH_TOKEN` or `TEST_API_KEY`

### Issue 2: Too Many Retries

**Symptoms**: Excessive retry attempts causing delays

**Solutions**:
1. Reduce retry count in `McpClientWebClientService.callMcp()`
2. Increase retry delay to give the server more time to recover
3. Check if the Odoo server is consistently slow and consider scaling it

### Issue 3: Connection Pool Exhaustion

**Symptoms**: "Pending acquire timeout" errors

**Solutions**:
1. Increase `maxConnections` in the ConnectionProvider
2. Decrease `maxIdleTime` to release connections faster
3. Check for connection leaks in the code

## Testing

To test the connection configuration locally:

```bash
# Start the BFF with debug logging
cd apps/bff
mvn spring-boot:run -Dlogging.level.com.lujanita.bff=DEBUG

# Make a test request
curl -X POST http://localhost:9000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "test mcp connection"}'
```

## References

- [Spec 004: Odoo MCP Client](../../specs/004-odoo-mcp-client/spec.md)
- [MCP Contracts](.github/copilot-knowledge/contracts-mcp.md)
- [Reactor Netty Documentation](https://projectreactor.io/docs/netty/release/reference/index.html)
- [Apache HttpClient 5 Documentation](https://hc.apache.org/httpcomponents-client-5.2.x/)
