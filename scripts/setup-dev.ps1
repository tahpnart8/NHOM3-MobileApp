# One time setup per clone (Windows). Same as scripts/setup-dev.sh, using the sh that ships with Git for Windows.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$git = (Get-Command git -ErrorAction Stop).Source
$gitRoot = Split-Path -Parent (Split-Path -Parent $git)
$sh = Join-Path $gitRoot 'bin\sh.exe'
if (-not (Test-Path $sh)) { $sh = Join-Path $gitRoot 'usr\bin\sh.exe' }
if (-not (Test-Path $sh)) { throw "Cannot find sh.exe under $gitRoot. Install Git for Windows." }

& $sh (Join-Path $root 'scripts/setup-dev.sh')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
