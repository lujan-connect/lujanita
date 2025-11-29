# Guía de Deployment - Ollama Embebido

## Requisitos de la VM

### Especificaciones Mínimas

```
CPU: 4 vCPUs
RAM: 8 GB
Disco: 50 GB SSD
OS: Ubuntu 22.04 LTS
```

### Especificaciones Recomendadas

```
CPU: 8 vCPUs
RAM: 16 GB
Disco: 100 GB SSD
OS: Ubuntu 22.04 LTS
```

## Instalación de Ollama

### 1. Instalar Ollama

```bash
# Descargar e instalar Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Verificar instalación
ollama --version
```

### 2. Configurar Ollama como Servicio

```bash
# Crear archivo de servicio systemd
sudo tee /etc/systemd/system/ollama.service > /dev/null <<EOF
[Unit]
Description=Ollama Service
After=network.target

[Service]
Type=simple
User=ollama
Group=ollama
ExecStart=/usr/local/bin/ollama serve
Restart=always
RestartSec=10
Environment="OLLAMA_HOST=0.0.0.0:11434"
Environment="OLLAMA_MODELS=/var/lib/ollama/models"

[Install]
WantedBy=multi-user.target
EOF

# Crear usuario ollama
sudo useradd -r -s /bin/false -d /var/lib/ollama ollama

# Crear directorio para modelos
sudo mkdir -p /var/lib/ollama/models
sudo chown -R ollama:ollama /var/lib/ollama

# Habilitar e iniciar servicio
sudo systemctl daemon-reload
sudo systemctl enable ollama
sudo systemctl start ollama

# Verificar estado
sudo systemctl status ollama
```

### 3. Descargar Modelos

```bash
# Modelo por defecto: TinyLlama (1.1B - ~700MB)
ollama pull tinyllama

# Modelo alternativo: Phi-2 (2.7B - ~1.6GB)
ollama pull phi-2

# Verificar modelos descargados
ollama list
```

### 4. Probar Ollama

```bash
# Test básico
curl http://localhost:11434/api/generate -d '{
  "model": "tinyllama",
  "prompt": "¿Cuál es el estado de mi pedido?",
  "stream": false
}'

# Test de chat
curl http://localhost:11434/api/chat -d '{
  "model": "tinyllama",
  "messages": [
    {
      "role": "user",
      "content": "Hola, soy Lujanita. ¿Cómo puedo ayudarte?"
    }
  ]
}'
```

## Optimización de Rendimiento

### 1. Límites de Recursos

```bash
# Editar /etc/systemd/system/ollama.service
[Service]
# Limitar uso de CPU (80%)
CPUQuota=80%

# Limitar uso de RAM (6GB)
MemoryMax=6G
MemoryHigh=5G

# Limitar número de hilos
Environment="OLLAMA_NUM_PARALLEL=2"
Environment="OLLAMA_MAX_LOADED_MODELS=2"

# Recargar configuración
sudo systemctl daemon-reload
sudo systemctl restart ollama
```

### 2. Configuración de Modelos

**TinyLlama (Recomendado para Producción):**
```bash
# Ventajas:
# - Rápido: ~1-2s por respuesta
# - Ligero: ~700MB de RAM
# - CPU-friendly: 2-4 vCPUs suficientes

# Desventajas:
# - Calidad limitada
# - Requiere prompts específicos
```

**Phi-2 (Mejor Calidad):**
```bash
# Ventajas:
# - Mejor comprensión
# - Respuestas más naturales
# - Menos alucinaciones

# Desventajas:
# - Más lento: ~3-5s por respuesta
# - Más pesado: ~1.6GB de RAM
# - Requiere más CPU: 4-8 vCPUs
```

### 3. Caché y Warmup

```bash
# Script de warmup (ejecutar al iniciar)
cat > /usr/local/bin/ollama-warmup.sh <<'EOF'
#!/bin/bash
echo "Warming up Ollama models..."

# Cargar modelo en memoria
curl -s http://localhost:11434/api/generate -d '{
  "model": "tinyllama",
  "prompt": "warmup",
  "stream": false
}' > /dev/null

echo "Warmup complete"
EOF

chmod +x /usr/local/bin/ollama-warmup.sh

# Agregar a cron para ejecutar después de reinicio
echo "@reboot sleep 30 && /usr/local/bin/ollama-warmup.sh" | sudo crontab -
```

## Instalación del Middleware

### 1. Prerequisitos

```bash
# Instalar Java 21
sudo apt update
sudo apt install -y openjdk-21-jdk

# Verificar instalación
java -version

# Instalar Maven
sudo apt install -y maven
mvn -version
```

### 2. Clonar y Compilar Proyecto

```bash
# Clonar repositorio
git clone https://github.com/lujan-de-cuyo/lujanita.git
cd lujanita/apps/middleware

# Compilar
mvn clean install -DskipTests

# Empaquetar
mvn package
```

### 3. Configurar Servicio del Middleware

```bash
# Crear usuario para la aplicación
sudo useradd -r -s /bin/false -d /opt/lujanita lujanita
sudo mkdir -p /opt/lujanita
sudo chown lujanita:lujanita /opt/lujanita

# Copiar JAR
sudo cp target/lujanita-middleware-1.0.0-SNAPSHOT.jar /opt/lujanita/
sudo chown lujanita:lujanita /opt/lujanita/*.jar

# Crear archivo de configuración
sudo tee /opt/lujanita/application.yml > /dev/null <<EOF
server:
  port: 9000

ollama:
  host: http://localhost:11434
  model: tinyllama
  timeout: 30000

mcp:
  odoo:
    url: \${ODOO_MCP_URL}
    timeout: 15000
EOF

# Crear servicio systemd
sudo tee /etc/systemd/system/lujanita-middleware.service > /dev/null <<EOF
[Unit]
Description=Lujanita Middleware
After=ollama.service
Requires=ollama.service

[Service]
Type=simple
User=lujanita
Group=lujanita
WorkingDirectory=/opt/lujanita
ExecStart=/usr/bin/java -jar lujanita-middleware-1.0.0-SNAPSHOT.jar
Restart=always
RestartSec=10
Environment="JAVA_OPTS=-Xmx2g -Xms1g"
Environment="SPRING_CONFIG_LOCATION=file:/opt/lujanita/application.yml"
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# Habilitar e iniciar
sudo systemctl daemon-reload
sudo systemctl enable lujanita-middleware
sudo systemctl start lujanita-middleware

# Verificar estado
sudo systemctl status lujanita-middleware
```

### 4. Configurar Nginx como Proxy Reverso

```bash
# Instalar Nginx
sudo apt install -y nginx

# Configurar virtual host
sudo tee /etc/nginx/sites-available/lujanita > /dev/null <<EOF
upstream middleware {
    server localhost:9000;
    keepalive 32;
}

server {
    listen 80;
    server_name lujanita.example.com;

    # Health check
    location /health {
        proxy_pass http://middleware;
        proxy_set_header Host \$host;
        access_log off;
    }

    # API
    location /api {
        proxy_pass http://middleware;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # Timeouts largos para Ollama
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Rate limiting
    limit_req_zone \$binary_remote_addr zone=api_limit:10m rate=10r/s;
    limit_req zone=api_limit burst=20 nodelay;
}
EOF

# Habilitar sitio
sudo ln -s /etc/nginx/sites-available/lujanita /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## Monitoreo

### 1. Health Checks

```bash
# Script de monitoreo
cat > /usr/local/bin/check-lujanita.sh <<'EOF'
#!/bin/bash

# Check Ollama
if ! curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "ERROR: Ollama is down"
    sudo systemctl restart ollama
fi

# Check Middleware
if ! curl -s http://localhost:9000/health > /dev/null; then
    echo "ERROR: Middleware is down"
    sudo systemctl restart lujanita-middleware
fi

echo "All services healthy"
EOF

chmod +x /usr/local/bin/check-lujanita.sh

# Agregar a cron (cada 5 minutos)
echo "*/5 * * * * /usr/local/bin/check-lujanita.sh" | sudo crontab -
```

### 2. Logs

```bash
# Ver logs de Ollama
sudo journalctl -u ollama -f

# Ver logs de Middleware
sudo journalctl -u lujanita-middleware -f

# Ver logs de Nginx
sudo tail -f /var/log/nginx/access.log
```

### 3. Métricas

```bash
# Verificar uso de recursos
htop

# Métricas de Ollama
curl http://localhost:11434/api/ps

# Métricas de Middleware
curl http://localhost:9000/actuator/metrics
```

## Troubleshooting

### Ollama no responde

```bash
# Verificar proceso
ps aux | grep ollama

# Verificar puerto
netstat -tulpn | grep 11434

# Revisar logs
sudo journalctl -u ollama --since "10 minutes ago"

# Reiniciar servicio
sudo systemctl restart ollama
```

### Middleware no puede conectar con Ollama

```bash
# Verificar conectividad
curl http://localhost:11434/api/tags

# Verificar configuración
cat /opt/lujanita/application.yml

# Revisar logs del middleware
sudo journalctl -u lujanita-middleware --since "10 minutes ago"
```

### Respuestas muy lentas

```bash
# Verificar uso de CPU
top -p $(pgrep ollama)

# Verificar uso de RAM
free -h

# Considerar:
# 1. Cambiar a modelo más pequeño (tinyllama)
# 2. Reducir max_tokens
# 3. Aumentar recursos de VM
```

## Backup y Recuperación

```bash
# Backup de modelos
sudo tar -czf ollama-models-backup.tar.gz /var/lib/ollama/models/

# Backup de configuración
sudo tar -czf lujanita-config-backup.tar.gz /opt/lujanita/

# Restaurar
sudo tar -xzf ollama-models-backup.tar.gz -C /
sudo tar -xzf lujanita-config-backup.tar.gz -C /
sudo systemctl restart ollama lujanita-middleware
```

## Actualización

```bash
# Actualizar Ollama
curl -fsSL https://ollama.com/install.sh | sh
sudo systemctl restart ollama

# Actualizar Middleware
cd lujanita/apps/middleware
git pull
mvn clean package
sudo systemctl stop lujanita-middleware
sudo cp target/lujanita-middleware-1.0.0-SNAPSHOT.jar /opt/lujanita/
sudo systemctl start lujanita-middleware
```

## Seguridad

```bash
# Firewall (UFW)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw deny 11434/tcp  # Ollama solo interno
sudo ufw deny 9000/tcp   # Middleware solo interno
sudo ufw enable

# Fail2ban para proteger contra ataques
sudo apt install -y fail2ban
sudo systemctl enable fail2ban
```

