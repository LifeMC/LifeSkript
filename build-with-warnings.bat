@echo off
@setlocal enableextensions
@cd /d "%~dp0"
mvn -e -DcompilerArgument=-Xlint:all clean install package
pause
cmd /k
