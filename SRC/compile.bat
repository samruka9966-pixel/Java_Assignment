@echo off
echo =======================================================================
echo Compiling Kongu Engineering College (R2024) Student Academic System...
echo =======================================================================

where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\Admin\jdk-extract\jdk-17.0.9+9\bin\javac.exe" (
        set "PATH=C:\Users\Admin\jdk-extract\jdk-17.0.9+9\bin;%PATH%"
    )
)

javac -cp ".;lib/*" *.java
if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] Compilation completed successfully!
    echo Run 'run_interactive.bat' to launch the menu or 'run_demo.bat' for live verification.
) else (
    echo.
    echo [ERROR] Compilation failed. Please check your JDK setup.
)
pause
