/**
 * 
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
 * 
 * @author Nathan F Syfrig
 *
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
