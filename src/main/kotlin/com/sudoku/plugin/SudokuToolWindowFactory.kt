package com.sudoku.plugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.sudoku.plugin.ui.SudokuPanel

class SudokuToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val sudokuPanel = SudokuPanel()
        val content = ContentFactory.getInstance().createContent(sudokuPanel.mainPanel, "", false)

        // 窗口关闭时释放资源
        val disposable = Disposable { sudokuPanel.dispose() }
        Disposer.register(content, disposable)

        toolWindow.contentManager.addContent(content)
    }
}
