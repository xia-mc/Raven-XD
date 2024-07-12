
<div align="center">
  
# Raven XD
<p align="center">
    <a href="https://github.com/xia-mc/Raven-bS/issues">
      <img src="https://img.shields.io/github/issues/xia-mc/Raven-bS?style=flat" alt="issues" />
    </a>
    <img src="https://img.shields.io/badge/license-GPLV3-green" alt="License">
</p>

[![Github Release Downloads](https://img.shields.io/github/downloads/xia-mc/Raven-bS/total?label=Github%20Release%20Downloads&style=flat-square)](https://github.com/xia-mc/Raven-bS/releases)
<!--
[![CurseForge Downloads](http://cf.way2muchnoise.eu/997222.svg?badge_style=flat)](https://www.curseforge.com/minecraft/mc-mods/cheatdetector)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/QNVaUzHT?label=Modrinth%20Downloads&logo=Modrinth%20Downloads&style=flat-square)](https://modrinth.com/mod/cheatdetector)
-->

Raven B4, but for those who can't afford it.

Raven B4, but not only for Hypixel.

![2024-07-10_14 13 51](https://github.com/xia-mc/Raven-XD/assets/108219418/15470b4f-b29d-49a5-8396-1c1d4bd315a1)

### ***More functionality while staying in sync with upstream branches.***

<a href="https://discord.gg/zSgDKArSH4"><img src="https://invidget.switchblade.xyz/zSgDKArSH4" alt="https://discord.gg/zSgDKArSH4"/></a><br>
</div>

## Additional Modules
 1. Hypixel
    - Up Sprint Scaffold
    - Fireball LongJump
    - NoFall
    - AntiVoid
 2. BlocksMC
    - NoSlow
    - AntiVoid
    - Tower
 3. Intave
    - KillAura
    - Scaffold
    - NoSlow
 4. GrimAC
    - KillAura
    - Scaffold
    - NoSlow
    - Velocity
    - Fly
 5. Polar
    - NoSlow
 6. Undetectable
    - AimAssist
    - AutoClicker
    - HitSelect
    - MoreKB
    - Block-In
    - Legit Scaffold
 7. Common
    - Client-side AntiCheat
    - TimerRange
    - Backtrack

## How do I build this client?

### Steps:

1. **Download and Install Java SDK version 8**:
   - Download from [Java SDK version 8](https://adoptium.net/en-GB/temurin/releases/?version=8).
   - Install it and set the `JAVA_HOME` environment variable to the installation directory.

2. **Verify Java Installation**:
   - Open a terminal or command prompt.
   - Run `java -version`.
   - Ensure the output contains `openjdk version "1.8.0_{...}"`.

3. **Install Gradle 4.7**:
   - Download from [Gradle 4.7](https://gradle.org/next-steps/?version=4.7&format=bin).
   - Follow the "Installing manually" guide on the [Gradle wiki](https://gradle.org/install).

4. **Clone the Repository**:
   - Run the following commands in the terminal or command prompt:
     ```bash
     git clone --recursive https://github.com/xia-mc/Raven-XD.git
     cd Raven-XD
     ```

5. **Set Up the Project**:
   - Run the following commands to set up the Gradle wrapper and the development environment:
     ```bash
     gradlew.bat
     gradle wrapper
     gradlew setupDecompWorkspace && gradlew idea
     gradlew build
     ```

6. **Build Success**:
   - If all steps complete without errors, the project is successfully built.


## Contribute
Feel free to raise an issue or submit a pull request.

If you want to co-develop this project with me, please contact me.

<img src="https://github.com/SAWARATSUKI/KawaiiLogos/blob/main/IntelliJ IDEA/IntelliJ IDEA.png" alt="Java" width="500" />
