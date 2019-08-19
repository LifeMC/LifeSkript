:: The UTF-8, working directory and echo off thing
@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"

:: Install manual dependencies to local repository
call ./mvnw.cmd install:install-file -Dfile=lib/timings-1.8.8.jar -DgroupId=co.aikar -DartifactId=timings -Dversion=1.8.8 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/vault-plugin-1.5.6.jar -DgroupId=net.milkbowl.vault -DartifactId=vault-plugin -Dversion=1.5.6 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/sqlibrary-7.1.jar -DgroupId=patpeter -DartifactId=sqlibrary -Dversion=7.1 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/griefprevention-13.9.1.jar -DgroupId=me.ryanhamshire -DartifactId=griefprevention -Dversion=13.9.1 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/worldguard-6.1.2.jar -DgroupId=com.sk89q -DartifactId=worldguard -Dversion=6.1.2 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/worldedit-6.1.9.jar -DgroupId=com.sk89q -DartifactId=worldedit -Dversion=6.1.9 -Dpackaging=jar -DgeneratePom=true

:: Pause, exit, or whatever. It's finished now.
pause
cmd /k
