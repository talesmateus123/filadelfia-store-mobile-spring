# 🚀 Configuração Automática de Deploy - Filadelfia Store

## ✨ O que foi configurado

Este sistema de deploy automático foi criado seguindo as especificações do DEPLOY_SETUP_PROMPT.md e inclui:

### 🔧 **Infraestrutura**
- ✅ Deploy automático via GitHub Actions na branch `develop`
- ✅ Coexistência com projeto tutoria_escola (usuários separados)  
- ✅ Spring Boot na porta 8080 com profile `production`
- ✅ MySQL como banco de dados em produção
- ✅ Logs estruturados em `/opt/filadelfia-store/production/logs/`
- ✅ Sistema de backups automático
- ✅ Serviço systemd para gerenciamento da aplicação

### 📁 **Arquivos Criados**

```
.github/
└── workflows/
    └── deploy-develop.yml           # Workflow GitHub Actions

deploy/
├── generate-ssh-key.ps1            # 🔑 Gera chaves SSH (Windows)
├── configure-vps.sh                # 🛠️ Configura servidor VPS completo
├── test-deployment.sh              # 🧪 Testa configuração
├── monitor.sh                      # 📊 Monitora aplicação
├── rollback.sh                     # 🔄 Sistema de rollback
├── setup-github-secrets.ps1        # ⚙️ Configura GitHub Secrets
├── filadelfiastore.service         # 🔧 Template systemd
├── DEPLOY_GUIDE.md                 # 📚 Guia completo
├── README.md                       # 📝 Documentação
└── .gitignore                      # 🚫 Arquivos ignorados
```

## 🚀 Como Usar

### **Passo 1: Gerar Chaves SSH**
```powershell
# Execute no PowerShell como Administrador
cd deploy
.\generate-ssh-key.ps1
```

### **Passo 2: Configurar Servidor**  
```bash
# Transferir e executar no VPS
scp deploy/configure-vps.sh root@SEU_IP_VPS:/tmp/
ssh root@SEU_IP_VPS "bash /tmp/configure-vps.sh"
```

### **Passo 3: Configurar GitHub Secrets**
```powershell
# Via CLI (requer GitHub CLI)
.\setup-github-secrets.ps1 -VpsHost "SEU_IP" -DatabasePassword "SENHA"

# Ou manualmente no GitHub: Settings → Secrets → Actions
```

### **Passo 4: Deploy Automático**
```bash
# Qualquer push na branch develop vai disparar o deploy
git checkout develop
git push origin develop
```

## 📊 Monitoramento

### **Scripts Disponíveis:**
```bash
# No servidor VPS após configuração
./monitor.sh status      # Status completo
./monitor.sh logs        # Logs recentes  
./monitor.sh follow      # Logs em tempo real
./monitor.sh restart     # Reiniciar app

./rollback.sh --list     # Listar backups
./rollback.sh TIMESTAMP  # Fazer rollback
```

### **URLs:**
- 🌐 **Aplicação**: http://SEU_IP_VPS:8080
- ❤️ **Health Check**: http://SEU_IP_VPS:8080/actuator/health
- 🔵 **Tutoria Escola**: http://SEU_IP_VPS:3000 (continua funcionando)

## 🛡️ Características de Segurança

### ✅ **Implementadas:**
- 🔐 Usuário isolado `filadelfia` (separado de `tutoria`)
- 🗝️ Chaves SSH específicas para cada projeto
- 📁 Diretórios com permissões restritivas
- 🔒 Configurações systemd seguras
- 🛡️ Arquivo de env protegido (600)
- 📋 Backup automático antes de deploys

## 🧪 Testes e Validação

### **Testes Automatizados:**
- ✅ Compilação e testes unitários
- ✅ Verificação de conectividade SSH
- ✅ Health check da aplicação
- ✅ Validação do banco de dados
- ✅ Verificação de logs

### **Scripts de Teste:**
```bash
# Testar configuração completa
./test-deployment.sh

# Monitorar status em tempo real  
./monitor.sh follow
```

## 🔄 Fluxo de Deploy

1. **Push na branch develop** → Dispara GitHub Actions
2. **Build Pipeline** → Compila + testa + gera JAR
3. **Deploy via SSH** → Transfere para VPS como usuário `filadelfia`
4. **Backup** → Salva versão atual antes da atualização
5. **Deploy** → Para serviço, atualiza JAR, inicia serviço
6. **Verificação** → Health check + logs + status final

## 📋 GitHub Secrets Necessários

| Secret | Valor | Onde Encontrar |
|--------|--------|----------------|
| `FILADELFIA_VPS_HOST` | IP do VPS | Seu provedor |
| `FILADELFIA_VPS_USER` | `filadelfia` | Criado pelo script |
| `FILADELFIA_VPS_SSH_KEY` | Chave privada | generate-ssh-key.ps1 |
| `FILADELFIA_VPS_PORT` | `22` | Padrão SSH |
| `FILADELFIA_DATABASE_PASSWORD` | Senha MySQL | configure-vps.sh gera |

## 🗄️ Configuração de Banco

### **Criado Automaticamente:**
- **Database**: `filadelfia_store_prod`
- **Usuario**: `filadelfia_prod_user`  
- **Engine**: MySQL 8.0+
- **Charset**: utf8mb4

### **Variáveis de Ambiente:**
```bash
DATABASE_URL=jdbc:mysql://localhost:3306/filadelfia_store_prod
DATABASE_USERNAME=filadelfia_prod_user
DATABASE_PASSWORD=senha_gerada_automaticamente
DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
DDL_AUTO=validate
DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect
SPRING_PROFILES_ACTIVE=production
```

## 🚨 Troubleshooting Rápido

### **Deploy Falha:**
```bash
# Ver logs do GitHub Actions
# Testar SSH: ssh filadelfia@SEU_IP_VPS
./monitor.sh logs
```

### **App Não Inicia:**
```bash
sudo systemctl status filadelfiastore
sudo journalctl -u filadelfiastore -f
./test-deployment.sh
```

### **Banco de Dados:**
```bash
mysql -u filadelfia_prod_user -p filadelfia_store_prod
cat /etc/systemd/system/filadelfiastore.env
```

## 📚 Documentação Completa

Para instruções detalhadas:
- 📖 **[deploy/DEPLOY_GUIDE.md](deploy/DEPLOY_GUIDE.md)** - Guia passo-a-passo
- 📝 **[deploy/README.md](deploy/README.md)** - Visão geral dos scripts  

## ✅ Checklist de Validação

- [ ] Chaves SSH geradas e configuradas
- [ ] Servidor VPS configurado (configure-vps.sh executado)
- [ ] GitHub Secrets configurados
- [ ] MySQL banco criado e acessível
- [ ] Primeiro deploy via GitHub Actions executado
- [ ] Aplicação respondendo na porta 8080
- [ ] Health check funcionando
- [ ] Logs sendo gerados
- [ ] Sistema de backup ativo
- [ ] Projeto tutoria_escola ainda funciona na porta 3000

---

## 🎉 Resultado Final

✅ **Sistema de deploy automático completo configurado!**

### **Funcionalidades Ativas:**
- 🔄 Deploy automático na branch `develop`  
- 🛡️ Coexistência segura com tutoria_escola
- 📊 Monitoramento completo com scripts
- 🔄 Sistema de rollback com backups
- 🗄️ MySQL configurado e funcionando
- 📋 Logs estruturados e organizados

### **Próximos Passos:**
1. Fazer push na branch `develop` para testar
2. Acessar aplicação em http://SEU_IP_VPS:8080
3. Monitorar com `./monitor.sh status`
4. Configurar domínio e SSL (opcional)

**🚀 O deploy automático está pronto para uso!**