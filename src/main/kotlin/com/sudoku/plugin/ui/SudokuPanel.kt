package com.sudoku.plugin.ui

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.sudoku.plugin.model.SudokuBoard
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class SudokuPanel {

    val mainPanel: JPanel
    private val board: SudokuBoard = SudokuBoard(1)

    // UI components
    private val gridPanel = JBPanel<JBPanel<*>>(GridLayout(9, 9, 0, 0))
    private val cells = Array(9) { Array<SudokuCell>(9) { SudokuCell() } }
    private val timerLabel = JBLabel("00:00")
    private val levelCombo = JComboBox(arrayOf("Lv.1", "Lv.2", "Lv.3"))
    private val messageLabel = JBLabel("")
    private val titleLabel = JBLabel("Sheet Analyzer")
    private val disguiseBtn = JToggleButton("Disguise")

    // State
    private var selectedRow = -1
    private var selectedCol = -1
    private var noteMode = false
    private var timerRunning = true
    private var timer: Timer? = null
    private var elapsedSeconds = 0
    private var disguiseMode = false
    private var suppressLevelEvents = false

    // Disguise mode label mappings
    private val realTitle = "Sheet Analyzer"
    private val disguisedTitle = "Data Grid v2.4"
    private val realLevels = arrayOf("Lv.1", "Lv.2", "Lv.3")
    private val disguisedLevels = arrayOf("S1", "S2", "S3")

    init {
        mainPanel = createMainPanel()
        initGame()
        startTimer()
    }

    private fun createMainPanel(): JPanel {
        val panel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)
            preferredSize = Dimension(520, 620)
        }

        // ===== Top bar: title + controls =====
        val topBar = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            border = JBUI.Borders.empty(0, 4, 6, 4)
        }

        titleLabel.apply {
            font = Font("Segoe UI", Font.BOLD, 15)
            foreground = JBColor(0x3A6B8C, 0x7CB9E8)
        }
        topBar.add(titleLabel, BorderLayout.WEST)

        // Right side: level selector + disguise + timer
        val rightPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.RIGHT, 6, 0))

        levelCombo.apply {
            font = Font("Segoe UI", Font.PLAIN, 12)
            setPreferredSize(Dimension(80, 26))
            addActionListener {
                if (suppressLevelEvents) return@addActionListener
                val result = JOptionPane.showConfirmDialog(
                    mainPanel, "Restart with new parameters? Current progress will be lost.",
                    "New Session", JOptionPane.YES_NO_OPTION
                )
                if (result == JOptionPane.YES_OPTION) {
                    newGame()
                } else {
                    levelCombo.selectedIndex = board.difficulty - 1
                }
            }
        }
        rightPanel.add(JBLabel("Level:").apply { font = Font("Segoe UI", Font.PLAIN, 11) })
        rightPanel.add(levelCombo)

        // Disguise toggle
        disguiseBtn.apply {
            font = Font("Segoe UI", Font.PLAIN, 11)
            isSelected = false
            setPreferredSize(Dimension(80, 26))
            addActionListener {
                disguiseMode = isSelected
                applyDisguise()
            }
        }
        rightPanel.add(Box.createHorizontalStrut(6))
        rightPanel.add(disguiseBtn)

        timerLabel.apply {
            font = Font("Consolas", Font.PLAIN, 14)
            foreground = JBColor(0x666666, 0xAAAAAA)
        }
        rightPanel.add(Box.createHorizontalStrut(10))
        rightPanel.add(timerLabel)

        topBar.add(rightPanel, BorderLayout.EAST)
        panel.add(topBar, BorderLayout.NORTH)

        // ===== Center: Grid =====
        val boardContainer = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            border = JBUI.Borders.empty(2)
        }
        gridPanel.apply {
            setPreferredSize(Dimension(450, 450))
            setMinimumSize(Dimension(360, 360))
            background = JBColor(0x3A3A3A, 0x5C5C5C)
            border = JBUI.Borders.customLine(JBColor(0x3A3A3A, 0x888888), 2)
        }
        initCells()
        boardContainer.add(gridPanel)
        panel.add(boardContainer, BorderLayout.CENTER)

        // ===== Bottom: controls + progress =====
        val bottomPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            border = JBUI.Borders.empty(4, 4, 0, 4)
        }

        // Number pad (1-9)
        val numPad = JBPanel<JBPanel<*>>(GridLayout(1, 9, 3, 0)).apply {
            border = JBUI.Borders.empty(0, 4, 4, 4)
        }
        for (n in 1..9) {
            val btn = createNumberBtn(n)
            numPad.add(btn)
        }
        bottomPanel.add(numPad, BorderLayout.NORTH)

        // Function buttons
        val funcBar = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.CENTER, 5, 1))

        val noteBtn = JToggleButton("Notes").apply {
            font = Font("Segoe UI", Font.PLAIN, 11)
            addActionListener { noteMode = isSelected }
        }
        funcBar.add(noteBtn)

        val hintBtn = JButton("Auto-fill").apply {
            font = Font("Segoe UI", Font.PLAIN, 11)
            addActionListener { giveHint() }
        }
        funcBar.add(hintBtn)

        val resetBtn = JButton("Clear").apply {
            font = Font("Segoe UI", Font.PLAIN, 11)
            addActionListener {
                val r = JOptionPane.showConfirmDialog(
                    mainPanel, "Reset all inputs?",
                    "Reset", JOptionPane.YES_NO_OPTION
                )
                if (r == JOptionPane.YES_OPTION) {
                    board.reset()
                    elapsedSeconds = 0
                    refreshAll()
                    clearMessage()
                }
            }
        }
        funcBar.add(resetBtn)

        val newBtn = JButton("New").apply {
            font = Font("Segoe UI", Font.PLAIN, 11)
            addActionListener { newGame() }
        }
        funcBar.add(newBtn)

        val checkBtn = JButton("Validate").apply {
            font = Font("Segoe UI", Font.PLAIN, 11)
            addActionListener { checkBoard() }
        }
        funcBar.add(checkBtn)

        bottomPanel.add(funcBar, BorderLayout.CENTER)



        // Message
        messageLabel.apply {
            font = Font("Segoe UI", Font.PLAIN, 11)
            horizontalAlignment = SwingConstants.CENTER
            foreground = JBColor(0x666666, 0x888888)
        }

        // Message label
        messageLabel.border = JBUI.Borders.empty(2, 4, 0, 4)
        bottomPanel.add(messageLabel, BorderLayout.SOUTH)

        panel.add(bottomPanel, BorderLayout.SOUTH)

        applyDisguise()
        return panel
    }

    private fun createNumberBtn(n: Int): JButton {
        val btn = JButton(n.toString()).apply {
            font = Font("Consolas", Font.BOLD, 15)
            setPreferredSize(Dimension(42, 30))
            isFocusPainted = false
            addActionListener { onNumberInput(n) }
        }
        return btn
    }

    private fun initCells() {
        gridPanel.removeAll()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val cell = SudokuCell(r, c)
                cells[r][c] = cell
                gridPanel.add(cell)

                cell.addMouseListener(object : MouseAdapter() {
                    override fun mousePressed(e: MouseEvent) {
                        selectCell(r, c)
                    }
                })
            }
        }
        gridPanel.isFocusable = true
        gridPanel.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (selectedRow < 0 || selectedCol < 0) return
                when (e.keyCode) {
                    KeyEvent.VK_UP -> if (selectedRow > 0) selectCell(selectedRow - 1, selectedCol)
                    KeyEvent.VK_DOWN -> if (selectedRow < 8) selectCell(selectedRow + 1, selectedCol)
                    KeyEvent.VK_LEFT -> if (selectedCol > 0) selectCell(selectedRow, selectedCol - 1)
                    KeyEvent.VK_RIGHT -> if (selectedCol < 8) selectCell(selectedRow, selectedCol + 1)
                    KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE -> onNumberInput(0)
                    KeyEvent.VK_N -> noteMode = !noteMode
                    in KeyEvent.VK_1..KeyEvent.VK_9 -> onNumberInput(e.keyCode - KeyEvent.VK_0)
                    KeyEvent.VK_0 -> onNumberInput(0)
                }
            }
        })
    }

    private fun selectCell(row: Int, col: Int) {
        if (selectedRow >= 0 && selectedCol >= 0) {
            cells[selectedRow][selectedCol].isSelected = false
        }
        selectedRow = row
        selectedCol = col
        cells[row][col].isSelected = true
        gridPanel.requestFocusInWindow()
        refreshHighlights()
        clearMessage()
    }

    private fun onNumberInput(num: Int) {
        if (selectedRow < 0 || selectedCol < 0) return
        val r = selectedRow
        val c = selectedCol
        if (board.isGiven(r, c)) return

        if (noteMode && num in 1..9) {
            board.toggleNote(r, c, num)
        } else {
            board.setNumber(r, c, num)
        }
        refreshAll()

        if (board.isComplete()) {
            timerRunning = false
            messageLabel.apply {
                text = "Complete! Time: ${formatTime(elapsedSeconds)}"
                foreground = Color(0x1B8A3D)
                font = Font("Segoe UI", Font.BOLD, 14)
            }
            JOptionPane.showMessageDialog(
                mainPanel,
                "Session complete.\nTime: ${formatTime(elapsedSeconds)}",
                "Complete",
                JOptionPane.INFORMATION_MESSAGE
            )
        }
    }

    private fun giveHint() {
        val hint = board.getHint()
        if (hint == null) {
            showMessage("All cells filled. Run validation.", JBColor(0x888888, 0x888888))
            return
        }
        val (r, c, num) = hint
        selectedRow = r
        selectedCol = c
        board.setNumber(r, c, num)
        refreshAll()
        selectCell(r, c)
        showMessage("Auto-fill: row ${r + 1}, col ${c + 1} => $num", JBColor(0x2B6F9E, 0x7CB9E8))
    }

    private fun checkBoard() {
        val conflicts = board.getAllConflicts()
        if (conflicts.isEmpty()) {
            if (board.isComplete()) {
                showMessage("Validation passed. All constraints satisfied.", Color(0x1B8A3D))
            } else {
                showMessage("No conflicts found. ${countFilled()}/81 cells filled.", Color(0x1B8A3D))
            }
        } else {
            showMessage("${conflicts.size} constraint violations detected.", JBColor(0xC0392B, 0xE74C3C))
        }
        refreshAll()
    }

    private fun countFilled(): Int {
        var count = 0
        for (r in 0 until 9) for (c in 0 until 9) if (board.getNumber(r, c) != 0) count++
        return count
    }

    private fun newGame() {
        timer?.stop()
        timerRunning = false
        val diff = levelCombo.selectedIndex + 1
        board.newGame(diff)
        selectedRow = -1
        selectedCol = -1
        noteMode = false
        elapsedSeconds = 0
        refreshAll()
        clearMessage()
        timerRunning = true
        startTimer()
    }

    private fun initGame() {
        refreshAll()
        levelCombo.selectedIndex = 0
    }

    private fun refreshAll() {
        val conflicts = board.getAllConflicts()
        val selectedNum = if (selectedRow >= 0 && selectedCol >= 0) {
            board.getNumber(selectedRow, selectedCol)
        } else 0

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val cell = cells[r][c]
                cell.number = board.getNumber(r, c)
                cell.isGiven = board.isGiven(r, c)
                cell.notes = board.getNotes(r, c)
                cell.hasConflict = conflicts.contains(r to c)
                cell.isSelected = (r == selectedRow && c == selectedCol)
                cell.isSameNumber = selectedNum != 0 && board.getNumber(r, c) == selectedNum && !cell.isSelected
                cell.isRelated = (selectedRow >= 0 && selectedCol >= 0) &&
                        (r == selectedRow || c == selectedCol ||
                                (r / 3 == selectedRow / 3 && c / 3 == selectedCol / 3)) &&
                        !cell.isSelected && !cell.isSameNumber
                cell.disguiseMode = disguiseMode
            }
        }
        gridPanel.repaint()

    }

    private fun refreshHighlights() = refreshAll()

    private fun showMessage(msg: String, color: Color) {
        messageLabel.text = msg
        messageLabel.foreground = color
    }

    private fun clearMessage() {
        messageLabel.text = ""
    }

    private fun startTimer() {
        timer?.stop()
        timer = Timer(1000) {
            if (timerRunning && !board.isComplete()) {
                elapsedSeconds++
                timerLabel.text = formatTime(elapsedSeconds)
            }
        }.apply { start() }
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    /** Toggle between game look and work disguise */
    private fun applyDisguise() {
        suppressLevelEvents = true
        if (disguiseMode) {
            titleLabel.text = disguisedTitle
            titleLabel.foreground = JBColor(0x666666, 0x999999)
            val sel = levelCombo.selectedIndex
            levelCombo.removeAllItems()
            disguisedLevels.forEach { levelCombo.addItem(it) }
            if (sel in disguisedLevels.indices) levelCombo.selectedIndex = sel
            disguiseBtn.text = "Normal"
            gridPanel.background = JBColor(0xE0E0E0, 0x444444)
            gridPanel.border = JBUI.Borders.customLine(JBColor(0xA0A0A0, 0x666666), 1)
        } else {
            titleLabel.text = realTitle
            titleLabel.foreground = JBColor(0x3A6B8C, 0x7CB9E8)
            val sel = levelCombo.selectedIndex
            levelCombo.removeAllItems()
            realLevels.forEach { levelCombo.addItem(it) }
            if (sel in realLevels.indices) levelCombo.selectedIndex = sel
            disguiseBtn.text = "Disguise"
            gridPanel.background = JBColor(0x3A3A3A, 0x5C5C5C)
            gridPanel.border = JBUI.Borders.customLine(JBColor(0x3A3A3A, 0x888888), 2)
        }
        suppressLevelEvents = false
        refreshAll()
    }

    fun dispose() {
        timerRunning = false
        timer?.stop()
        timer = null
    }

}
