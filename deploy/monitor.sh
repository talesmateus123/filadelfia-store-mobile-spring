#!/bin/bash

# 📊 Script para monitorar status da aplicação Filadelfia Store
# Uso: ./monitor.sh [start|stop|restart|status|logs]

set -e

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Variáveis
SERVICE_NAME="filadelfiastore"
APP_DIR="/var/www/filadelfiastore"
LOG_DIR="/opt/filadelfia-store/production/logs"
BACKUP_DIR="/var/www/backups/filadelfiastore"

# Função para print colorido
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[OK]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Função para verificar status
check_status() {
    echo "================== STATUS DO FILADELFIA STORE =================="
    echo ""
    
    # Status do serviço
    print_info "Status do serviço systemd:"
    if systemctl is-active --quiet $SERVICE_NAME; then
        print_success "Serviço está ATIVO"
    else
        print_error "Serviço está INATIVO"
    fi
    
    systemctl status $SERVICE_NAME --no-pager -l || true
    echo ""
    
    # Verificar processo Java
    print_info "Processos Java relacionados:"
    ps aux | grep -E "(filadelfiastore|java.*filadelfia)" | grep -v grep || print_warning "Nenhum processo encontrado"
    echo ""
    
    # Verificar porta 8080
    print_info "Status da porta 8080:"
    if netstat -tuln | grep -q ":8080"; then
        print_success "Porta 8080 está em uso"
        netstat -tuln | grep ":8080"
    else
        print_warning "Porta 8080 não está em uso"
    fi
    echo ""
    
    # Teste HTTP
    print_info "Teste de conectividade HTTP:"
    if curl -f -s --connect-timeout 5 http://localhost:8080/actuator/health > /dev/null 2>&1; then
        HEALTH=$(curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "N/A")
        print_success "Health check: $HEALTH"
    elif curl -f -s --connect-timeout 5 http://localhost:8080 > /dev/null 2>&1; then
        print_success "Aplicação responde na porta 8080"
    else
        print_error "Aplicação NÃO responde na porta 8080"
    fi
    echo ""
    
    # Verificar logs
    print_info "Status dos logs:"
    if [ -f "$LOG_DIR/application.log" ]; then
        LOG_SIZE=$(du -h "$LOG_DIR/application.log" | cut -f1)
        LOG_LINES=$(wc -l < "$LOG_DIR/application.log")
        print_success "Log existe: $LOG_SIZE ($LOG_LINES linhas)"
        
        print_info "Últimas 5 linhas do log:"
        tail -5 "$LOG_DIR/application.log" 2>/dev/null || print_warning "Não foi possível ler o log"
    else
        print_warning "Arquivo de log não existe ainda"
    fi
    echo ""
    
    # Verificar backups
    print_info "Status dos backups:"
    BACKUP_COUNT=$(ls -1 "$BACKUP_DIR"/filadelfiastore_backup_*.jar 2>/dev/null | wc -l)
    if [ "$BACKUP_COUNT" -gt 0 ]; then
        print_success "Backups encontrados: $BACKUP_COUNT"
        ls -lht "$BACKUP_DIR"/filadelfiastore_backup_*.jar | head -3
    else
        print_warning "Nenhum backup encontrado"
    fi
    echo ""
    
    # Uso de recursos
    print_info "Uso de memória:"
    if pgrep -f filadelfiastore > /dev/null; then
        ps -o pid,ppid,cmd,%mem,%cpu --sort=-%mem -C java | grep -i filadelfia || true
    else
        print_warning "Processo não encontrado"
    fi
}

# Função para ver logs
show_logs() {
    local num_lines=${1:-50}
    echo "================== LOGS DO FILADELFIA STORE =================="
    
    print_info "Logs do systemd (últimas $num_lines linhas):"
    journalctl -u $SERVICE_NAME --no-pager -l -n $num_lines
    
    echo ""
    if [ -f "$LOG_DIR/application.log" ]; then
        print_info "Logs da aplicação (últimas $num_lines linhas):"
        tail -n $num_lines "$LOG_DIR/application.log"
    else
        print_warning "Arquivo de log da aplicação não existe"
    fi
}

# Função para seguir logs em tempo real
follow_logs() {
    echo "================== LOGS EM TEMPO REAL =================="
    print_info "Pressione Ctrl+C para sair"
    echo ""
    
    # Seguir logs do systemd e da aplicação simultaneamente
    if [ -f "$LOG_DIR/application.log" ]; then
        tail -f "$LOG_DIR/application.log" &
        APP_LOG_PID=$!
    fi
    
    journalctl -u $SERVICE_NAME -f &
    JOURNAL_PID=$!
    
    # Trap para cleanup
    trap "kill $APP_LOG_PID $JOURNAL_PID 2>/dev/null; exit 0" INT TERM
    wait
}

# Função para reiniciar aplicação
restart_app() {
    print_info "Reiniciando $SERVICE_NAME..."
    
    sudo systemctl stop $SERVICE_NAME || true
    sleep 3
    sudo systemctl start $SERVICE_NAME
    
    print_info "Aguardando inicialização..."
    sleep 10
    
    if systemctl is-active --quiet $SERVICE_NAME; then
        print_success "Serviço reiniciado com sucesso!"
        check_status
    else
        print_error "Falha ao reiniciar o serviço!"
        journalctl -u $SERVICE_NAME --no-pager -l -n 20
    fi
}

# Função para parar aplicação
stop_app() {
    print_info "Parando $SERVICE_NAME..."
    sudo systemctl stop $SERVICE_NAME
    print_success "Serviço parado"
}

# Função para iniciar aplicação
start_app() {
    print_info "Iniciando $SERVICE_NAME..."
    sudo systemctl start $SERVICE_NAME
    
    print_info "Aguardando inicialização..."
    sleep 10
    
    if systemctl is-active --quiet $SERVICE_NAME; then
        print_success "Serviço iniciado com sucesso!"
    else
        print_error "Falha ao iniciar o serviço!"
        journalctl -u $SERVICE_NAME --no-pager -l -n 20
    fi
}

# Menu de ajuda
show_help() {
    echo "📊 Monitor do Filadelfia Store"
    echo ""
    echo "Uso: $0 [comando]"
    echo ""
    echo "Comandos disponíveis:"
    echo "  status    - Mostra status completo da aplicação (padrão)"
    echo "  logs      - Mostra logs recentes"
    echo "  follow    - Segue logs em tempo real"
    echo "  start     - Inicia a aplicação"
    echo "  stop      - Para a aplicação"  
    echo "  restart   - Reinicia a aplicação"
    echo "  help      - Mostra esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0                # Status completo"
    echo "  $0 logs          # Logs recentes"
    echo "  $0 follow        # Seguir logs"
    echo "  $0 restart       # Reiniciar app"
}

# Main
case "${1:-status}" in
    "status")
        check_status
        ;;
    "logs")
        show_logs ${2:-50}
        ;;
    "follow"|"tail")
        follow_logs
        ;;
    "start")
        start_app
        ;;
    "stop")
        stop_app
        ;;
    "restart")
        restart_app
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        print_error "Comando inválido: $1"
        echo ""
        show_help
        exit 1
        ;;
esac