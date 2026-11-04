# Pratica 4 - Gera trafego na aplicacao para popular o Prometheus/Grafana.
# Uso (PowerShell, com a app rodando em localhost:8081):
#   .\observabilidade\gerar-carga.ps1            # 3 minutos
#   .\observabilidade\gerar-carga.ps1 -Minutos 5
param([int]$Minutos = 3)

$base = "http://localhost:8081"
$fim = (Get-Date).AddMinutes($Minutos)
$i = 0

function Post($url) {
    try { Invoke-RestMethod -Method Post -Uri $url | Out-Null; return $true }
    catch { return $false }
}

Write-Host "Gerando carga por $Minutos minuto(s)... (Ctrl+C para parar)"
while ((Get-Date) -lt $fim) {
    $i++
    # Fase com falhas ao salvar entre as iteracoes 20 e 35 (aparece no painel de falhas)
    if ($i -eq 20) { Post "$base/simulacao/falhas?probabilidade=0.4" | Out-Null; Write-Host ">> Falhas ao salvar LIGADAS (40%)" }
    if ($i -eq 35) { Post "$base/simulacao/falhas?probabilidade=0"   | Out-Null; Write-Host ">> Falhas ao salvar DESLIGADAS" }

    $qtd = Get-Random -Minimum 2 -Maximum 8
    $semResultado = if ((Get-Random -Maximum 10) -eq 0) { 1 } else { 0 }   # ~10% com SMS falhando
    Post "$base/simulacao/jogos?quantidade=$qtd&semResultado=$semResultado" | Out-Null
    $ok = Post "$base/jogos/finalizar"
    Invoke-RestMethod -Uri "$base/jogos" | Out-Null

    Write-Host ("[{0}] criados={1} finalizar={2}" -f $i, $qtd, $(if ($ok) { "OK" } else { "ERRO" }))
    Start-Sleep -Milliseconds 1500
}
Post "$base/simulacao/falhas?probabilidade=0" | Out-Null
Write-Host "Fim."
