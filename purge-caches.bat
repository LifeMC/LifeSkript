@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call mvn -e dependency:purge-local-repository
pause
cmd /k
