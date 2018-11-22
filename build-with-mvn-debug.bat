@echo off
@setlocal enableextensions
@cd /d "%~dp0"
mvn -e -X -DcompilerArgument=-O clean install package
pause
cmd /k