@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call mvn -e -U -DcompilerArgument=-O clean install package
call java -jar lib\proguard.jar @Skript.pro
pause
cmd /k
