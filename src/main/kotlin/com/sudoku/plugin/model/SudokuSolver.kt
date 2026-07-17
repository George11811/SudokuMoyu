package com.sudoku.plugin.model

/**
 * 数独求解器，使用回溯算法
 */
object SudokuSolver {

    /**
     * 解数独，返回解的个数（最多查两个，用于判断唯一性）
     */
    fun countSolutions(board: Array<IntArray>, limit: Int = 2): Int {
        val grid = board.map { it.clone() }.toTypedArray()
        var count = 0
        fun solve(): Boolean {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    if (grid[row][col] == 0) {
                        for (num in 1..9) {
                            if (isValid(grid, row, col, num)) {
                                grid[row][col] = num
                                if (solve()) return true
                                grid[row][col] = 0
                            }
                        }
                        return false
                    }
                }
            }
            count++
            return count >= limit
        }
        solve()
        return count
    }

    /**
     * 检查在指定位置放置数字是否有效
     */
    fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // 检查行
        for (c in 0 until 9) {
            if (board[row][c] == num) return false
        }
        // 检查列
        for (r in 0 until 9) {
            if (board[r][col] == num) return false
        }
        // 检查 3x3 宫
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (board[r][c] == num) return false
            }
        }
        return true
    }

    /**
     * 检查整个棋盘是否有效（用于验证玩家输入）
     */
    fun isBoardValid(board: Array<IntArray>): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val num = board[row][col]
                if (num != 0) {
                    board[row][col] = 0
                    val valid = isValid(board, row, col, num)
                    board[row][col] = num
                    if (!valid) return false
                }
            }
        }
        return true
    }

    /**
     * 检查棋盘是否已填满且有效（胜利判定）
     */
    fun isBoardComplete(board: Array<IntArray>): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (board[row][col] == 0) return false
            }
        }
        return isBoardValid(board)
    }
}
