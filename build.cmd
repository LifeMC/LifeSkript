:: Used to build a production ready JAR file in target\Skript.jar.
:: GitHub repository can be found at: https://github.com/LifeMC/LifeSkript

@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call ./mvnw.cmd -Duser.name="Skript Team" -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=Asia/Istanbul -DcompilerArgument=-O -e -U clean install package
call java -Duser.name="Skript Team" -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=Asia/Istanbul -Dmaximum.inlined.code.length=80 -jar lib\proguard.jar @Skript.pro
pause
cmd /k
