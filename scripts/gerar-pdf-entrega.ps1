# Gera docs/Entrega-Sprint4-Animed.pdf a partir de docs/entrega-sprint4.html,
# com o link do YouTube preenchido. Usa o Microsoft Edge em modo headless.
#   .\scripts\gerar-pdf-entrega.ps1 -YouTube "https://youtu.be/xxxx"
param([Parameter(Mandatory = $true)][string]$YouTube)

$docs = Join-Path $PSScriptRoot '..\docs' | Resolve-Path
$html = [IO.File]::ReadAllText("$docs\entrega-sprint4.html", [Text.Encoding]::UTF8)
$link = "<a href=""$YouTube"">$YouTube</a>"
$tmp  = Join-Path $env:TEMP 'entrega-sprint4.html'
[IO.File]::WriteAllText($tmp, $html.Replace('LINK_DO_YOUTUBE', $link), (New-Object Text.UTF8Encoding $false))

$edge = "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe"
$pdf  = "$docs\Entrega-Sprint4-Animed.pdf"
& $edge --headless=new --disable-gpu --no-pdf-header-footer "--print-to-pdf=$pdf" $tmp | Out-Null
Write-Host "PDF gerado: $pdf"
