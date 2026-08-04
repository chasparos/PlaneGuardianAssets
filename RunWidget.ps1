param(
    [Parameter(Mandatory = $false)]
    [string]$RepositoryLabel
)

$ErrorActionPreference = "Stop"

function Resolve-RepositoryRoot {
    param([string]$ScriptPath)

    return (Split-Path -Parent $ScriptPath)
}

function Test-JavaHome {
    param([string]$Candidate)

    if ([string]::IsNullOrWhiteSpace($Candidate)) {
        return $false
    }

    return (Test-Path -LiteralPath (Join-Path $Candidate "bin\java.exe") -PathType Leaf) -and
        (Test-Path -LiteralPath (Join-Path $Candidate "bin\javac.exe") -PathType Leaf)
}

function New-JavaResolution {
    param(
        [string]$Source,
        [string]$JavaHome
    )

    if (-not (Test-JavaHome $JavaHome)) {
        return $null
    }

    return [pscustomobject]@{
        Source = $Source
        JavaHome = (Resolve-Path -LiteralPath $JavaHome).Path
    }
}

function Resolve-JavaHomeFromEnvironment {
    return New-JavaResolution -Source "JAVA_HOME" -JavaHome $env:JAVA_HOME
}

function Resolve-IntelliJProjectJdkName {
    param([string]$Root)

    $miscXml = Join-Path $Root ".idea\misc.xml"
    if (-not (Test-Path -LiteralPath $miscXml -PathType Leaf)) {
        return $null
    }

    try {
        [xml]$document = Get-Content -LiteralPath $miscXml -Raw
        $component = $document.project.component |
            Where-Object { $_.name -eq "ProjectRootManager" } |
            Select-Object -First 1
        return $component.'project-jdk-name'
    } catch {
        Write-Warning ("Could not read IntelliJ project SDK metadata from {0}: {1}" -f $miscXml, $_.Exception.Message)
        return $null
    }
}

function Get-IntelliJTableCandidates {
    if ([string]::IsNullOrWhiteSpace($env:APPDATA)) {
        return @()
    }

    return @(
        (Join-Path $env:APPDATA "JetBrains\IntelliJIdea*\options\jdk.table.xml"),
        (Join-Path $env:APPDATA "JetBrains\IdeaIC*\options\jdk.table.xml")
    )
}

function Resolve-JavaHomeFromIntelliJ {
    param([string]$Root)

    $jdkName = Resolve-IntelliJProjectJdkName $Root
    if ([string]::IsNullOrWhiteSpace($jdkName)) {
        return $null
    }

    foreach ($pattern in (Get-IntelliJTableCandidates)) {
        foreach ($table in (Get-ChildItem -Path $pattern -File -ErrorAction SilentlyContinue)) {
            try {
                [xml]$document = Get-Content -LiteralPath $table.FullName -Raw
                foreach ($jdk in $document.application.component.jdk) {
                    $name = ($jdk.name.option | Where-Object { $_.name -eq "name" }).value
                    if ($name -ne $jdkName) {
                        continue
                    }

                    $home2 = ($jdk.homePath.option | Where-Object { $_.name -eq "homePath" }).value
                    if ([string]::IsNullOrWhiteSpace($home2)) {
                        continue
                    }

                    $expanded = [Environment]::ExpandEnvironmentVariables($home.Replace('$USER_HOME$', $HOME))
                    $resolution = New-JavaResolution -Source "IntelliJ project SDK '$jdkName'" -JavaHome $expanded
                    if ($resolution) {
                        return $resolution
                    }
                }
            } catch {
                Write-Warning ("Could not read IntelliJ SDK table {0}: {1}" -f $table.FullName, $_.Exception.Message)
            }
        }
    }

    return $null
}

function Resolve-JavaHomeFromPath {
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($null -eq $javaCommand -or [string]::IsNullOrWhiteSpace($javaCommand.Source)) {
        return $null
    }

    $candidate = Split-Path -Parent (Split-Path -Parent $javaCommand.Source)
    return New-JavaResolution -Source "PATH" -JavaHome $candidate
}

function Get-CommonJdkRoots {
    $roots = @()

    if (-not [string]::IsNullOrWhiteSpace($HOME)) {
        $roots += (Join-Path $HOME ".jdks")
    }
    if (-not [string]::IsNullOrWhiteSpace($env:ProgramFiles)) {
        $roots += (Join-Path $env:ProgramFiles "Java")
        $roots += (Join-Path $env:ProgramFiles "Eclipse Adoptium")
    }
    if (-not [string]::IsNullOrWhiteSpace(${env:ProgramFiles(x86)})) {
        $roots += (Join-Path ${env:ProgramFiles(x86)} "Java")
    }

    return $roots | Where-Object { Test-Path -LiteralPath $_ -PathType Container }
}

function Resolve-JavaHomeFromCommonLocations {
    param([string]$PreferredName)

    $candidates = foreach ($root in (Get-CommonJdkRoots)) {
        Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue
    }

    if (-not [string]::IsNullOrWhiteSpace($PreferredName)) {
        foreach ($candidate in ($candidates |
                Where-Object { $_.Name -eq $PreferredName -or $_.Name -like "*$PreferredName*" } |
                Sort-Object LastWriteTime -Descending)) {
            $resolution = New-JavaResolution -Source "common installation matching IntelliJ SDK '$PreferredName'" -JavaHome $candidate.FullName
            if ($resolution) {
                return $resolution
            }
        }
    }

    foreach ($candidate in ($candidates | Sort-Object LastWriteTime -Descending)) {
        $resolution = New-JavaResolution -Source "common JDK installation" -JavaHome $candidate.FullName
        if ($resolution) {
            return $resolution
        }
    }

    return $null
}

function Resolve-JavaHome {
    param([string]$Root)

    $resolution = Resolve-JavaHomeFromEnvironment
    if ($resolution) {
        return $resolution
    }

    $resolution = Resolve-JavaHomeFromIntelliJ $Root
    if ($resolution) {
        return $resolution
    }

    $resolution = Resolve-JavaHomeFromPath
    if ($resolution) {
        return $resolution
    }

    $preferredName = Resolve-IntelliJProjectJdkName $Root
    return Resolve-JavaHomeFromCommonLocations $preferredName
}

function Initialize-JavaEnvironment {
    param([pscustomobject]$Resolution)

    $env:JAVA_HOME = $Resolution.JavaHome
    $javaBin = Join-Path $Resolution.JavaHome "bin"
    $pathEntries = $env:PATH -split ';' | Where-Object { $_ -and $_ -ne $javaBin }
    $env:PATH = (@($javaBin) + $pathEntries) -join ';'
}

function Show-JavaSetupInstructions {
    Write-Error @"
No usable JDK could be found for this repository.

Set JAVA_HOME to a JDK installation before running the widget, for example:
  `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
  `$env:PATH = "`$env:JAVA_HOME\bin;`$env:PATH"

To persist it for your user account:
  [Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-21', 'User')

If this project uses IntelliJ, select a Project SDK under Project Structure. The
launcher checks .idea\misc.xml and IntelliJ's registered SDK table when present.
"@
}

function Invoke-Widget {
    param(
        [string]$Root,
        [string]$MavenWrapper,
        [string]$Label
    )

    Set-Location $Root
    $mavenArguments = @("-q", "-DskipTests", "compile", "exec:java")
    if (-not [string]::IsNullOrWhiteSpace($Label)) {
        $mavenArguments += "-Dexec.args=$Label"
    }

    & $MavenWrapper @mavenArguments
    return $LASTEXITCODE
}

$repoRoot = Resolve-RepositoryRoot $MyInvocation.MyCommand.Path
$mavenWrapper = Join-Path $repoRoot "mvnw.cmd"

if (-not (Test-Path -LiteralPath $mavenWrapper -PathType Leaf)) {
    Write-Error @"
Maven Wrapper not found at: $mavenWrapper

Ask a human with a working Maven installation to generate and commit:
  mvnw
  mvnw.cmd
  .mvn/wrapper/maven-wrapper.properties
"@
    exit 2
}

$javaResolution = Resolve-JavaHome $repoRoot
if (-not $javaResolution) {
    Show-JavaSetupInstructions
    exit 3
}

Initialize-JavaEnvironment $javaResolution
Write-Host "Repository root: $repoRoot"
Write-Host "Using JDK from $($javaResolution.Source): $($javaResolution.JavaHome)"

exit (Invoke-Widget -Root $repoRoot -MavenWrapper $mavenWrapper -Label $RepositoryLabel)
