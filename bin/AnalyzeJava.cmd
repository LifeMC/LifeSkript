@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call "C:\Program Files\Java\jdk-12.0.2\bin\java.exe" -Duser.name="Skript Team" -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=Asia/Istanbul -DcompilerArgument=-O --source 12 JavaAnalyzer.java "../src/main/java"
pause
cmd /k
