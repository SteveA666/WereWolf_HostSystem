package ui;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Font;

public class LogPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JTextArea logArea;

	public LogPanel() {
		super(new BorderLayout());

		logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

		JScrollPane scrollPane = new JScrollPane(logArea);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	public void log(String message) {
		logArea.append(message + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	public void clear() {
		logArea.setText("");
		logArea.setCaretPosition(0);
	}
}

