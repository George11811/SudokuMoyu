package com.sudoku.plugin.model

/**
 * 数独棋盘状态管理
 */
class SudokuBoard(difficulty: Int = 1) {

    var difficulty: Int
    val solution: Array<IntArray>
    val initial: Array<IntArray>        // 初始谜题（不可修改的格）
    val current: Array<IntArray>        // 当前玩家填入的值
    val notes: Array<Array<MutableSet<Int>>>   // 每个格子的候选笔记

    private var _startTime: Long = System.currentTimeMillis()

    val startTime: Long get() = _startTime

    init {
        this.difficulty = difficulty.coerceIn(1, 3)
        val (puzzle, sol) = SudokuGenerator.generatePuzzle(this.difficulty)
        this.initial = puzzle
        this.solution = sol
        this.current = puzzle.map { it.clone() }.toTypedArray()
        this.notes = Array(9) { Array(9) { mutableSetOf() } }
    }

    /** 该位置是否为初始给定的数字（不可编辑） */
    fun isGiven(row: Int, col: Int): Boolean = initial[row][col] != 0

    /** 该位置是否已被玩家填入数字 */
    fun isFilled(row: Int, col: Int): Boolean = current[row][col] != 0

    /** 获取当前位置的数字（0表示空） */
    fun getNumber(row: Int, col: Int): Int = current[row][col]

    /** 设置数字，返回是否设置成功（初始格不可设置） */
    fun setNumber(row: Int, col: Int, num: Int): Boolean {
        if (isGiven(row, col)) return false
        current[row][col] = num
        // 设置数字时清除该格的笔记
        notes[row][col].clear()
        // 清除同行同列同宫相同数字的笔记（整洁）
        if (num != 0) {
            clearRelatedNotes(row, col, num)
        }
        return true
    }

    /** 清除与当前位置相关的笔记 */
    private fun clearRelatedNotes(row: Int, col: Int, num: Int) {
        for (i in 0 until 9) {
            notes[row][i].remove(num)
            notes[i][col].remove(num)
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                notes[r][c].remove(num)
            }
        }
    }

    /** 切换笔记（候选数字） */
    fun toggleNote(row: Int, col: Int, num: Int) {
        if (isGiven(row, col) || isFilled(row, col)) return
        if (notes[row][col].contains(num)) {
            notes[row][col].remove(num)
        } else {
            notes[row][col].add(num)
        }
    }

    /** 获取该格的笔记 */
    fun getNotes(row: Int, col: Int): Set<Int> = notes[row][col].toSet()

    /** 检查当前位置是否有冲突 */
    fun hasConflict(row: Int, col: Int): Boolean {
        val num = current[row][col]
        if (num == 0) return false
        current[row][col] = 0
        val valid = SudokuSolver.isValid(current, row, col, num)
        current[row][col] = num
        return !valid
    }

    /** 获取所有有冲突的位置 */
    fun getAllConflicts(): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (hasConflict(r, c)) {
                    conflicts.add(r to c)
                }
            }
        }
        return conflicts
    }

    /** 检查是否胜利 */
    fun isComplete(): Boolean = SudokuSolver.isBoardComplete(current)

    /** 获取提示：在某个空格填入正确答案 */
    fun getHint(): Triple<Int, Int, Int>? {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (current[r][c] == 0) {
                    return Triple(r, c, solution[r][c])
                }
            }
        }
        return null
    }

    /** 重置所有玩家的输入 */
    fun reset() {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                current[r][c] = initial[r][c]
                notes[r][c].clear()
            }
        }
        _startTime = System.currentTimeMillis()
    }

    /** 重新开始（新游戏） */
    fun newGame(difficulty: Int = this.difficulty) {
        this.difficulty = difficulty.coerceIn(1, 3)
        val (puzzle, sol) = SudokuGenerator.generatePuzzle(this.difficulty)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                initial[r][c] = puzzle[r][c]
                solution[r][c] = sol[r][c]
                current[r][c] = puzzle[r][c]
                notes[r][c].clear()
            }
        }
        _startTime = System.currentTimeMillis()
    }
}
