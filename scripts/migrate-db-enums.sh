#!/bin/bash
# =============================================================================
# migrate-db-enums.sh
# Migração do banco MySQL: sincroniza enums com os valores do código Java.
#
# Uso no VPS:
#   chmod +x migrate-db-enums.sh
#   ./migrate-db-enums.sh
#
# Ou passando credenciais como argumentos:
#   ./migrate-db-enums.sh --host=localhost --user=MEU_USER --password=MINHA_SENHA --database=MEU_DB
# =============================================================================

set -euo pipefail

# --- Cores para output ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC}  $1"; }
log_ok()      { echo -e "${GREEN}[OK]${NC}    $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }
log_section() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

# =============================================================================
# 1. Leitura de configurações
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

DB_HOST="localhost"
DB_PORT="3306"
DB_USER=""
DB_PASS=""
DB_NAME=""

# Carrega .env se existir
if [[ -f "$ENV_FILE" ]]; then
    log_info "Carregando variáveis de $ENV_FILE"
    DB_URL=$(grep '^DATABASE_URL=' "$ENV_FILE" 2>/dev/null | cut -d'=' -f2- | tr -d '"' || true)
    if [[ -n "$DB_URL" ]]; then
        # Extrai host, porta e nome do banco da JDBC URL
        # Formato: jdbc:mysql://host:port/dbname
        DB_HOST=$(echo "$DB_URL" | sed -E 's|.*://([^:/]+).*|\1|')
        DB_PORT=$(echo "$DB_URL" | sed -E 's|.*:([0-9]+)/.*|\1|')
        DB_NAME=$(echo "$DB_URL" | sed -E 's|.*/([^?]+).*|\1|')
    fi
    DB_USER=$(grep '^DATABASE_USERNAME=' "$ENV_FILE" 2>/dev/null | cut -d'=' -f2- | tr -d '"' || true)
    DB_PASS=$(grep '^DATABASE_PASSWORD=' "$ENV_FILE" 2>/dev/null | cut -d'=' -f2- | tr -d '"' || true)
fi

# Sobrescreve com argumentos da linha de comando, se fornecidos
for arg in "$@"; do
    case $arg in
        --host=*)     DB_HOST="${arg#*=}" ;;
        --port=*)     DB_PORT="${arg#*=}" ;;
        --user=*)     DB_USER="${arg#*=}" ;;
        --password=*) DB_PASS="${arg#*=}" ;;
        --database=*) DB_NAME="${arg#*=}" ;;
    esac
done

# Solicita interativamente o que ainda estiver vazio
[[ -z "$DB_USER" ]]     && read -rp  "Usuário do banco: "   DB_USER
[[ -z "$DB_PASS" ]]     && { read -rsp "Senha do banco: "   DB_PASS; echo ""; }
[[ -z "$DB_NAME" ]]     && read -rp  "Nome do banco: "      DB_NAME

# =============================================================================
# 2. Cria arquivo temporário de configuração MySQL (evita problemas com senhas
#    contendo caracteres especiais como $, @, !, etc.)
# =============================================================================

MYSQL_CNF=$(mktemp /tmp/mysql_migrate_XXXXXX.cnf)
chmod 600 "$MYSQL_CNF"

# Garante remoção do arquivo mesmo em caso de erro
cleanup() { rm -f "$MYSQL_CNF"; }
trap cleanup EXIT

cat > "$MYSQL_CNF" <<EOF
[client]
host=$DB_HOST
port=$DB_PORT
user=$DB_USER
password=$DB_PASS
database=$DB_NAME
EOF

# Atalho para rodar mysql com o arquivo de config
mysql_run() { mysql --defaults-file="$MYSQL_CNF" "$@" 2>/dev/null; }

# =============================================================================
# 3. Verifica conexão
# =============================================================================

log_section "Verificando conexão com o banco"

if ! mysql_run -e "SELECT 1;" > /dev/null; then
    log_error "Falha ao conectar no banco '$DB_NAME' em $DB_HOST:$DB_PORT como '$DB_USER'"
    exit 1
fi
log_ok "Conexão bem-sucedida com '$DB_NAME' em $DB_HOST:$DB_PORT"

# =============================================================================
# 4. Snapshot do estado atual
# =============================================================================

log_section "Estado atual dos enums no banco"
mysql_run -e "
    SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = '$DB_NAME'
      AND DATA_TYPE = 'enum'
    ORDER BY TABLE_NAME, COLUMN_NAME;"

# =============================================================================
# 5. Função de migração (só aplica se o valor ainda não existir)
# =============================================================================

run_migration() {
    local description="$1"
    local table="$2"
    local column="$3"
    local new_type="$4"
    local check_value="$5"   # Um valor novo que deve estar presente após a migração

    local current
    current=$(mysql_run -sN -e "
        SELECT COLUMN_TYPE
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA='$DB_NAME'
          AND TABLE_NAME='$table'
          AND COLUMN_NAME='$column';")

    if echo "$current" | grep -q "'$check_value'"; then
        log_warn "Já atualizado — ignorando: $description"
        return
    fi

    log_info "Aplicando: $description"
    if mysql_run -e "ALTER TABLE \`$table\` MODIFY COLUMN \`$column\` $new_type NOT NULL;"; then
        log_ok "Sucesso: $description"
    else
        log_error "Falha ao aplicar: $description"
        exit 1
    fi
}

# =============================================================================
# 6. Migrações
# =============================================================================

log_section "Aplicando migrações"

# orders.payment_method — adicionados BANK_TRANSFER, STRIPE_CARD, STRIPE_PIX
run_migration \
    "orders.payment_method → STRIPE_CARD, STRIPE_PIX" \
    "orders" "payment_method" \
    "ENUM('BANK_TRANSFER','BOLETO','CASH','CREDIT_CARD','DEBIT_CARD','PIX','STRIPE_CARD','STRIPE_PIX')" \
    "STRIPE_CARD"

# orders.status — valores completos de OrderStatus
run_migration \
    "orders.status → todos os valores de OrderStatus" \
    "orders" "status" \
    "ENUM('PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED','REFUNDED')" \
    "REFUNDED"

# payments.payment_method — adicionados BANK_TRANSFER, STRIPE_CARD, STRIPE_PIX
run_migration \
    "payments.payment_method → STRIPE_CARD, STRIPE_PIX" \
    "payments" "payment_method" \
    "ENUM('BANK_TRANSFER','BOLETO','CASH','CREDIT_CARD','DEBIT_CARD','PIX','STRIPE_CARD','STRIPE_PIX')" \
    "STRIPE_CARD"

# payments.status — valores completos de PaymentStatus
run_migration \
    "payments.status → todos os valores de PaymentStatus" \
    "payments" "status" \
    "ENUM('PENDING','PROCESSING','AUTHORIZED','CAPTURED','CONFIRMED','CANCELLED','FAILED','REFUNDED','EXPIRED','REJECTED')" \
    "REJECTED"

# users.role — valores completos de UserRole
run_migration \
    "users.role → USER, MANAGER, ADMIN" \
    "users" "role" \
    "ENUM('USER','MANAGER','ADMIN')" \
    "MANAGER"

# =============================================================================
# 7. Verificação final
# =============================================================================

log_section "Estado final dos enums (após migração)"
mysql_run -e "
    SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = '$DB_NAME'
      AND DATA_TYPE = 'enum'
    ORDER BY TABLE_NAME, COLUMN_NAME;"

log_section "✅ Migração concluída com sucesso"
log_info "Reinicie o servidor para aplicar as mudanças:"
log_info "  sudo systemctl restart filadelfiastore"
