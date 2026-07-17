# Sheet Analyzer — Rider Sudoku Plugin (Stealth Mode)

A Sudoku game disguised as a data analysis tool for JetBrains Rider.
Looks like work, plays like a game. 🐟

## Features

- **3 Levels**: Lv.1 / Lv.2 / Lv.3 (Easy / Medium / Hard)
- **Notes Mode**: Record candidate values in cells
- **Constraint Validation**: Highlights conflicts automatically
- **Auto-fill**: Get hints when stuck
- **Timer**: Track your session time
- **Disguise Mode**: One-click switch between game UI and "Data Grid" work UI

## Disguise Mode

Click the **Disguise** button to toggle:

| Normal Mode | Disguise Mode |
|-------------|---------------|
| Sheet Analyzer | Data Grid v2.4 |
| Lv.1 / Lv.2 / Lv.3 | S1 / S2 / S3 |
| Progress: X/81 | Progress: X/81 sectors processed |

## Keyboard Shortcuts

| Key | Action |
|-----|--------|
| `↑↓←→` | Navigate cells |
| `1-9` | Enter value |
| `0` / `Delete` / `Backspace` | Clear cell |
| `N` | Toggle notes mode |

## Build

```bash
cd sudoku-plugin
export JAVA_HOME="C:/Program Files/Java/jdk-17.0.12+7"
./gradlew buildPlugin
# Output: build/distributions/SudokuMoyu-1.0.0.zip
```

## Install

**File → Settings → Plugins → ⚙ → Install Plugin from Disk...**

Select `SudokuMoyu-1.0.0.zip`, restart Rider, then open the **Sudoku** tab at the bottom.
