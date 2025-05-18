@echo off
echo Cambiando perfil de Spring Boot

if "%1"=="prod" (
    echo Activando perfil de produccion
    echo spring: > src\main\resources\application.yml
    echo   profiles: >> src\main\resources\application.yml
    echo     active: prod >> src\main\resources\application.yml
    echo Perfil de produccion activado
    echo.
    echo IMPORTANTE: Asegurate de que tienes la variable de entorno SPRING_DATASOURCE_PASSWORD configurada
    echo con la contrasena correcta para la base de datos en produccion.
) else if "%1"=="dev" (
    echo Activando perfil de desarrollo
    echo spring: > src\main\resources\application.yml
    echo   profiles: >> src\main\resources\application.yml
    echo     active: dev >> src\main\resources\application.yml
    echo Perfil de desarrollo activado
) else (
    echo Uso: switch-profile [prod^|dev]
    echo   prod: Activa el perfil de produccion
    echo   dev: Activa el perfil de desarrollo
)
