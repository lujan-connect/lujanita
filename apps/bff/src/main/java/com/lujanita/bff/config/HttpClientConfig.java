package com.lujanita.bff.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate(BffProperties properties) {
        int timeoutMs = properties.getMcp().getTimeoutMs();
        log.info("Configurando RestTemplate con timeout={}ms, insecureSkipTlsVerify={}", 
                 timeoutMs, properties.getMcp().isInsecureSkipTlsVerify());
        
        try {
            // Configuración de timeouts
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeoutMs))
                    .setResponseTimeout(Timeout.ofMilliseconds(timeoutMs))
                    .build();

            SocketConfig socketConfig = SocketConfig.custom()
                    .setSoTimeout(Timeout.ofMilliseconds(timeoutMs))
                    .build();

            if (properties.getMcp().isInsecureSkipTlsVerify()) {
                TrustStrategy trustAllStrategy = (chain, authType) -> true;
                SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, trustAllStrategy)
                    .build();

                var sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(sslContext)
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build();

                var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .setDefaultSocketConfig(socketConfig)
                        .setMaxConnTotal(100)
                        .setMaxConnPerRoute(20)
                        .build();

                CloseableHttpClient httpClient = HttpClients.custom()
                        .setConnectionManager(connectionManager)
                        .setDefaultRequestConfig(requestConfig)
                        .evictExpiredConnections()
                        .evictIdleConnections(org.apache.hc.core5.util.TimeValue.ofSeconds(30))
                        .build();

                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
                return new RestTemplate(factory);
            } else {
                var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                        .setDefaultSocketConfig(socketConfig)
                        .setMaxConnTotal(100)
                        .setMaxConnPerRoute(20)
                        .build();

                CloseableHttpClient httpClient = HttpClients.custom()
                        .setConnectionManager(connectionManager)
                        .setDefaultRequestConfig(requestConfig)
                        .evictExpiredConnections()
                        .evictIdleConnections(org.apache.hc.core5.util.TimeValue.ofSeconds(30))
                        .build();

                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
                return new RestTemplate(factory);
            }
        } catch (Exception e) {
            log.error("Error configurando RestTemplate con timeouts, usando configuración básica", e);
            return new RestTemplate();
        }
    }

    @Bean
    public WebClient.Builder webClientBuilder(BffProperties properties) {
        int timeoutMs = properties.getMcp().getTimeoutMs();
        log.info("Configurando WebClient con timeout={}ms, insecureSkipTlsVerify={}", 
                 timeoutMs, properties.getMcp().isInsecureSkipTlsVerify());

        // Configurar connection provider con límites y timeouts
        ConnectionProvider connectionProvider = ConnectionProvider.builder("mcp-connection-pool")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofMillis(timeoutMs))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        WebClient.Builder builder = WebClient.builder();
        
        HttpClient httpClient;
        try {
            if (properties.getMcp().isInsecureSkipTlsVerify()) {
                var sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                httpClient = HttpClient.create(connectionProvider)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                    .responseTimeout(Duration.ofMillis(timeoutMs))
                    .doOnConnected(conn -> conn
                            .addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)))
                    .secure(sslSpec ->
                        sslSpec
                            .sslContext(sslContext)
                            .handshakeTimeout(Duration.ofSeconds(30))
                    );
            } else {
                httpClient = HttpClient.create(connectionProvider)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                    .responseTimeout(Duration.ofMillis(timeoutMs))
                    .doOnConnected(conn -> conn
                            .addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)));
            }
        } catch (SSLException e) {
            log.error("Error configurando SSL para WebClient", e);
            throw new RuntimeException("Failed to build SSL context for WebClient", e);
        }
        
        builder = builder.clientConnector(new ReactorClientHttpConnector(httpClient));
        return builder;
    }
}
