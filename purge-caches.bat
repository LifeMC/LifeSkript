@echo off
@setlocal enableextensions
@cd /d "%~dp0"
mvn dependency:purge-local-repository
pause
cmd /k