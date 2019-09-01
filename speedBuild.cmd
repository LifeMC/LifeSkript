@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call mvn install -e -Dversions.skip=true -Dmaven.test.skip=true -DskipTests=true -Dmaven.test.skip.exec=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true -P !deploy,!only-eclipse
pause
