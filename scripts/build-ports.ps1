param(
    [ValidateSet('all','original','26.2-fabric')]
    [string[]]$Target = @('all'),
    [switch]$Validate
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$targets = [ordered]@{
    'original' = @{ Java=21; Directory='.'; Gate='runGameTestServer' }
    '26.2-fabric' = @{ Java=25; Directory='ports/26.2-fabric'; Gate='runSmokeServer' }
}
function Find-Jdk([int]$major) {
    $candidates = @([Environment]::GetEnvironmentVariable("JAVA${major}_HOME"), $env:JAVA_HOME)
    $toolchains = Join-Path $env:USERPROFILE '.gradle/jdks'
    if (Test-Path -LiteralPath $toolchains) {
        $candidates += Get-ChildItem -LiteralPath $toolchains -Directory | Select-Object -ExpandProperty FullName
    }
    foreach ($candidate in $candidates) {
        if (-not $candidate -or -not (Test-Path -LiteralPath (Join-Path $candidate 'release'))) { continue }
        $versionLine = Get-Content -LiteralPath (Join-Path $candidate 'release') | Where-Object { $_ -match '^JAVA_VERSION=' }
        if ($versionLine -match ('^JAVA_VERSION="' + $major + '[.+"]') -and
            (Test-Path -LiteralPath (Join-Path $candidate 'bin/java.exe'))) { return $candidate }
    }
    throw "JDK $major is required. Set JAVA${major}_HOME to its installation directory."
}
$selected = if ($Target -contains 'all') { @($targets.Keys) } else { $Target }
$output = Join-Path $repoRoot 'build/ports'
New-Item -ItemType Directory -Force -Path $output | Out-Null
$originalJavaHome = $env:JAVA_HOME
$originalCi = $env:CI
try {
    foreach ($name in $selected) {
        $entry = $targets[$name]
        $env:JAVA_HOME = Find-Jdk $entry.Java
        # Loom skips IDE dependency-source remapping in CI; builds and tests still run.
        $env:CI = if ($name -like '*-fabric') { 'true' } else { $originalCi }
        $directory = Join-Path $repoRoot $entry.Directory
        Push-Location $directory
        try {
            $arguments = @('build','--console=plain','--max-workers=2')
            if ($Validate) { $arguments += $entry.Gate }
            & (Join-Path $directory 'gradlew.bat') @arguments
            if ($LASTEXITCODE -ne 0) { throw "Build failed: $name" }
            $jars = @(Get-ChildItem -LiteralPath (Join-Path $directory 'build/libs') -Filter '*.jar' |
                Where-Object { $_.Name -notmatch '-(sources|javadoc|slim|dev)\.jar$' })
            $releaseVersion = (Get-Content -LiteralPath (Join-Path $directory 'gradle.properties') |
                Where-Object { $_ -match '^mod_version=' }) -replace '^mod_version=', ''
            if (-not $releaseVersion) {
                $versionLine = Get-Content -LiteralPath (Join-Path $directory 'build.gradle') |
                    Where-Object { $_ -match "^version = '([^']+)'" }
                if ($versionLine -match "^version = '([^']+)'") { $releaseVersion = $Matches[1] }
            }
            if (-not $releaseVersion) { throw "Cannot find release version for $name" }
            $releasePattern = '-' + [regex]::Escape($releaseVersion.Trim()) + '(?:\+mc[^/]+)?\.jar$'
            $jars = @($jars | Where-Object { $_.Name -match $releasePattern })
            if ($jars.Count -ne 1) { throw "Expected one release JAR for $name; found $($jars.Count). Clean that target's build outputs first." }
            $outputName = if ($name -eq 'original') { $jars[0].BaseName + '-neoforge-1.21.1.jar' } else { $jars[0].Name }
            Copy-Item -LiteralPath $jars[0].FullName -Destination (Join-Path $output $outputName) -Force
        } finally { Pop-Location }
    }
} finally {
    $env:JAVA_HOME = $originalJavaHome
    $env:CI = $originalCi
}
Get-ChildItem -LiteralPath $output -Filter '*.jar' | Get-FileHash -Algorithm SHA256 |
    ForEach-Object { "$($_.Hash)  $(Split-Path $_.Path -Leaf)" } |
    Set-Content -LiteralPath (Join-Path $output 'SHA256SUMS.txt')
Write-Output "Installable JARs: $output"
