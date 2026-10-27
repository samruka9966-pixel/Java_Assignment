@echo off
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\Admin\jdk-extract\jdk-17.0.9+9\bin\java.exe" (
        set "PATH=C:\Users\Admin\jdk-extract\jdk-17.0.9+9\bin;%PATH%"
    )
)

java -ea -cp ".;lib/*" SystemVerifier
pause
