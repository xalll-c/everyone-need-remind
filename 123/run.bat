@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo [编译] 正在编译 Java 源码...
javac -encoding UTF-8 -d out src\com\gamereminder\*.java

if %errorlevel% neq 0 (
    echo 编译失败，请检查是否安装了 JDK。
    pause
    exit /b 1
)

echo [启动] 游戏学习提醒已启动，请查看系统托盘。
start javaw -Dfile.encoding=UTF-8 -cp out com.gamereminder.App