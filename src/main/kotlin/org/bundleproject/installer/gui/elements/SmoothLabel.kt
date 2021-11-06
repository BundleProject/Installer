package org.bundleproject.installer.gui.elements

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon
import javax.swing.JLabel

class SmoothLabel(text: String? = "", icon: Icon? = null, horizontalAlignment: Int = 0) : JLabel(text, icon, horizontalAlignment) {
    override fun paintComponent(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        super.paintComponent(g)
    }
}