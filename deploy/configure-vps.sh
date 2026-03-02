#!/bin/bash

# 🛠️ Script de configuração completa do VPS para Filadelfia Store
# Execute como root: sudo bash configure-vps.sh

set -e

echo "🚀 Iniciando configuração do VPS para Filadelfia Store..."

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variáveis
FILADELFIA_USER="filadelfia"
APP_DIR="/var/www/filadelfiastore"
BACKUP_DIR="/var/www/backups/filadelfiastore"
LOG_DIR="/opt/filadelfia-store/production/logs"
MYSQL_DB="filadelfia_store_prod"
MYSQL_USER="filadelfia_prod_user"

# Função para print colorido
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar se está executando como root
if [ "$EUID" -ne 0 ]; then
    print_error "Execute este script como root: sudo bash configure-vps.sh"
    exit 1
fi

# 1. Atualizar sistema
print_status "Atualizando sistema..."
apt update && apt upgrade -y

# 2. Instalar pacotes necessários
print_status "Instalando pacotes base..."
apt install -y curl wget git unzip software-properties-common apt-transport-https ca-certificates

# 3. Verificar/Instalar Java 21
print_status "Verificando Java 21..."
if java -version 2>&1 | grep -q "21"; then
    print_status "Java 21 já está instalado"
else
    print_status "Instalando OpenJDK 21..."
    apt install -y openjdk-21-jdk
fi

# Verificar instalação
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
print_status "Java instalado: $JAVA_VERSION"

# 4. Configurar JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
echo "export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64" >> /etc/environment

# 5. Verificar/Instalar MySQL
print_status "Verificando MySQL..."
if systemctl is-active --quiet mysql; then
    print_status "MySQL já está instalado e ativo"
else
    print_status "Instalando MySQL Server..."
    apt install -y mysql-server
    systemctl start mysql
    systemctl enable mysql
fi

# 6. Criar usuário filadelfia
print_status "Criando usuário $FILADELFIA_USER..."
if id "$FILADELFIA_USER" &>/dev/null; then
    print_warning "Usuário $FILADELFIA_USER já existe"
else
    useradd -m -s /bin/bash "$FILADELFIA_USER"
    usermod -aG sudo "$FILADELFIA_USER"
    print_status "Usuário $FILADELFIA_USER criado com privilégios sudo"
fi

# 7. Configurar diretórios da aplicação
print_status "Criando diretórios da aplicação..."
mkdir -p "$APP_DIR"
mkdir -p "$BACKUP_DIR"
mkdir -p "$LOG_DIR"

# Configurar permissões
chown -R "$FILADELFIA_USER:$FILADELFIA_USER" "$APP_DIR"
chown -R "$FILADELFIA_USER:$FILADELFIA_USER" "$BACKUP_DIR"
chown -R "$FILADELFIA_USER:$FILADELFIA_USER" "$LOG_DIR"

chmod 755 "$APP_DIR"
chmod 755 "$BACKUP_DIR"
chmod 755 "$LOG_DIR"

print_status "Diretórios criados:"
print_status "  - Aplicação: $APP_DIR"
print_status "  - Backups: $BACKUP_DIR"
print_status "  - Logs: $LOG_DIR"

# 8. Configurar SSH para usuário filadelfia
print_status "Configurando SSH para usuário $FILADELFIA_USER..."
FILADELFIA_HOME="/home/$FILADELFIA_USER"
SSH_DIR="$FILADELFIA_HOME/.ssh"

# Criar diretório SSH
sudo -u "$FILADELFIA_USER" mkdir -p "$SSH_DIR"
sudo -u "$FILADELFIA_USER" chmod 700 "$SSH_DIR"

# Criar arquivo authorized_keys se não existir
if [ ! -f "$SSH_DIR/authorized_keys" ]; then
    sudo -u "$FILADELFIA_USER" touch "$SSH_DIR/authorized_keys"
    sudo -u "$FILADELFIA_USER" chmod 600 "$SSH_DIR/authorized_keys"
fi

print_warning "⚠️ ATENÇÃO: Adicione a chave pública SSH do deploy no arquivo:"
print_warning "  $SSH_DIR/authorized_keys"

# 9. Configurar banco de dados MySQL
print_status "Configurando banco de dados MySQL..."

# Gerar senha aleatória se não fornecida
if [ -z "$MYSQL_PASSWORD" ]; then
    MYSQL_PASSWORD=$(openssl rand -base64 32)
    print_status "Senha gerada para banco: $MYSQL_PASSWORD"
fi

# Executar comandos MySQL
mysql -u root << EOF
CREATE DATABASE IF NOT EXISTS $MYSQL_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$MYSQL_USER'@'localhost' IDENTIFIED BY '$MYSQL_PASSWORD';
GRANT ALL PRIVILEGES ON $MYSQL_DB.* TO '$MYSQL_USER'@'localhost';
FLUSH PRIVILEGES;
EOF

print_status "Banco de dados configurado:"
print_status "  - Database: $MYSQL_DB"
print_status "  - User: $MYSQL_USER"
print_status "  - Password: $MYSQL_PASSWORD"

# 10. Criar arquivo de serviço systemd
print_status "Criando serviço systemd..."
cat > /etc/systemd/system/filadelfiastore.service << EOF
[Unit]
Description=Filadelfia Store Spring Boot Application
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=$FILADELFIA_USER
Group=$FILADELFIA_USER
EnvironmentFile=/etc/systemd/system/filadelfiastore.env
ExecStart=/usr/bin/java -jar $APP_DIR/filadelfiastore.jar
ExecStop=/bin/kill -15 \$MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=filadelfiastore
KillMode=mixed
KillSignal=SIGTERM
SendSIGKILL=yes

# Configurações de segurança
PrivateTmp=yes
NoNewPrivileges=yes
ProtectSystem=strict
ReadWritePaths=$APP_DIR $BACKUP_DIR $LOG_DIR
ProtectHome=yes

# Configurações de recursos
MemoryMax=1G
TasksMax=100

[Install]
WantedBy=multi-user.target
EOF

# 11. Criar arquivo de variáveis de ambiente
print_status "Criando arquivo de variáveis de ambiente..."
cat > /etc/systemd/system/filadelfiastore.env << EOF
DATABASE_URL=jdbc:mysql://localhost:3306/$MYSQL_DB
DATABASE_USERNAME=$MYSQL_USER
DATABASE_PASSWORD=$MYSQL_PASSWORD
DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
DDL_AUTO=validate
DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect
SPRING_PROFILES_ACTIVE=production
JAVA_OPTS=-Xmx512m -Xms256m
EOF

# Proteger arquivo de variáveis
chmod 600 /etc/systemd/system/filadelfiastore.env
chown root:root /etc/systemd/system/filadelfiastore.env

# 12. Habilitar serviço systemd
print_status "Habilitando serviço systemd..."
systemctl daemon-reload
systemctl enable filadelfiastore

# 13. Configurar logrotate
print_status "Configurando logrotate..."
cat > /etc/logrotate.d/filadelfiastore << EOF
$LOG_DIR/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    copytruncate
}
EOF

# 14. Configurar firewall (se ufw estiver instalado)
if command -v ufw &> /dev/null; then
    print_status "Configurando firewall..."
    ufw allow 8080/tcp comment "Filadelfia Store"
    ufw --force enable
fi

# 15. Criar script de status
print_status "Criando scripts auxiliares..."
cat > /usr/local/bin/filadelfia-status << 'EOF'
#!/bin/bash
echo "=== Status do Filadelfia Store ==="
echo ""
echo "🔧 Serviço systemd:"
systemctl status filadelfiastore --no-pager -l
echo ""
echo "📊 Uso de recursos:"
ps aux | grep -E "(filadelfiastore|java.*filadelfia)" | grep -v grep
echo ""
echo "🌐 Teste de conectividade:"
curl -s -f http://localhost:8080/actuator/health || echo "❌ Serviço não responde na porta 8080"
echo ""
echo "📋 Logs recentes:"
journalctl -u filadelfiastore --no-pager -l --since "1 hour ago" | tail -20
EOF

chmod +x /usr/local/bin/filadelfia-status

# 16. Criar script de restart
cat > /usr/local/bin/filadelfia-restart << 'EOF'
#!/bin/bash
echo "🔄 Reiniciando Filadelfia Store..."
sudo systemctl restart filadelfiastore
sleep 5
echo "✅ Serviço reiniciado!"
/usr/local/bin/filadelfia-status
EOF

chmod +x /usr/local/bin/filadelfia-restart

# 17. Salvar informações importantes
INFO_FILE="/root/filadelfiastore-setup.txt"
print_status "Salvando informações de configuração..."
cat > "$INFO_FILE" << EOF
=== CONFIGURAÇÃO FILADELFIA STORE ===
Data: $(date)

USUÁRIO:
- Nome: $FILADELFIA_USER
- Home: $FILADELFIA_HOME
- SSH: $SSH_DIR

DIRETÓRIOS:
- Aplicação: $APP_DIR
- Backups: $BACKUP_DIR
- Logs: $LOG_DIR

BANCO DE DADOS:
- Database: $MYSQL_DB
- User: $MYSQL_USER
- Password: $MYSQL_PASSWORD

SERVIÇO:
- Nome: filadelfiastore.service
- Status: systemctl status filadelfiastore
- Logs: journalctl -u filadelfiastore

SCRIPTS:
- Status: filadelfia-status
- Restart: filadelfia-restart

GITHUB SECRETS NECESSÁRIOS:
- FILADELFIA_VPS_HOST: $(curl -s ipinfo.io/ip)
- FILADELFIA_VPS_USER: $FILADELFIA_USER
- FILADELFIA_VPS_SSH_KEY: [chave privada gerada localmente]
- FILADELFIA_VPS_PORT: 22
- FILADELFIA_DATABASE_PASSWORD: $MYSQL_PASSWORD

URL DA APLICAÇÃO:
http://$(curl -s ipinfo.io/ip):8080
EOF

print_status "Informações salvas em: $INFO_FILE"

# 18. Verificações finais
print_status "Executando verificações finais..."

# Verificar Java
java -version

# Verificar MySQL
systemctl status mysql --no-pager

# Verificar usuário
id "$FILADELFIA_USER"

# Verificar diretórios
ls -la "$APP_DIR"
ls -la "$BACKUP_DIR"
ls -la "$LOG_DIR"

# Verificar serviço
systemctl cat filadelfiastore

echo ""
print_status "🎉 Configuração do VPS concluída com sucesso!"
echo ""
print_status "📋 PRÓXIMOS PASSOS:"
print_status "1. Adicione a chave SSH pública ao arquivo: $SSH_DIR/authorized_keys"
print_status "2. Configure os GitHub Secrets (veja $INFO_FILE)"
print_status "3. Faça push na branch 'develop' para testar o deploy"
print_status "4. Monitore com: filadelfia-status"
echo ""
print_warning "⚠️ IMPORTANTE: Anote a senha do banco de dados!"
print_warning "  Database Password: $MYSQL_PASSWORD"
echo ""
print_status "📄 Todas as informações estão em: $INFO_FILE"