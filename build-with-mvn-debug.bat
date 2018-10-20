@echo off
@setlocal enableextensions
@cd /d "%~dp0"
mvn -e -X clean install package
pause
cmd /k