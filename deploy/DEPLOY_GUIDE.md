# 🚀 Guia Completo de Deploy - Filadelfia Store

## 📋 Visão Geral

Este guia fornece instruções passo-a-passo para configurar um sistema de deploy automático para o projeto Spring Boot **Filadelfia Store** usando GitHub Actions.

### 🎯 Objetivos
- ✅ Deploy automático na branch `develop`
- ✅ Coexistência com projeto tutoria_escola
- ✅ Usuários e chaves SSH isolados
- ✅ Spring Boot na porta 8080
- ✅ Banco MySQL em produção
- ✅ Logs estruturados

---

## 🔧 Pré-requisitos

### No seu computador local:
- ✅ PowerShell (Windows)
- ✅ SSH client
- ✅ Acesso ao GitHub do projeto

### No servidor VPS:
- ✅ Ubuntu 20.04+ ou Debian 11+
- ✅ Acesso root (sudo)
- ✅ MySQL Server
- ✅ Portas 22 (SSH) e 8080 (aplicação) abertas

---

## 📝 Passo 1: Gerar Chaves SSH Localmente

Execute no PowerShell como **Administrador**:

```powershell
cd "D:\Users\Tales\Documents\projects\filadelfiastore\deploy"
.\generate-ssh-key.ps1
```

### O que o script faz:
- 🔑 Gera par de chaves SSH específico para filladelfiastore
- 📁 Salva em `~/.ssh/filadelfia_store`
- 📋 Exibe chave pública para o servidor
- 🔐 Exibe chave privada para GitHub Secrets

### ⚠️ **IMPORTANTE**: 
- Copie a **chave pública** (será usada no servidor)
- Copie a **chave privada** (será usada no GitHub)

---

## 📝 Passo 2: Configurar Servidor VPS

### 2.1 Upload do Script
```bash
# No seu computador local
scp deploy/configure-vps.sh root@SEU_IP_VPS:/tmp/
```

### 2.2 Executar Configuração
```bash
# Conectar no VPS como root
ssh root@SEU_IP_VPS

# Executar configuração
sudo bash /tmp/configure-vps.sh
```

### O que o script faz:
- 👤 Cria usuário `filadelfia` com sudo
- ☕ Instala Java 21 (OpenJDK)
- 📁 Cria estrutura de diretórios
- 🗄️ Configura MySQL e banco de dados
- ⚙️ Cria serviço systemd
- 🔒 Configura segurança e permissões

### 2.3 Configurar Chave SSH
```bash
# Conectar como usuário filadelfia (criar se necessário)
sudo -u filadelfia bash
cd ~
mkdir -p .ssh
chmod 700 .ssh

# Adicionar chave pública (cole a chave gerada no Passo 1)
nano .ssh/authorized_keys
# Cole aqui a chave pública
chmod 600 .ssh/authorized_keys
```

---

## 📝 Passo 3: Configurar GitHub Secrets

Acesse: `GitHub → Settings → Secrets and variables → Actions`

### Adicionar os seguintes secrets:

| Secret | Valor | Descrição |
|--------|-------|-----------|
| `FILADELFIA_VPS_HOST` | `SEU_IP_VPS` | IP do servidor VPS |
| `FILADELFIA_VPS_USER` | `filadelfia` | Usuário para deploy |
| `FILADELFIA_VPS_SSH_KEY` | `chave_privada_completa` | Chave privada (gerada no Passo 1) |
| `FILADELFIA_VPS_PORT` | `22` | Porta SSH |
| `FILADELFIA_DATABASE_PASSWORD` | `senha_gerada` | Senha do MySQL (veja logs do script) |

### 📋 **Como encontrar a senha do banco:**
```bash
# No VPS, visualize o arquivo de configuração
sudo cat /root/filadelfiastore-setup.txt
```

---

## 📝 Passo 4: Testar Deploy

### 4.1 Teste Manual no Servidor
```bash
# Conectar no VPS como usuário filadelfia
ssh filadelfia@SEU_IP_VPS

# Executar testes
bash /tmp/test-deployment.sh
```

### 4.2 Primeiro Deploy via GitHub
```bash
# No seu projeto local
git checkout develop
git add .
git commit -m "chore: setup deploy automation"
git push origin develop
```

### 📊 **Monitorar Deploy:**
- GitHub Actions: `Actions` tab no repositório
- Logs no servidor: `sudo journalctl -u filadelfiastore`

---

## 📝 Passo 5: Verificar Funcionamento

### 5.1 Status do Serviço
```bash
# No VPS
sudo systemctl status filadelfiastore
```

### 5.2 Logs da Aplicação
```bash
# Logs do systemd
sudo journalctl -u filadelfiastore -f

# Logs da aplicação
tail -f /opt/filadelfia-store/production/logs/application.log
```

### 5.3 Teste HTTP
```bash
# Health check
curl http://localhost:8080/actuator/health

# Página principal
curl http://SEU_IP_VPS:8080
```

### 5.4 Scripts de Monitoramento
```bash
# Status completo
filadelfia-status

# Restart rápido
filadelfia-restart
```

---

## 🛠️ Comandos Úteis

### 🔄 **Reiniciar Aplicação**
```bash
sudo systemctl restart filadelfiastore
```

### 📋 **Ver Logs em Tempo Real**
```bash
sudo journalctl -u filadelfiastore -f
```

### 🗃️ **Gerenciar Backups**
```bash
# Listar backups
ls -la /var/www/backups/filadelfiastore/

# Restaurar backup
sudo systemctl stop filadelfiastore
cp /var/www/backups/filadelfiastore/filadelfiastore_backup_TIMESTAMP.jar /var/www/filadelfiastore/filadelfiastore.jar
sudo systemctl start filadelfiastore
```

### 📊 **Monitorar Recursos**
```bash
# Uso de memória
ps aux | grep filadelfia

# Porta 8080
sudo netstat -tuln | grep :8080
```

---

## 🚨 Troubleshooting

### ❌ **Deploy Falha**
1. Verifique GitHub Secrets
2. Teste conexão SSH: `ssh filadelfia@SEU_IP_VPS`
3. Verifique logs: `sudo journalctl -u filadelfiastore`

### ❌ **Aplicação Não Inicia**
1. Verificar Java: `java -version`
2. Verificar banco: `mysql -u filadelfia_prod_user -p`
3. Verificar configuração: `/etc/systemd/system/filadelfiastore.env`

### ❌ **Erro de Conexão com Banco**
1. Verificar MySQL: `sudo systemctl status mysql`
2. Testar credenciais: `mysql -u filadelfia_prod_user -p filadelfia_store_prod`
3. Verificar variáveis: `cat /etc/systemd/system/filadelfiastore.env`

### ❌ **Porta 8080 Não Responde**
1. Verificar serviço: `sudo systemctl status filadelfiastore`
2. Verificar firewall: `sudo ufw status`
3. Verificar processo: `sudo netstat -tuln | grep :8080`

---

## 🔒 Considerações de Segurança

### ✅ **Implementadas:**
- 🔐 Usuários separados (filadelfia vs tutoria)
- 🗝️ Chaves SSH isoladas
- 📁 Diretórios com permissões restritivas
- 🔒 Arquivo de configuração protegido
- 🛡️ Configurações systemd seguras

### 🔄 **Recomendações Adicionais:**
- 🔄 Rotacionar chaves SSH periodicamente
- 📊 Configurar monitoramento (Prometheus/Grafana)
- 🔒 Implementar SSL/HTTPS com Let's Encrypt
- 📋 Backup automático do banco de dados

---

## 📚 Arquivos Importantes

### **No Projeto:**
- `.github/workflows/deploy-develop.yml` - GitHub Actions workflow
- `deploy/configure-vps.sh` - Script de configuração do servidor
- `deploy/generate-ssh-key.ps1` - Geração de chaves SSH
- `deploy/test-deployment.sh` - Testes de validação

### **No Servidor:**
- `/etc/systemd/system/filadelfiastore.service` - Serviço systemd
- `/etc/systemd/system/filadelfiastore.env` - Variáveis de ambiente
- `/var/www/filadelfiastore/` - Aplicação
- `/var/www/backups/filadelfiastore/` - Backups
- `/opt/filadelfia-store/production/logs/` - Logs

---

## 🎯 URLs e Endpoints

### **Aplicação:**
- 🌐 **Principal**: `http://SEU_IP_VPS:8080`
- ❤️ **Health Check**: `http://SEU_IP_VPS:8080/actuator/health`
- 📊 **Metrics**: `http://SEU_IP_VPS:8080/actuator/metrics`

### **Coexistência:**
- 🔵 **Tutoria Escola**: `http://SEU_IP_VPS:3000`
- 🟢 **Filadelfia Store**: `http://SEU_IP_VPS:8080`

---

## ✅ Checklist Final

- [ ] Chaves SSH geradas e configuradas
- [ ] VPS configurado com script
- [ ] GitHub Secrets configurados  
- [ ] MySQL banco criado e testado
- [ ] Primeiro deploy executado com sucesso
- [ ] Aplicação respondendo na porta 8080
- [ ] Logs sendo gerados corretamente
- [ ] Backup funcionando
- [ ] Scripts de monitoramento testados
- [ ] Coexistência com tutoria_escola verificada

---

## 🆘 Suporte

### **Para problemas:**
1. 📋 Verifique logs: `sudo journalctl -u filadelfiastore`
2. 🧪 Execute testes: `bash test-deployment.sh`
3. 📊 Verifique status: `filadelfia-status`
4. 📄 Consulte configuração: `/root/filadelfiastore-setup.txt`

### **Contatos:**
- 📧 GitHub Issues no repositório
- 📄 Documentação do projeto
- 🔧 Logs detalhados para debug

---

**🎉 Deploy concluído com sucesso!** 

A aplicação Filadelfia Store está agora configurada para deploy automático e funcionando em coexistência com o projeto tutoria_escola.