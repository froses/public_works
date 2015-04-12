package com.froses.tradcat.gui;

import java.awt.Desktop;
import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * <p>
 * Just a dialog to show info in HTML format.
 * </p>
 * @author Francesc
 *
 */
public class ShowInfoDlg extends JDialog {
	private JPanel panButtons;
	private JButton pbClose;
	private JPanel panFields;
	private JScrollPane spInfo;
	private JEditorPane taInfo;
	
	public void setPage(URL url) {
		try {
			getTaInfo().setPage(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setInfoRTF(InputStream is) {
		setInfo(is, "text/rtf");
	}
	
	public void setInfoHTML(InputStream is) {
		setInfo(is, "text/html");
	}   // setInfoHTML()
	
	public void setInfo(InputStream is, String contentType) {
		if (contentType == null) {
			throw new IllegalArgumentException("Content type is null.");
		}
		getTaInfo().setContentType(contentType);
		try {
			getTaInfo().read(is, "About");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setInfo(String info) {
		getTaInfo().setContentType("text/html");
		getTaInfo().setText(info);
	}
	
	private void defineKeys() {
		KeyStroke VK_EXIT = KeyStroke.getKeyStroke("ESCAPE");
		AbstractAction aExit = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDlg();
			}
		};
		
		InputMap iMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap aMap = getRootPane().getActionMap();
		iMap.put(VK_EXIT, "aExit");
		aMap.put("aExit", aExit);
	}   // defineKeys()
	
	
	private void closeDlg() {
		this.setVisible(false);
		this.dispose();
	}   // closeDlg()

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ShowInfoDlg dialog = new ShowInfoDlg();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public ShowInfoDlg() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeDlg();
			}
			@Override
			public void windowOpened(WindowEvent e) {
				defineKeys();
			}
		});
		setBounds(100, 100, 727, 521);
		getContentPane().add(getPanButtons(), BorderLayout.SOUTH);
		getContentPane().add(getPanFields(), BorderLayout.CENTER);

	}

	private JPanel getPanButtons() {
		if (panButtons == null) {
			panButtons = new JPanel();
			panButtons.add(getPbClose());
		}
		return panButtons;
	}
	private JButton getPbClose() {
		if (pbClose == null) {
			pbClose = new JButton("Tancar");
			pbClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closeDlg();
				}
			});
			pbClose.setMnemonic('t');
		}
		return pbClose;
	}
	private JPanel getPanFields() {
		if (panFields == null) {
			panFields = new JPanel();
			panFields.setLayout(new BorderLayout(0, 0));
			panFields.add(getSpInfo(), BorderLayout.CENTER);
		}
		return panFields;
	}
	private JScrollPane getSpInfo() {
		if (spInfo == null) {
			spInfo = new JScrollPane();
			spInfo.setViewportView(getTaInfo());
		}
		return spInfo;
	}
	private JEditorPane getTaInfo() {
		if (taInfo == null) {
			taInfo = new JEditorPane();
			taInfo.setEditable(false);
			taInfo.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		                 JEditorPane pane = (JEditorPane) e.getSource();
		                 if (e instanceof HTMLFrameHyperlinkEvent) {
		                     HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
		                     HTMLDocument doc = (HTMLDocument)pane.getDocument();
		                     doc.processHTMLFrameHyperlinkEvent(evt);
		                 } else {
		                     try {
		                    	 Desktop dsk = null;
		                    	 if (Desktop.isDesktopSupported()) {
		                    		 dsk = Desktop.getDesktop();
		                    		 dsk.browse(e.getURL().toURI());
		                    	 }
		                         //pane.setPage(e.getURL());
		                     } catch (Throwable t) {
		                         t.printStackTrace();
		                     }
		                 }
		             }
				}
			});
		}
		return taInfo;
	}
}
