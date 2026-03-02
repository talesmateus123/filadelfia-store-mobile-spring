# Script para gerar chave SSH especifica para filadelfiastore
# Execucao: PowerShell como administrador

Write-Host "`nGerando chave SSH para filadelfiastore..." -ForegroundColor Green

# Definir caminho da chave
$sshDir = "$env:USERPROFILE\.ssh"
$keyName = "filadelfia_store"
$keyPath = "$sshDir\$keyName"

# Criar diretorio .ssh se nao existir
if (!(Test-Path $sshDir)) {
    New-Item -ItemType Directory -Path $sshDir -Force
    Write-Host "Diretorio ~/.ssh criado" -ForegroundColor Yellow
}

# Verificar se a chave ja existe
if (Test-Path $keyPath) {
    Write-Host "`nChave SSH ja existe: $keyPath" -ForegroundColor Yellow
    $response = Read-Host "Deseja sobrescrever? (y/n)"
    if ($response -ne "y") {
        Write-Host "Operacao cancelada" -ForegroundColor Red
        exit 1
    }
}

# Gerar chave SSH
Write-Host "`nGerando par de chaves SSH..." -ForegroundColor Cyan
ssh-keygen -t ed25519 -C "deploy-filadelfiastore" -f $keyPath -N '""'

if ($LASTEXITCODE -eq 0) {
    Write-Host "Chave SSH gerada com sucesso!" -ForegroundColor Green
    
    # Exibir chave publica para copiar
    Write-Host "`n"
    Write-Host "=============== CHAVE PUBLICA (COPIE PARA O SERVIDOR) ===============" -ForegroundColor Green
    Get-Content "$keyPath.pub"
    Write-Host "=====================================================================" -ForegroundColor Green
    
    # Exibir chave privada para GitHub Secrets
    Write-Host "`n"
    Write-Host "=============== CHAVE PRIVADA (COPIE PARA GITHUB SECRETS) ===============" -ForegroundColor Blue
    Get-Content $keyPath
    Write-Host "=========================================================================" -ForegroundColor Blue
    
    Write-Host "`nPROXIMOS PASSOS:" -ForegroundColor Cyan
    Write-Host "1. Copie a chave PUBLICA e adicione no servidor VPS" -ForegroundColor White
    Write-Host "2. Copie a chave PRIVADA e adicione no GitHub em:" -ForegroundColor White
    Write-Host "   Settings > Secrets and variables > Actions" -ForegroundColor White
    Write-Host "   Nome do secret: FILADELFIA_VPS_SSH_KEY" -ForegroundColor White
    Write-Host "3. Execute o script de configuracao do servidor" -ForegroundColor White
    
} else {
    Write-Host "Erro ao gerar chave SSH" -ForegroundColor Red
    exit 1
}

# Configurar SSH config local (opcional)
$sshConfigPath = "$sshDir\config"
$configEntry = @"

# Filadelfia Store VPS
Host filadelfiastore-vps
    HostName SEU_IP_VPS_AQUI
    User filadelfia
    IdentityFile ~/.ssh/filadelfia_store
    Port 22
"@

if (!(Test-Path $sshConfigPath)) {
    $configEntry | Out-File -FilePath $sshConfigPath -Encoding UTF8
    Write-Host "`nConfiguracao SSH criada em ~/.ssh/config" -ForegroundColor Green
} else {
    Write-Host "`nPara facilitar, adicione isto ao seu ~/.ssh/config:" -ForegroundColor Yellow
    Write-Host $configEntry -ForegroundColor Gray
}

Write-Host "`nSetup de chave SSH concluido!" -ForegroundColor Green