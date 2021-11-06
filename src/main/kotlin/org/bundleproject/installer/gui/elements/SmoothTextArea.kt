package org.bundleproject.installer.gui.elements

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JTextArea
import javax.swing.text.Document

class SmoothTextArea(doc: Document? = null, text: String? = null, rows: Int = 0, columns: Int = 0) : JTextArea(doc, text, rows, columns) {
    override fun paintComponent(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        super.paintComponent(g)
    }
}