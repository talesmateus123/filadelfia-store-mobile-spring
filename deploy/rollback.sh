#!/bin/bash

# 🔄 Script para fazer rollback da aplicação Filadelfia Store
# Uso: ./rollback.sh [timestamp] ou ./rollback.sh --list

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
BACKUP_DIR="/var/www/backups/filadelfiastore"
CURRENT_JAR="$APP_DIR/filadelfiastore.jar"

# Funções de print
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

# Função para listar backups
list_backups() {
    echo "📦 Backups disponíveis:"
    echo ""
    
    if ls "$BACKUP_DIR"/filadelfiastore_backup_*.jar >/dev/null 2>&1; then
        echo "| Timestamp       | Arquivo                               | Tamanho | Data              |"
        echo "|-----------------|---------------------------------------|---------|-------------------|"
        
        for backup in $(ls -t "$BACKUP_DIR"/filadelfiastore_backup_*.jar); do
            filename=$(basename "$backup")
            timestamp=$(echo "$filename" | sed 's/filadelfiastore_backup_\(.*\)\.jar/\1/')
            size=$(du -h "$backup" | cut -f1)
            
            # Converter timestamp para data legível
            year=${timestamp:0:4}
            month=${timestamp:4:2}
            day=${timestamp:6:2}
            hour=${timestamp:9:2}
            minute=${timestamp:11:2}
            second=${timestamp:13:2}
            
            date_formatted="$day/$month/$year $hour:$minute:$second"
            
            printf "| %-15s | %-37s | %-7s | %-17s |\n" "$timestamp" "$filename" "$size" "$date_formatted"
        done
        
        echo ""
        print_info "Para fazer rollback: ./rollback.sh TIMESTAMP"
        print_info "Exemplo: ./rollback.sh 20240302_143022"
        
    else
        print_warning "Nenhum backup encontrado em $BACKUP_DIR"
    fi
}

# Função para fazer rollback
perform_rollback() {
    local timestamp=$1
    local backup_file="$BACKUP_DIR/filadelfiastore_backup_${timestamp}.jar"
    
    # Verificar se o backup existe
    if [ ! -f "$backup_file" ]; then
        print_error "Backup não encontrado: $backup_file"
        echo ""
        list_backups
        exit 1
    fi
    
    print_info "Iniciando rollback para: $timestamp"
    print_info "Backup: $backup_file"
    
    # Confirmar ação
    print_warning "⚠️ Esta ação irá substituir a versão atual!"
    read -p "Deseja continuar? (y/N): " confirm
    if [[ $confirm != [yY] ]]; then
        print_info "Rollback cancelado"
        exit 0
    fi
    
    # Criar backup da versão atual antes do rollback
    current_timestamp=$(date +%Y%m%d_%H%M%S)
    pre_rollback_backup="$BACKUP_DIR/filadelfiastore_pre_rollback_${current_timestamp}.jar"
    
    print_info "Criando backup da versão atual..."
    if [ -f "$CURRENT_JAR" ]; then
        cp "$CURRENT_JAR" "$pre_rollback_backup"
        print_success "Backup da versão atual criado: $(basename "$pre_rollback_backup")"
    else
        print_warning "JAR atual não encontrado, pulando backup"
    fi
    
    # Parar serviço
    print_info "Parando serviço $SERVICE_NAME..."
    sudo systemctl stop $SERVICE_NAME || true
    sleep 3
    
    # Fazer rollback
    print_info "Restaurando backup..."
    cp "$backup_file" "$CURRENT_JAR"
    
    # Configurar permissões
    chown filadelfia:filadelfia "$CURRENT_JAR"
    chmod 755 "$CURRENT_JAR"
    
    print_success "Arquivo restaurado com sucesso"
    
    # Iniciar serviço
    print_info "Iniciando serviço $SERVICE_NAME..."
    sudo systemctl start $SERVICE_NAME
    
    # Aguardar inicialização
    print_info "Aguardando inicialização..."
    sleep 15
    
    # Verificar status
    if systemctl is-active --quiet $SERVICE_NAME; then
        print_success "✅ Rollback concluído com sucesso!"
        
        # Teste de conectividade
        if curl -f -s --connect-timeout 10 http://localhost:8080 > /dev/null 2>&1; then
            print_success "✅ Aplicação está respondendo na porta 8080"
        else
            print_warning "⚠️ Aplicação pode não estar totalmente carregada ainda"
        fi
        
        # Mostrar status
        echo ""
        print_info "Status do serviço:"
        systemctl status $SERVICE_NAME --no-pager -l
        
    else
        print_error "❌ Falha no rollback!"
        print_error "Serviço não está ativo, verificando logs..."
        echo ""
        journalctl -u $SERVICE_NAME --no-pager -l --since "5 minutes ago"
        
        print_info "Tentando restaurar backup da versão atual..."
        if [ -f "$pre_rollback_backup" ]; then
            cp "$pre_rollback_backup" "$CURRENT_JAR"
            sudo systemctl start $SERVICE_NAME
        fi
        exit 1
    fi
    
    # Limpeza de backups antigos (manter apenas últimos 10)
    print_info "Limpando backups antigos..."
    ls -t "$BACKUP_DIR"/filadelfiastore_backup_*.jar | tail -n +11 | xargs -r rm
    ls -t "$BACKUP_DIR"/filadelfiastore_pre_rollback_*.jar | tail -n +6 | xargs -r rm
    
    print_success "🎉 Rollback concluído!"
}

# Função de ajuda
show_help() {
    echo "🔄 Rollback do Filadelfia Store"
    echo ""
    echo "Uso: $0 [opção]"
    echo ""
    echo "Opções:"
    echo "  --list                Lista backups disponíveis (padrão)"
    echo "  TIMESTAMP             Faz rollback para timestamp específico"
    echo "  --help                Mostra esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0                    # Lista backups"
    echo "  $0 --list            # Lista backups"  
    echo "  $0 20240302_143022   # Rollback para timestamp"
    echo ""
    echo "Timestamps têm formato: YYYYMMDD_HHMMSS"
}

# Main
case "${1:-list}" in
    "--list"|"list"|"")
        list_backups
        ;;
    "--help"|"-h"|"help")
        show_help
        ;;
    *)
        # Assumir que é um timestamp
        if [[ $1 =~ ^[0-9]{8}_[0-9]{6}$ ]]; then
            perform_rollback "$1"
        else
            print_error "Timestamp inválido: $1"
            print_error "Formato esperado: YYYYMMDD_HHMMSS"
            echo ""
            show_help
            exit 1
        fi
        ;;
esac