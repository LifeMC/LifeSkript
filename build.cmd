:: Used to build a production ready JAR file in target\Skript.jar.
:: GitHub repository can be found at: https://github.com/LifeMC/LifeSkript

:: The UTF-8, working directory and echo off thing
@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"

:: Install manual dependencies to local repository
call ./mvnw.cmd -nsu install:install-file -Dfile=lib/timings-1.8.8.jar -DgroupId=co.aikar -DartifactId=timings -Dversion=1.8.8 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd -nsu install:install-file -Dfile=lib/vault-plugin-1.5.6.jar -DgroupId=net.milkbowl.vault -DartifactId=vault-plugin -Dversion=1.5.6 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd -nsu install:install-file -Dfile=lib/sqlibrary-7.1.jar -DgroupId=patpeter -DartifactId=sqlibrary -Dversion=7.1 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd -nsu install:install-file -Dfile=lib/griefprevention-13.9.1.jar -DgroupId=me.ryanhamshire -DartifactId=griefprevention -Dversion=13.9.1 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd -nsu install:install-file -Dfile=lib/worldguard-6.1.2.jar -DgroupId=com.sk89q -DartifactId=worldguard -Dversion=6.1.2 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd -nsu install:install-file -Dfile=lib/worldedit-6.1.9.jar -DgroupId=com.sk89q -DartifactId=worldedit -Dversion=6.1.9 -Dpackaging=jar -DgeneratePom=true

:: Build and optimize the JAR files of the plugin
call ./mvnw.cmd -Duser.name="Skript Team" -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=Asia/Istanbul -DcompilerArgument=-O -e -U -nsu clean install package
call java -Duser.name="Skript Team" -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=Asia/Istanbul -jar lib\proguard.jar @Skript.pro

:: Pause, exit, or whatever. It's finished now.
pause
cmd /k
