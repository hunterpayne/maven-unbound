@echo off

pushd %~dp0
set DIR=%CD%
popd

if EXIST %DIR%/unbound-1.0.0-exec.jar (
  java -jar %DIR%/unbound-1.0.0-exec.jar %*
) ELSE (
  REM For testing in a dev environment
  java -jar %DIR%/target/unbound-1.0.0-exec.jar %*
)

:loop 
  if [%1]==["--generate-hocon"] echo "Done" & goto :done
  if [%1]==["--generate-json"] echo "Done" & goto :done
  echo %1
  shift
  goto :loop

mvn %*

:done
