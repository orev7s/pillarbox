param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$env:JAVA_HOME = "C:\Users\v80q\jdk-21\jdk-21.0.12.1+1"
$env:ANDROID_HOME = "C:\Users\v80q\android-sdk"
$gradle = "C:\Users\v80q\gradle-8.11.1\bin\gradle.bat"
$version = "1.0.0"

function Convert-ToLf([string]$path) {
    $bytes = [System.IO.File]::ReadAllBytes($path)
    $out = New-Object System.Collections.Generic.List[byte]
    for ($i = 0; $i -lt $bytes.Length; $i++) {
        if ($bytes[$i] -eq 13 -and ($i + 1) -lt $bytes.Length -and $bytes[$i + 1] -eq 10) { continue }
        $out.Add($bytes[$i])
    }
    [System.IO.File]::WriteAllBytes($path, $out.ToArray())
}

Write-Host "== normalise line endings"
Get-ChildItem "$root\module" -Recurse -File | Where-Object {
    $_.Extension -in ".sh", ".prop", ".txt" -or $_.Name -eq "update-binary" -or $_.Name -eq "updater-script" -or $_.Name -eq "pillarboxd"
} | ForEach-Object { Convert-ToLf $_.FullName }

Write-Host "== stage engine asset"
$assets = "$root\app\src\main\assets\engine"
New-Item -ItemType Directory -Force -Path $assets | Out-Null
Copy-Item "$root\module\bin\pillarboxd" "$assets\pillarboxd" -Force

if (-not $SkipBuild) {
    Write-Host "== gradle assembleRelease"
    & $gradle -p $root :app:assembleRelease --console=plain --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "gradle failed with exit code $LASTEXITCODE" }
}

$apk = "$root\app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apk)) { throw "apk not found at $apk" }

$dist = "$root\dist"
New-Item -ItemType Directory -Force -Path $dist | Out-Null
$apkOut = "$dist\PillarBox-$version.apk"
Copy-Item $apk $apkOut -Force

Write-Host "== packaging module zip"
$stage = "$root\build\module-stage"
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
Copy-Item "$root\module" $stage -Recurse
$zipOut = "$dist\PillarBox-Module-v$version.zip"
if (Test-Path $zipOut) { Remove-Item $zipOut -Force }
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($zipOut, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    Get-ChildItem $stage -Recurse -File | ForEach-Object {
        $rel = $_.FullName.Substring($stage.Length + 1).Replace('\', '/')
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
            $zip, $_.FullName, $rel, [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally {
    $zip.Dispose()
}

Write-Host "== done"
Get-ChildItem $dist | Select-Object Name, Length
