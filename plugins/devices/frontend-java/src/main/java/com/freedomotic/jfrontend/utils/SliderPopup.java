/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.jfrontend.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;

import com.freedomotic.behaviors.RangedIntBehaviorLogic;

/**
 *
 * @author Mauro Cicolella
 */
@SuppressWarnings("serial")
public class SliderPopup extends JPopupMenu implements ActionListener,
		PropertyChangeListener, MouseListener {

	private JSlider slider;
	private RangedIntBehaviorLogic rib;
	private JFormattedTextField txtValue;

    /**
     *
     * @param slider
     * @param rib
     */
    public SliderPopup(JSlider slider, RangedIntBehaviorLogic rib) {
		super();
		this.slider = slider;
		this.rib = rib;

		txtValue = new JFormattedTextField(NumberFormat.getInstance());
		txtValue.setColumns(10);
		txtValue.addActionListener(this);
		add(txtValue);
		addPropertyChangeListener("visible", this);
	}

    /**
     *
     * @param val
     */
    public void setValue(int val) {
		txtValue.setValue((double) val / rib.getScale());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// we got a number, convert to scale
		int val = (int) (((Number) txtValue.getValue()).doubleValue() * rib
				.getScale());
		// set slider value; slider and filterParams will take care of checks
		slider.setValue(val);
		setVisible(false);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// when it becomes visible, set cursor on the textbox
		if (evt.getNewValue().equals(true))
			txtValue.requestFocus();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		checkIfPopup(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		checkIfPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		checkIfPopup(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	private void checkIfPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			if (slider.isEnabled()) {
				setValue(slider.getValue());
				show(slider, e.getX(), e.getY());
			}
		}
	}
}
