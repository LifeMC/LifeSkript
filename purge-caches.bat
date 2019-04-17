@echo off
@setlocal enableextensions
@cd /d "%~dp0"
mvn -e dependency:purge-local-repository
pause
cmd /k
