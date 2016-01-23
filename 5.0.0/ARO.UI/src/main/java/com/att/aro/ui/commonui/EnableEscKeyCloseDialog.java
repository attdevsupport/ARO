/**
 * Copyright 2016 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.att.aro.ui.commonui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 * Quick common way of enablinb the Esc key to close a dialog window.
 */
public class EnableEscKeyCloseDialog {
	public static final String ESCAPE_KEY = "ESCAPE_KEY";

	private final boolean disposeIt;

	private AtomicBoolean escPressed = new AtomicBoolean();

	public EnableEscKeyCloseDialog(JRootPane rootPane, Window disposeFrame, boolean disposeIt) {
		this.disposeIt = disposeIt;
		enableEscKeyCloseDialog(rootPane, disposeFrame);
	}
	public EnableEscKeyCloseDialog(JRootPane rootPane, Window disposeFrame) {
		this(rootPane, disposeFrame, true);
	}

	public boolean consumeEscPressed() {
		return escPressed.getAndSet(false);
	}

	private void enableEscKeyCloseDialog(JRootPane rootPane, final Window disposeFrame) {
		rootPane.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESCAPE_KEY);
		rootPane.getRootPane().getActionMap().put(ESCAPE_KEY, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				escPressed.set(true);
				if (disposeIt) {
					disposeFrame.dispose();
				}
				else {
					disposeFrame.setVisible(false);
				}
			}
		});
	}
}
