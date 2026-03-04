# 🚀 Deploy Automation - Filadelfia Store

Este diretório contém todos os arquivos necessários para configurar um sistema de deploy automático para o projeto Spring Boot **Filadelfia Store**.

## 📁 Estrutura de Arquivos

```
deploy/
├── 🔑 generate-ssh-key.ps1          # Gera chaves SSH no Windows
├── 🛠️ configure-vps.sh              # Configura servidor VPS completo
├── 🧪 test-deployment.sh            # Testa configuração do deployment
├── 📊 monitor.sh                    # Monitora aplicação em produção
├── ⚙️ setup-github-secrets.ps1     # Configura GitHub Secrets via CLI
├── 🔧 filadelfiastore.service       # Template do serviço systemd
├── 📚 DEPLOY_GUIDE.md               # Guia completo passo-a-passo
└── 📝 README.md                     # Este arquivo
```

## ⚡ Quick Start

### 1️⃣ **Gerar Chaves SSH (Windows)**
```powershell
# Execute como Administrador
.\generate-ssh-key.ps1
```

### 2️⃣ **Configurar Servidor VPS**  
```bash
# Upload script para o servidor
scp configure-vps.sh root@SEU_IP_VPS:/tmp/

# SSH no servidor e executar
ssh root@SEU_IP_VPS
sudo bash /tmp/configure-vps.sh
```

### 3️⃣ **Configurar GitHub Secrets**
```powershell
# Via PowerShell (requer GitHub CLI)
.\setup-github-secrets.ps1 -VpsHost "SEU_IP" -DatabasePassword "SENHA_MYSQL"

# Ou manualmente no GitHub:
# Settings → Secrets → Actions
```

### 4️⃣ **Primeiro Deploy**
```bash
# Push na branch develop
git checkout develop  
git push origin develop
```

### 5️⃣ **Monitorar Aplicação**
```bash
# No servidor VPS
./monitor.sh status
./monitor.sh logs
./monitor.sh follow
```

## 🔧 Scripts Principais

### 🔑 **generate-ssh-key.ps1**
- ✅ Gera par de chaves SSH específico
- ✅ Salva em `~/.ssh/filadelfia_store`
- ✅ Exibe chaves para copiar
- ✅ Configura SSH config local

### 🛠️ **configure-vps.sh** 
- ✅ Cria usuário `filadelfia` com sudo
- ✅ Instala Java 21 e MySQL
- ✅ Configura estrutura de diretórios
- ✅ Cria banco de dados MySQL  
- ✅ Configura serviço systemd
- ✅ Instala scripts de monitoramento

### 📊 **monitor.sh**
- ✅ Status completo da aplicação
- ✅ Logs em tempo real
- ✅ Start/Stop/Restart
- ✅ Verificação de saúde
- ✅ Monitoramento de recursos

## 🎯 Configuração Resultante

### **🔧 Infraestrutura**
- **Usuário**: `filadelfia` (isolado do tutoria_escola)
- **Diretórios**: 
  - App: `/var/www/filadelfiastore`
  - Backups: `/var/www/backups/filadelfiastore`  
  - Logs: `/opt/filadelfia-store/production/logs`
- **Serviço**: `filadelfiastore.service` (systemd)
- **Porta**: 8080 (Spring Boot)

### **🗄️ Banco de Dados**
- **Database**: `filadelfia_store_prod`
- **User**: `filadelfia_prod_user`
- **Engine**: MySQL 8.0+

### **🚀 Deploy Automático**
- **Trigger**: Push na branch `develop`
- **Pipeline**: Test → Build → Deploy
- **Rollback**: Backups automáticos
- **Monitoramento**: Health checks + logs

## 📊 GitHub Secrets Necessários

| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `FILADELFIA_VPS_HOST` | IP do servidor | `192.168.1.100` |
| `FILADELFIA_VPS_USER` | Usuário SSH | `filadelfia` |
| `FILADELFIA_VPS_SSH_KEY` | Chave privada | `-----BEGIN OPENSSH...` |
| `FILADELFIA_VPS_PORT` | Porta SSH | `22` |
| `FILADELFIA_DATABASE_PASSWORD` | Senha MySQL | `senha_gerada_pelo_script` |

## 🔍 Monitoramento

### **Comandos Úteis no Servidor:**
```bash
# Status completo
./monitor.sh status

# Logs recentes  
./monitor.sh logs

# Logs em tempo real
./monitor.sh follow

# Reiniciar aplicação
./monitor.sh restart

# Status systemd
sudo systemctl status filadelfiastore

# Logs do systemd
sudo journalctl -u filadelfiastore -f
```

### **URLs de Monitoramento:**
- 🌐 **App**: `http://SEU_IP:8080`
- ❤️ **Health**: `http://SEU_IP:8080/actuator/health`
- 📊 **Metrics**: `http://SEU_IP:8080/actuator/metrics`

## 🛡️ Segurança

### **✅ Implementada:**
- 🔐 Usuários isolados (filadelfia vs tutoria)
- 🗝️ Chaves SSH específicas
- 📁 Permissions restritivas
- 🔒 Configurações systemd seguras
- 🛡️ Firewall configurado

### **🔄 Recomendações:**
- 🔑 Rotacionar chaves SSH periodicamente  
- 📊 Implementar monitoramento avançado
- 🔒 Configurar SSL/HTTPS
- 📋 Backup automático do banco

## 🚨 Troubleshooting

### **❌ Deploy Falha:**
```bash
# Verificar GitHub Actions logs
# Testar conexão SSH
ssh filadelfia@SEU_IP_VPS

# Verificar logs
./monitor.sh logs
```

### **❌ Aplicação Não Inicia:**
```bash
# Verificar configuração
sudo systemctl status filadelfiastore
sudo journalctl -u filadelfiastore

# Testar JAR manualmente
cd /var/www/filadelfiastore
java -jar filadelfiastore.jar --spring.profiles.active=production
```

### **❌ Banco de Dados:**
```bash
# Testar conexão
mysql -u filadelfia_prod_user -p filadelfia_store_prod

# Verificar configuração
cat /etc/systemd/system/filadelfiastore.env
```

## 📚 Documentação Completa

Para instruções detalhadas passo-a-passo, consulte:
📖 **[DEPLOY_GUIDE.md](DEPLOY_GUIDE.md)**

## 🆘 Suporte

- 📋 **Logs detalhados**: `./monitor.sh logs`
- 🧪 **Teste configuração**: `./test-deployment.sh`  
- 📊 **Status completo**: `./monitor.sh status`
- 📄 **Config info**: `/root/filadelfiastore-setup.txt`

---

**🎉 Deploy configurado com sucesso!** 

Aplicação Filadelfia Store executando em coexistência com tutoria_escola, com deploy automático na branch `develop`.

### 🔗 URLs Importantes:
- 🟢 **Filadelfia Store**: `http://SEU_IP:8080`
- 🔵 **Tutoria Escola**: `http://SEU_IP:3000` 
- 🐙 **GitHub Actions**: `Actions` tab no repositório