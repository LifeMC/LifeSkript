@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call mvn install
pause
