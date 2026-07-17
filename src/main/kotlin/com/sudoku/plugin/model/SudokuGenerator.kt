package com.sudoku.plugin.model

import kotlin.random.Random

/**
 * 数独谜题生成器
 */
object SudokuGenerator {

    /**
     * 生成一个完整的有效数独棋盘
     */
    private fun generateCompleteBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) { 0 } }
        fillBoard(board)
        return board
    }

    private fun fillBoard(board: Array<IntArray>): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (board[row][col] == 0) {
                    val numbers = (1..9).shuffled()
                    for (num in numbers) {
                        if (SudokuSolver.isValid(board, row, col, num)) {
                            board[row][col] = num
                            if (fillBoard(board)) return true
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    /**
     * 根据难度生成谜题
     *
     * @param difficulty 难度: 1=简单, 2=中等, 3=困难
     * @return Pair<初始棋盘 (0表示空格), 完整解答>
     */
    fun generatePuzzle(difficulty: Int = 1): Pair<Array<IntArray>, Array<IntArray>> {
        val solution = generateCompleteBoard()
        val puzzle = solution.map { it.clone() }.toTypedArray()

        // 根据难度决定要挖掉多少个数字
        val cellsToRemove = when (difficulty) {
            1 -> 30 + Random.nextInt(5)   // 30-34 简单
            2 -> 40 + Random.nextInt(5)   // 40-44 中等
            3 -> 50 + Random.nextInt(5)   // 50-54 困难
            else -> 35
        }

        // 收集所有位置并打乱
        val positions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                positions.add(r to c)
            }
        }
        positions.shuffle()

        var removed = 0
        for ((row, col) in positions) {
            if (removed >= cellsToRemove) break
            val backup = puzzle[row][col]
            puzzle[row][col] = 0

            // 检查解是否唯一
            if (SudokuSolver.countSolutions(puzzle) == 1) {
                removed++
            } else {
                puzzle[row][col] = backup // 恢复
            }
        }

        return Pair(puzzle, solution)
    }
}
