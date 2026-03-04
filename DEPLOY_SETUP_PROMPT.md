# 🚀 Prompt: Configurar Deploy Automático Spring Boot - Projeto Filadelfia Store

## 🎯 Contexto
Configure um sistema de deploy automático para um projeto Spring Boot Maven chamado "filadelfiastore" usando GitHub Actions. O projeto deve ser deployado na mesma VPS onde já roda outro projeto (tutoria_escola), mas com usuário e configurações separados.

## 📋 Requisitos

### 1. **Usuário no Servidor VPS**
- Criar usuário `filadelfia` com privilégios sudo
- Configurar SSH para este usuário específico
- Manter o usuário `tutoria` existente funcionando (não interferir)

### 2. **Chaves SSH Isoladas** 
- Gerar novo par de chaves SSH específico para filadelfiastore
- Arquivo sugerido: `~/.ssh/filadelfia_store` 
- Configurar authorized_keys do usuário `filadelfia`
- **IMPORTANTE**: As duas aplicações devem coexistir (tutoria + filadelfia)

### 3. **Estrutura do Projeto Spring Boot**
O projeto filadelfiastore tem estrutura Maven típica:
```
filadelfiastore/
├── pom.xml (Spring Boot 3.4.0, Java 21)
├── src/
│   ├── main/
│   │   ├── java/com/filadelfia/
│   │   └── resources/
│   │   │   ├── application.properties (H2 dev)
│   │   │   ├── application-production.properties (MySQL)
│   │   │   ├── static/
│   │   │   └── templates/
│   └── test/
└── target/ (gerado no build)
```

**Configurações importantes:**
- **Java Version**: 21 (obrigatório - verificar no servidor)
- **Database**: MySQL em produção (`filadelfia_store_prod`)
- **Logs**: `/opt/filadelfia-store/production/logs/application.log`
- **Profile**: `production`
- **Port**: 8080

### 4. **Pipeline GitHub Actions**
- **Trigger**: Push na branch `develop` (não main)
- **Fluxo**: 
  1. Checkout código
  2. Setup Java 21 e Maven
  3. Executar testes (`mvn test`)
  4. Build da aplicação (`mvn clean package -DskipTests`)
  5. Deploy via SSH no usuário `filadelfia`

### 5. **Configuração no Servidor**
- **Diretório da aplicação**: `/var/www/filadelfiastore`
- **Diretório de backups**: `/var/www/backups/filadelfiastore`
- **Diretório de logs**: `/opt/filadelfia-store/production/logs/` (criar com permissões)
- **Porta da aplicação**: 8080 (Spring Boot padrão)
- **Profile**: `production`
- **Process Manager**: systemd service (não PM2, para diferir do Node.js)

### 6. **Deployment Script (Executar no Servidor)**
O script SSH deve:
- Fazer backup da versão atual
- Parar o serviço Spring Boot
- Substituir o JAR antigo pelo novo
- Configurar/atualizar application-production.properties
- Configurar variáveis de ambiente necessárias
- Iniciar o serviço
- Verificar se está respondendo na porta 8080
- Log de status final

### 7. **Configuração do Banco MySQL**
Criar banco de dados e usuário:
```sql
CREATE DATABASE filadelfia_store_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'filadelfia_prod_user'@'localhost' IDENTIFIED BY 'senha_segura';
GRANT ALL PRIVILEGES ON filadelfia_store_prod.* TO 'filadelfia_prod_user'@'localhost';
FLUSH PRIVILEGES;
```

Variáveis de ambiente obrigatórias:
- `DATABASE_URL`: jdbc:mysql://localhost:3306/filadelfia_store_prod
- `DATABASE_USERNAME`: filadelfia_prod_user  
- `DATABASE_PASSWORD`: senha_segura
- `DDL_AUTO`: validate (produção)

### 8. **GitHub Secrets Necessários**
Sugerir nomes de secrets específicos para evitar conflito:
- `FILADELFIA_VPS_HOST`
- `FILADELFIA_VPS_USER` (valor: filadelfia)
- `FILADELFIA_VPS_SSH_KEY`
- `FILADELFIA_VPS_PORT` (valor: 22)
- `FILADELFIA_DATABASE_PASSWORD` (senha do MySQL)
- `FILADELFIA_DATABASE_URL` (opcional, override padrão)

### 9. **Service Systemd**
Criar um serviço systemd para gerenciar a aplicação Spring Boot:
- Nome: `filadelfiastore.service`
- Localização: `/etc/systemd/system/`
- Auto-start no boot
- Restart automático em caso de falha

## 🔧 Deliverables Esperados

1. **Script de configuração do servidor** (bash)
2. **Arquivo workflow GitHub Actions** (.github/workflows/deploy-develop.yml)
3. **Arquivo de serviço systemd** (filadelfiastore.service)
4. **Scripts auxiliares**:
   - Geração de chaves SSH local (PowerShell para Windows)
   - Comandos para testar deploy
5. **Documentação** com instruções passo-a-passo

## 📌 Observações Importantes

- **Coexistência**: tutoria_escola (porta 3000, PM2) + filadelfiastore (porta 8080, systemd)
- **Java**: Verificar se JDK 21 está instalado, se não, incluir instalação (obrigatório)
- **MySQL**: Configurar MySQL Server se não estiver instalado
- **Maven**: Pode usar Maven wrapper (mvnw) se disponível
- **Segurança**: Usuários isolados, chaves separadas, diretórios próprios
- **Logs**: Configurar logs estruturados para debugging (já configurado em `/opt/filadelfia-store/production/logs/`)
- **Rollback**: Considerar estratégia de rollback rápido

## 🎯 Resultado Final
Ao final, deve ser possível:
1. Fazer push na branch `develop`
2. GitHub Actions faz deploy automático
3. Aplicação Spring Boot roda em `http://VPS_IP:8080`
4. Usuário `tutoria` e aplicação na porta 3000 continuam funcionando normalmente

## ❓ Perguntas para Considerar
- Configuração de banco de dados (H2, PostgreSQL, MySQL?)
- Configurações específicas de produção
- Monitoramento e health checks
- SSL/HTTPS (se necessário)
- Variáveis de ambiente sensíveis