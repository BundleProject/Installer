package org.bundleproject.installer.gui.elements

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.ComboBoxModel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class SmoothComboBox<E>(model: ComboBoxModel<E> = DefaultComboBoxModel()) : JComboBox<E>(model) {
    override fun paintComponent(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        super.paintComponent(g)
    }
}