@echo off
@setlocal enableextensions
@cd /d "%~dp0"
mvn -e clean install package -U
pause
cmd /k