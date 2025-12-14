package com.lujanita.bff.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.time.Duration;

@Slf4j
@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate(BffProperties properties) {
        if (properties.getMcp().isInsecureSkipTlsVerify()) {
            try {
                TrustStrategy trustAllStrategy = (chain, authType) -> true;
                SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, trustAllStrategy)
                    .build();

                var sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(sslContext)
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build();

                CloseableHttpClient httpClient = HttpClients.custom()
                        .setConnectionManager(
                                PoolingHttpClientConnectionManagerBuilder.create()
                                        .setSSLSocketFactory(sslSocketFactory)
                                        .build())
                        .evictExpiredConnections()
                        .build();

                return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
            } catch (Exception e) {
                log.warn("No se pudo configurar RestTemplate inseguro, usando el por defecto", e);
            }
        }
        return new RestTemplate();
    }

    @Bean
    public WebClient.Builder webClientBuilder(BffProperties properties) {
        // Configure connection provider with proper pool settings
        ConnectionProvider connectionProvider = ConnectionProvider.builder("mcp-connection-pool")
            .maxConnections(50)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(45))
            .evictInBackground(Duration.ofSeconds(120))
            .build();

        // Get timeout from properties, default to 30 seconds
        int timeoutMs = properties.getMcp().getTimeoutMs();
        if (timeoutMs <= 0) {
            timeoutMs = 30000;
        }
        Duration timeout = Duration.ofMillis(timeoutMs);

        HttpClient httpClient;
        if (properties.getMcp().isInsecureSkipTlsVerify()) {
            try {
                var sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                httpClient = HttpClient.create(connectionProvider)
                    .secure(sslSpec ->
                        sslSpec
                            .sslContext(sslContext)
                            .handshakeTimeout(Duration.ofSeconds(30))
                    )
                    .responseTimeout(timeout)
                    .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                    .option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                    .doOnConnected(conn ->
                        conn.addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS))
                            .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS))
                    );
                log.info("WebClient configurado con timeout={}ms, insecureSkipTlsVerify=true", timeoutMs);
            } catch (SSLException e) {
                throw new RuntimeException("Failed to build insecure SSL context for WebClient", e);
            }
        } else {
            httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(timeout)
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                .option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                .doOnConnected(conn ->
                    conn.addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS))
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS))
                );
            log.info("WebClient configurado con timeout={}ms, insecureSkipTlsVerify=false", timeoutMs);
        }

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
