@echo off

pushd %~dp0
set DIR=%CD%
popd

if EXIST "%DIR%/unbound-1.0.0-exec.jar" java -jar "%DIR%\unbound-1.0.0-exec.jar" %*
REM For testing in a dev environment
if EXIST "%DIR%\target\unbound-1.0.0-exec.jar" java -jar "%DIR%\target\unbound-1.0.0-exec.jar" %*

:loop
  if [%1]==[--generate-hocon] (
	goto :done
  ) else if [%1]==[--generate-json] (
	goto :done
  ) else if [%1]==[] (
	goto :run-mvn
  )
  rem echo "arg %1"
  shift
  goto :loop

:run-mvn
mvn %*

:done
