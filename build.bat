@echo off
@setlocal enableextensions
@cd /d "%~dp0"
mvn -e -DcompilerArgument=-O clean install package
pause
cmd /k