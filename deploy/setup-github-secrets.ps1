# 📝 Script para configurar GitHub Secrets via CLI
# Requisito: GitHub CLI instalado (gh auth login)

param(
    [Parameter(Mandatory=$true)]
    [string]$VpsHost,
    
    [Parameter(Mandatory=$true)]
    [string]$DatabasePassword,
    
    [string]$VpsUser = "filadelfia",
    [string]$VpsPort = "22",
    [string]$SshKeyPath = "$env:USERPROFILE\.ssh\filadelfia_store"
)

Write-Host "🔧 Configurando GitHub Secrets..." -ForegroundColor Green

# Verificar se GitHub CLI está instalado
if (-not (Get-Command "gh" -ErrorAction SilentlyContinue)) {
    Write-Host "❌ GitHub CLI não encontrado!" -ForegroundColor Red
    Write-Host "📥 Instale: https://cli.github.com/" -ForegroundColor Yellow
    exit 1
}

# Verificar se está autenticado
$authStatus = gh auth status 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Não autenticado no GitHub CLI" -ForegroundColor Red
    Write-Host "🔐 Execute: gh auth login" -ForegroundColor Yellow
    exit 1
}

# Verificar se a chave SSH existe
if (-not (Test-Path $SshKeyPath)) {
    Write-Host "❌ Chave SSH não encontrada: $SshKeyPath" -ForegroundColor Red
    Write-Host "🔑 Execute primeiro: ./generate-ssh-key.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ GitHub CLI configurado" -ForegroundColor Green
Write-Host "✅ Chave SSH encontrada" -ForegroundColor Green

# Ler chave privada
$privateKey = Get-Content $SshKeyPath -Raw

# Configurar secrets
$secrets = @{
    "FILADELFIA_VPS_HOST" = $VpsHost
    "FILADELFIA_VPS_USER" = $VpsUser  
    "FILADELFIA_VPS_SSH_KEY" = $privateKey
    "FILADELFIA_VPS_PORT" = $VpsPort
    "FILADELFIA_DATABASE_PASSWORD" = $DatabasePassword
}

Write-Host "`n🔐 Configurando secrets..." -ForegroundColor Cyan

foreach ($secret in $secrets.GetEnumerator()) {
    $name = $secret.Key
    $value = $secret.Value
    
    try {
        # Configurar secret via GitHub CLI
        $value | gh secret set $name
        Write-Host "✅ $name" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Erro ao configurar $name" -ForegroundColor Red
        Write-Host "   $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host "`n📋 Listando secrets configurados..." -ForegroundColor Cyan
gh secret list

Write-Host "`n🎉 GitHub Secrets configurados com sucesso!" -ForegroundColor Green
Write-Host "`n📝 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host "1. Verifique se todos os secrets foram criados" -ForegroundColor White
Write-Host "2. Configure o servidor VPS com ./configure-vps.sh" -ForegroundColor White  
Write-Host "3. Faça push na branch develop para testar" -ForegroundColor White