@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
:analyze
call "C:\Program Files\Java\jdk-15\bin\java.exe" -Duser.name="Skript Team" -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=Asia/Istanbul -DcompilerArgument=-O --enable-preview --source 15 JavaAnalyzer.java "../src/main/java"
pause
goto analyze
