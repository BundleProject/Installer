package org.bundleproject.installer.gui.elements

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JTextField
import javax.swing.text.Document

class SmoothTextField(doc: Document? = null, text: String? = null, columns: Int = 0) : JTextField(doc, text, columns) {
    override fun paintComponent(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        super.paintComponent(g)
    }
}