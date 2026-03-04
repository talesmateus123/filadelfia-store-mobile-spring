#!/bin/bash

# 🧪 Script para testar o deployment do Filadelfia Store
# Execute no servidor VPS como usuário filadelfia

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Função para print colorido
print_test() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✅ PASS]${NC} $1"
}

print_fail() {
    echo -e "${RED}[❌ FAIL]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[⚠️ WARNING]${NC} $1"
}

echo "🧪 Iniciando testes de deployment do Filadelfia Store..."
echo ""

# Contadores
TESTS_PASSED=0
TESTS_FAILED=0

# Variáveis
APP_DIR="/var/www/filadelfiastore"
BACKUP_DIR="/var/www/backups/filadelfiastore"
LOG_DIR="/opt/filadelfia-store/production/logs"
SERVICE_NAME="filadelfiastore"

# 1. Teste de estrutura de diretórios
print_test "Verificando estrutura de diretórios..."
if [ -d "$APP_DIR" ] && [ -d "$BACKUP_DIR" ] && [ -d "$LOG_DIR" ]; then
    print_success "Diretórios existem"
    ((TESTS_PASSED++))
else
    print_fail "Diretórios não encontrados"
    ls -la /var/www/ /opt/filadelfia-store/production/
    ((TESTS_FAILED++))
fi

# 2. Teste de permissões
print_test "Verificando permissões de diretórios..."
if [ -w "$APP_DIR" ] && [ -w "$BACKUP_DIR" ] && [ -w "$LOG_DIR" ]; then
    print_success "Permissões de escrita OK"
    ((TESTS_PASSED++))
else
    print_fail "Problemas de permissão"
    ls -la "$APP_DIR" "$BACKUP_DIR" "$LOG_DIR"
    ((TESTS_FAILED++))
fi

# 3. Teste de Java
print_test "Verificando Java 21..."
if java -version 2>&1 | grep -q "21"; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_success "Java 21 disponível: $JAVA_VERSION"
    ((TESTS_PASSED++))
else
    print_fail "Java 21 não encontrado"
    java -version
    ((TESTS_FAILED++))
fi

# 4. Teste de conectividade MySQL
print_test "Verificando MySQL..."
if systemctl is-active --quiet mysql; then
    print_success "MySQL está ativo"
    
    # Testar conexão se credenciais estiverem disponível
    if [ -f "/etc/systemd/system/filadelfiastore.env" ]; then
        source /etc/systemd/system/filadelfiastore.env
        if mysql -u "${DATABASE_USERNAME}" -p"${DATABASE_PASSWORD}" -e "SELECT 1;" 2>/dev/null; then
            print_success "Conexão com banco de dados OK"
            ((TESTS_PASSED++))
        else
            print_fail "Erro na conexão com banco de dados"
            ((TESTS_FAILED++))
        fi
    else
        print_warning "Arquivo de configuração não encontrado, pulando teste de DB"
    fi
else
    print_fail "MySQL não está ativo"
    systemctl status mysql --no-pager
    ((TESTS_FAILED++))
fi

# 5. Teste do serviço systemd
print_test "Verificando configuração do serviço..."
if [ -f "/etc/systemd/system/$SERVICE_NAME.service" ]; then
    print_success "Arquivo de serviço existe"
    
    if systemctl is-enabled --quiet "$SERVICE_NAME"; then
        print_success "Serviço está habilitado"
        ((TESTS_PASSED++))
    else
        print_fail "Serviço não está habilitado"
        ((TESTS_FAILED++))
    fi
else
    print_fail "Arquivo de serviço não encontrado"
    ((TESTS_FAILED++))
fi

# 6. Teste de JAR (se existir)
print_test "Verificando JAR da aplicação..."
if [ -f "$APP_DIR/filadelfiastore.jar" ]; then
    print_success "JAR existe: $(ls -lh $APP_DIR/filadelfiastore.jar)"
    
    # Verificar se é um JAR válido
    if java -jar "$APP_DIR/filadelfiastore.jar" --version 2>/dev/null || file "$APP_DIR/filadelfiastore.jar" | grep -q "Java"; then
        print_success "JAR é válido"
        ((TESTS_PASSED++))
    else
        print_fail "JAR pode estar corrompido"
        file "$APP_DIR/filadelfiastore.jar"
        ((TESTS_FAILED++))
    fi
else
    print_warning "JAR não encontrado (primeira execução?)"
fi

# 7. Teste de status do serviço
print_test "Verificando status do serviço..."
if systemctl is-active --quiet "$SERVICE_NAME"; then
    print_success "Serviço está ativo"
    
    # Verificar logs recentes para erros
    if ! journalctl -u "$SERVICE_NAME" --since "10 minutes ago" | grep -i error; then
        print_success "Sem erros nos logs recentes"
        ((TESTS_PASSED++))
    else
        print_warning "Erros encontrados nos logs"
    fi
    
else
    print_warning "Serviço não está ativo (pode ser normal se primeira vez)"
    systemctl status "$SERVICE_NAME" --no-pager || true
fi

# 8. Teste de conectividade HTTP
print_test "Testando conectividade HTTP..."
if curl -f -s --connect-timeout 5 http://localhost:8080/actuator/health > /dev/null 2>&1; then
    print_success "Aplicação responde na porta 8080"
    
    # Testar endpoint específico se disponível
    HEALTH_STATUS=$(curl -s http://localhost:8080/actuator/health | grep -o '"status":"[^"]*"' || echo "N/A")
    print_success "Health check: $HEALTH_STATUS"
    ((TESTS_PASSED++))
    
elif curl -f -s --connect-timeout 5 http://localhost:8080 > /dev/null 2>&1; then
    print_success "Aplicação responde na porta 8080 (sem actuator)"
    ((TESTS_PASSED++))
else
    print_warning "Aplicação não responde na porta 8080"
    
    # Verificar se porta está em uso
    if netstat -tuln | grep -q ":8080"; then
        print_warning "Porta 8080 está em uso por outro processo"
        netstat -tuln | grep ":8080"
    else
        print_warning "Nada está executando na porta 8080"
    fi
fi

# 9. Teste de logs
print_test "Verificando configuração de logs..."
if [ -d "$LOG_DIR" ]; then
    print_success "Diretório de logs existe"
    
    # Verificar logs da aplicação se existir
    if [ -f "$LOG_DIR/application.log" ]; then
        print_success "Arquivo de log existe"
        LOG_SIZE=$(du -h "$LOG_DIR/application.log" | cut -f1)
        print_success "Tamanho do log: $LOG_SIZE"
        ((TESTS_PASSED++))
    else
        print_warning "Arquivo de log ainda não foi criado"
    fi
else
    print_fail "Diretório de logs não existe"
    ((TESTS_FAILED++))
fi

# 10. Teste de backup
print_test "Verificando configuração de backup..."
if [ -w "$BACKUP_DIR" ]; then
    print_success "Diretório de backup acessível"
    
    # Contar backups existentes
    BACKUP_COUNT=$(ls -1 "$BACKUP_DIR"/filadelfiastore_backup_*.jar 2>/dev/null | wc -l)
    print_success "Backups existentes: $BACKUP_COUNT"
    ((TESTS_PASSED++))
else
    print_fail "Diretório de backup não acessível"
    ((TESTS_FAILED++))
fi

echo ""
echo "================== RESUMO DOS TESTES =================="
echo -e "${GREEN}✅ Testes aprovados: $TESTS_PASSED${NC}"
echo -e "${RED}❌ Testes falharam: $TESTS_FAILED${NC}"
echo ""

if [ "$TESTS_FAILED" -eq 0 ]; then
    echo -e "${GREEN}🎉 Todos os testes principais passaram!${NC}"
    echo -e "${GREEN}   O ambiente está pronto para deploy.${NC}"
    exit 0
elif [ "$TESTS_FAILED" -le 2 ]; then
    echo -e "${YELLOW}⚠️ Alguns testes falharam, mas pode estar OK.${NC}"
    echo -e "${YELLOW}   Revise os problemas antes do deploy.${NC}"
    exit 1
else
    echo -e "${RED}❌ Muitos testes falharam!${NC}"
    echo -e "${RED}   Corrija os problemas antes de prosseguir.${NC}"
    exit 1
fi