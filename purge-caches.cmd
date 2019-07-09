@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call ./mvnw.cmd -e dependency:purge-local-repository
pause
cmd /k
