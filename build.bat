@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call mvn -e -DcompilerArgument=-O clean install package
pause
cmd /k
