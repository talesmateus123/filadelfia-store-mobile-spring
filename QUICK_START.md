# ⚡ Quick Start - Deploy Filadelfia Store

## 🎯 Resumo Executivo

Sistema de deploy automático para **Filadelfia Store** (Spring Boot + MySQL) configurado para coexistir com tutoria_escola na mesma VPS.

### 📊 **Configuração Final:**
- ✅ **Aplicação**: Spring Boot 3.4.0 + Java 21 + MySQL
- ✅ **Deploy**: Automático via GitHub Actions (branch develop)
- ✅ **Porta**: 8080 (filadelfia) | 3000 (tutoria) 
- ✅ **Usuários**: `filadelfia` (novo) | `tutoria` (existente)
- ✅ **Chaves SSH**: Isoladas e específicas para cada projeto

---

## ⚡ Execução Rápida (5 Passos)

### **1️⃣ Gerar SSH (2 minutos)**
```powershell
# PowerShell como Admin
cd "D:\Users\Tales\Documents\projects\filadelfiastore\deploy"
.\generate-ssh-key.ps1
```
> ✅ Copie as chaves exibidas (pública para servidor, privada para GitHub)

### **2️⃣ Configurar VPS (5 minutos)**
```bash
# Upload script
scp deploy/configure-vps.sh root@SEU_IP_VPS:/tmp/

# SSH e executar
ssh root@SEU_IP_VPS "bash /tmp/configure-vps.sh"
```
> ✅ Anote a senha do MySQL gerada

### **3️⃣ Configurar SSH no VPS (1 minuto)**
```bash
ssh root@SEU_IP_VPS
sudo -u filadelfia bash
echo "SUA_CHAVE_PUBLICA_AQUI" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

### **4️⃣ GitHub Secrets (2 minutos)**
```powershell
# Via CLI (requer: gh auth login)
.\setup-github-secrets.ps1 -VpsHost "SEU_IP" -DatabasePassword "SENHA_MYSQL"

# Ou Manual: GitHub → Settings → Secrets → Actions
```

| Secret | Valor |
|---------|--------|
| FILADELFIA_VPS_HOST | SEU_IP_VPS |
| FILADELFIA_VPS_USER | filadelfia |
| FILADELFIA_VPS_SSH_KEY | [chave privada completa] |
| FILADELFIA_VPS_PORT | 22 |
| FILADELFIA_DATABASE_PASSWORD | [senha MySQL] |

### **5️⃣ Primeiro Deploy (1 minuto)**
```bash
git checkout develop
git add .
git commit -m "chore: setup deploy automation"  
git push origin develop
```
> 🚀 GitHub Actions executará automaticamente

---

## 📊 Verificação Rápida

### **✅ Funcionando:**
```bash
# No VPS
curl http://localhost:8080                    # App responde
sudo systemctl status filadelfiastore        # Serviço ativo
./monitor.sh status                          # Status completo
```

### **🌐 URLs:**
- **Filadelfia Store**: http://SEU_IP:8080
- **Tutoria Escola**: http://SEU_IP:3000 ✅ (mantém funcionando)

---

## 🛠️ Comandos Essenciais

```bash
# === NO VPS (após configuração) ===
./monitor.sh status      # Status completo
./monitor.sh restart     # Reiniciar app
./monitor.sh logs        # Ver logs
./monitor.sh follow      # Logs tempo real

./rollback.sh --list     # Listar backups  
./rollback.sh TIMESTAMP  # Fazer rollback

# === MANUTENÇÃO SYSTEMD ===
sudo systemctl status filadelfiastore     # Status serviço
sudo systemctl restart filadelfiastore    # Restart manual
sudo journalctl -u filadelfiastore -f     # Logs systemd

# === TROUBLESHOOTING ===
./test-deployment.sh     # Testar configuração
mysql -u filadelfia_prod_user -p          # Testar banco
curl http://localhost:8080/actuator/health # Health check
```

---

## 🚨 Resolução Rápida de Problemas

| Problema | Comando |
|----------|---------|
| **Deploy falha** | Ver GitHub Actions → verificar Secrets → `ssh filadelfia@IP` |
| **App não inicia** | `sudo journalctl -u filadelfiastore -f` |
| **Banco erro** | `mysql -u filadelfia_prod_user -p` + verificar env |
| **Porta não responde** | `sudo netstat -tuln \| grep 8080` + firewall |
| **Logs cheios** | Logrotate configurado (automático) |

---

## 📋 Arquivos Importantes

### **Scripts Criados:**
```
deploy/
├── generate-ssh-key.ps1        # 🔑 Gerar chaves SSH
├── configure-vps.sh            # 🛠️ Configurar servidor  
├── setup-github-secrets.ps1    # ⚙️ Configurar secrets
├── monitor.sh                  # 📊 Monitorar app
├── rollback.sh                 # 🔄 Sistema rollback
├── test-deployment.sh          # 🧪 Testar config
└── DEPLOY_GUIDE.md             # 📚 Guia completo
```

### **Configuração Produção:**
```
/var/www/filadelfiastore/             # App directory  
/var/www/backups/filadelfiastore/     # Backups automáticos
/opt/filadelfia-store/production/logs/ # Logs estruturados
/etc/systemd/system/filadelfiastore.service # Serviço
/etc/systemd/system/filadelfiastore.env     # Variáveis
```

---

## 🎯 Status Final

### ✅ **Sistema Ativo:**
- 🔄 Deploy automático (branch develop)
- 🛡️ Segurança isolada (usuários + chaves separados)
- 📊 Monitoramento completo 
- 🔄 Rollback automático
- 🗄️ MySQL produção configurado
- 📋 Logs estruturados
- 🚀 Coexistência com tutoria_escola

### 🚀 **Ready to Deploy!**

**Qualquer push na branch `develop` → deploy automático ativado!**

Para documentação completa: [deploy/DEPLOY_GUIDE.md](deploy/DEPLOY_GUIDE.md)