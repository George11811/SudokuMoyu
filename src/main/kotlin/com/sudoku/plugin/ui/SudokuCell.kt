package com.sudoku.plugin.ui

import com.intellij.ui.JBColor
import java.awt.*
import javax.swing.JComponent

class SudokuCell(
    val row: Int = 0,
    val col: Int = 0
) : JComponent() {

    var number: Int = 0
        set(value) { field = value; repaint() }
    var isGiven: Boolean = false
        set(value) { field = value; repaint() }
    var isSelected: Boolean = false
        set(value) { field = value; repaint() }
    var isSameNumber: Boolean = false
        set(value) { field = value; repaint() }
    var isRelated: Boolean = false
        set(value) { field = value; repaint() }
    var hasConflict: Boolean = false
        set(value) { field = value; repaint() }
    var notes: Set<Int> = emptySet()
        set(value) { field = value; repaint() }
    var disguiseMode: Boolean = false
        set(value) { field = value; repaint() }

    override fun getPreferredSize() = Dimension(46, 46)
    override fun getMinimumSize() = Dimension(34, 34)

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val w = width
        val h = height

        // ── Background ──
        if (disguiseMode) {
            g2.color = JBColor(0xF5F5F5, 0x2C2C2C)
            g2.fillRect(0, 0, w, h)
            if (isSelected) {
                g2.color = JBColor(0xE0E4E8, 0x383838)
                g2.fillRect(0, 0, w, h)
            }
        } else {
            when {
                isSelected -> g2.color = JBColor(0xE3EAF3, 0x3A4A5A)
                isSameNumber -> g2.color = JBColor(0xEDF0F5, 0x303F4A)
                isRelated -> g2.color = JBColor(0xF5F6F8, 0x2A3640)
                (row + col) % 2 == 0 -> g2.color = JBColor(0xFAFAFA, 0x2D2D2D)
                else -> g2.color = JBColor(0xF2F2F2, 0x333333)
            }
            g2.fillRect(0, 0, w, h)
        }

        // ── Conflict ──
        if (hasConflict) {
            if (disguiseMode) {
                g2.color = JBColor(0xECECEC, 0x3A3030)
                g2.fillRect(0, 0, w, h)
            } else {
                g2.color = JBColor(0xF2E0E0, 0x4A3030)
                g2.fillRect(0, 0, w, h)
            }
        }

        // ── Grid lines (right & bottom only) ──
        val isBoxRight = col % 3 == 2
        val isBoxBottom = row % 3 == 2
        val isLastCol = col == 8
        val isLastRow = row == 8

        if (disguiseMode) {
            val lc = JBColor(0xD8D8D8, 0x4A4A4A)
            val bc = JBColor(0xB0B0B0, 0x666666)
            drawBorders(g2, w, h, isLastCol, isLastRow, isBoxRight, isBoxBottom, lc, bc, 0.5f, 1.5f)
            if (isSelected) {
                g2.color = JBColor(0xA0A8B0, 0x5A6A7A)
                g2.stroke = BasicStroke(1.0f)
                g2.drawRect(0, 0, w - 1, h - 1)
            }
        } else {
            val lc = JBColor(0xC0C0C0, 0x555555)
            val bc = JBColor(0x888888, 0x888888)
            drawBorders(g2, w, h, isLastCol, isLastRow, isBoxRight, isBoxBottom, lc, bc, 0.5f, 1.8f)
            if (isSelected) {
                g2.color = JBColor(0x7BA6D0, 0x4A7A9A)
                g2.stroke = BasicStroke(1.5f)
                g2.drawRect(0, 0, w - 1, h - 1)
            }
        }

        // ── Number ──
        if (number != 0) {
            val fs = if (disguiseMode) 18 else 20
            val c = when {
                disguiseMode -> JBColor(0x707070, 0xA0A0A0)
                isGiven -> JBColor(0x4A5A6A, 0x90A8C0)
                hasConflict -> JBColor(0x9A6A6A, 0xB08080)
                else -> JBColor(0x5A6A5A, 0x88A888)
            }
            g2.color = c
            g2.font = Font("Consolas", Font.PLAIN, fs)
            val fm = g2.fontMetrics
            val s = number.toString()
            val x = (w - fm.stringWidth(s)) / 2f
            val y = (h + fm.ascent - fm.descent) / 2f
            g2.drawString(s, x, y)
        } else if (notes.isNotEmpty()) {
            drawNotes(g2, w, h)
        }

        g2.dispose()
    }

    private fun drawBorders(g2: Graphics2D, w: Int, h: Int,
                            lastCol: Boolean, lastRow: Boolean,
                            boxRight: Boolean, boxBottom: Boolean,
                            lineColor: Color, boxColor: Color,
                            thin: Float, thick: Float) {
        if (!lastCol) {
            g2.color = if (boxRight) boxColor else lineColor
            g2.stroke = BasicStroke(if (boxRight) thick else thin)
            g2.drawLine(w - 1, 0, w - 1, h)
        }
        if (!lastRow) {
            g2.color = if (boxBottom) boxColor else lineColor
            g2.stroke = BasicStroke(if (boxBottom) thick else thin)
            g2.drawLine(0, h - 1, w, h - 1)
        }
        if (lastCol) {
            g2.color = boxColor; g2.stroke = BasicStroke(thick)
            g2.drawLine(w - 1, 0, w - 1, h)
        }
        if (lastRow) {
            g2.color = boxColor; g2.stroke = BasicStroke(thick)
            g2.drawLine(0, h - 1, w, h - 1)
        }
    }

    private fun drawNotes(g2: Graphics2D, w: Int, h: Int) {
        g2.color = if (disguiseMode) JBColor(0x999999, 0x888888) else JBColor(0x888888, 0xAAAAAA)
        g2.font = Font("Consolas", Font.PLAIN, 9)
        val fm = g2.fontMetrics
        val cw = w / 3f
        val ch = h / 3f
        val pos = mapOf(
            1 to Pair(0,0), 2 to Pair(1,0), 3 to Pair(2,0),
            4 to Pair(0,1), 5 to Pair(1,1), 6 to Pair(2,1),
            7 to Pair(0,2), 8 to Pair(1,2), 9 to Pair(2,2)
        )
        for (n in notes.sorted()) {
            val (gx, gy) = pos[n] ?: continue
            val s = n.toString()
            val x = gx * cw + (cw - fm.stringWidth(s)) / 2f
            val y = gy * ch + (ch + fm.ascent) / 2f
            g2.drawString(s, x, y)
        }
    }
}
