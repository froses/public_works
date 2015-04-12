package com.froses.tradcat.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JScrollPane;

import com.froses.table.GenericTable;
import com.froses.table.tablemodels.GenericTableModel;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.JSeparator;

public class TradCatInformatica extends JFrame {

	private JPanel contentPane;
	
	
	private Connection con = null;
	private JPanel panButtons;
	private JButton pbClear;
	private JButton pbExit;
	private JPanel panSearch;
	private JLabel stSearch;
	private JTextField efSearch;
	private JButton pbSearch;
	private JScrollPane spTabResults;
	private GenericTable tabResults;
	
	private ArrayList<String> colNames = null;
	private JMenuBar menuBar;
	private JMenu menFile;
	private JMenuItem miExit;
	private JMenu menHelp;
	private JMenuItem miHelp;
	private JMenuItem miAbout;
	private JSeparator sepHelpAbout;
	
	public ArrayList<String> getColNames() {
		if (colNames == null) {
			colNames = new ArrayList<String>();
			colNames.add("English");
			colNames.add("Català");
			colNames.add("Cat. gram.");
		}
		return colNames;
	}   // getColNames()
	
	private void goHome() {
		getEfSearch().requestFocusInWindow();
	}   // goHome()
	
	private void clearFields() {
		getEfSearch().setText("");
		clearTabResults();
		goHome();
	}   // clearFields()
	
	private void clearTabResults() {
		GenericTableModel model = new GenericTableModel(getColNames(), 0);
		getTabResults().setModel(model);
	}   // clearTabResults()
	
	
	
	public Connection getCon() {
		if (con == null) {
			try {
				Class.forName("org.hsqldb.jdbc.JDBCDriver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
			
			try {
				con = DriverManager.getConnection("jdbc:hsqldb:mem:dictdb", "SA", "");
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
			getPbSearch().setVisible(true);
			getPbClear().setEnabled(true);
			getPbExit().setEnabled(true);
		}
		return con;
	}   // getCon()
	
	public boolean disconnect() {
		if (con == null) {
			return true;
		}
		
		try {
			con.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}   // disconnect()
	
	public boolean createTable() {
		if (getCon() == null) {
			return false;
		}
		String CT = "CREATE TABLE DICT (EN VARCHAR(1000) NOT NULL, CA VARCHAR(1000) NOT NULL, CG VARCHAR(25))";
		String IX = "CREATE INDEX IX_DICT ON DICT (EN)"; 
		String INS = "INSERT INTO DICT (EN, CA, CG) VALUES (?,?,?)";
		Statement stmt = null;
		PreparedStatement pstmt = null;
		
		try {
			stmt = getCon().createStatement();
			stmt.executeUpdate(CT);
			stmt.executeUpdate(IX);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		String glossari = "/sc-glossary.csv";
		glossari = "/tots-glossary.csv";
		glossari = "/Termcat_glossary.csv";
		InputStream is = getClass().getResourceAsStream(glossari);
		try {
			if (is.available() == 0) {
				System.out.println("Zero bytes al fitxer");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		
		try {
			pstmt = getCon().prepareStatement(INS);
		} catch (SQLException e2) {
			e2.printStackTrace();
			return false;
		}
		
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return false;
		}
		BufferedReader br = new BufferedReader(isr);
		String delimit = ";";
		try {
			while (br.ready()) {
				String l = br.readLine();
				if (l == null) {
					break;
				}
				if (l.trim().length() == 0) {
					continue;
				}
				StringTokenizer st = new StringTokenizer(l, delimit);
				String en = st.nextToken();
				String ca = st.nextToken();
				String cg = st.nextToken();
				pstmt.setString(1, en);
				pstmt.setString(2, ca);
				pstmt.setString(3, cg);
				pstmt.executeUpdate();
				getCon().commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return true;
	}   // createTable()
	
	private void search() {
		String w = getEfSearch().getText().trim();
		if (w.length() == 0) {
			showError("Cal entrar un mot per poder buscar-lo.");
			goHome();
		}
		ArrayList<ArrayList<String>> rows = searchWord(w);
		if (rows == null) {
			goHome();
			return;
		}
		if (rows.size() == 0) {
			showInfo("La consulta no ha tornat resultats.");
			goHome();
			return;
		}
		GenericTableModel model = new GenericTableModel(rows, getColNames());
		getTabResults().setModel(model);
		getTabResults().setMaxColumnWidth(0);
		getTabResults().setMaxColumnWidth(1);
		goHome();
		getEfSearch().selectAll();
	}   // search()
	
	private ArrayList<ArrayList<String>> searchWord(String enWord) {
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		String sel = "SELECT EN, CA, CG FROM DICT WHERE EN LIKE '%" + enWord.toLowerCase() + "%' ORDER BY 1";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getCon().createStatement();
			rs = stmt.executeQuery(sel);
			while (rs.next()) {
				ArrayList<String> row = new ArrayList<String>();
				String e = rs.getString(1);
				String c = rs.getString(2);
				String cg = rs.getString(3);
				if (rs.wasNull()) {
					cg = "?";
				}
				row.add(e);
				row.add(c);
				row.add(cg);
				rows.add(row);
			}
			return rows;
		} catch (SQLException e) {
			showError("Error a la consulta: " + e.getMessage());
			return null;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// showError("Error tancant Statement: " + e.getMessage());
				}
			}
		}
	}   // searchWord()
	
	private void showError(String msg) {
		if (msg == null || msg.trim().length() == 0) {
			return;
		}
		JOptionPane.showMessageDialog(this, msg, "Error:", JOptionPane.ERROR_MESSAGE);
	}   // showError()
	
	private void showInfo(String msg) {
		if (msg == null || msg.trim().length() == 0) {
			return;
		}
		JOptionPane.showMessageDialog(this, msg, "Info:", JOptionPane.INFORMATION_MESSAGE);
	}   // showInfo()
	
	private void defineKeys() {
		KeyStroke VK_CLEAR = KeyStroke.getKeyStroke("ESCAPE");
		KeyStroke VK_EXIT = KeyStroke.getKeyStroke("F3");
		KeyStroke VK_HELP = KeyStroke.getKeyStroke("F1");
		
		AbstractAction aClear = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearFields();
			}
		};
		
		AbstractAction aExit = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeApp();
			}
		};
		
		AbstractAction aHelp = new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				help();
			}
		};
		
		InputMap iMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap aMap = getRootPane().getActionMap();
		iMap.put(VK_CLEAR, "aClear");
		iMap.put(VK_EXIT, "aExit");
		iMap.put(VK_HELP, "aHelp");
		aMap.put("aClear", aClear);
		aMap.put("aExit", aExit);
		aMap.put("aHelp", aHelp);
	}   // defineKeys()
	
	private void help() {
		URL urlHelp = TradCatInformatica.class.getResource("/doc/TermInfCat_help.html");
		openInfoDlg(urlHelp, "Ajuda");
		return;
	}   // help()
	
	private void about() {
		URL urlAbout = TradCatInformatica.class.getResource("/doc/TermInfCat_About.html");
		openInfoDlg(urlAbout, "Quant a");
		return;
	}   // about()
	
	private void openInfoDlg(URL url, String dlgTitle) {
		ShowInfoDlg dlg = new ShowInfoDlg();
		dlg.setTitle(dlgTitle);
		dlg.setPage(url);
		dlg.setModal(true);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
		goHome();
		return;
	}   // openInfoDlg()
	
	private void closeApp() {
		disconnect();
		this.setVisible(false);
		this.dispose();
	}   // closeApp()

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TradCatInformatica frame = new TradCatInformatica();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TradCatInformatica() {
		setTitle("Terminologia informàtica");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				clearTabResults();
				createTable();
				defineKeys();
				goHome();
			}
			@Override
			public void windowClosing(WindowEvent e) {
				closeApp();
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		setJMenuBar(getMenuBar_1());
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		contentPane.add(getPanButtons(), BorderLayout.SOUTH);
		contentPane.add(getPanSearch(), BorderLayout.NORTH);
		contentPane.add(getSpTabResults(), BorderLayout.CENTER);
	}

	private JPanel getPanButtons() {
		if (panButtons == null) {
			panButtons = new JPanel();
			panButtons.add(getPbSearch());
			panButtons.add(getPbClear());
			panButtons.add(getPbExit());
		}
		return panButtons;
	}
	private JButton getPbClear() {
		if (pbClear == null) {
			pbClear = new JButton("Netejar");
			pbClear.setEnabled(false);
			pbClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearFields();
				}
			});
			pbClear.setMnemonic('n');
		}
		return pbClear;
	}
	private JButton getPbExit() {
		if (pbExit == null) {
			pbExit = new JButton("Sortir");
			pbExit.setEnabled(false);
			pbExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closeApp();
				}
			});
			pbExit.setMnemonic('s');
		}
		return pbExit;
	}
	private JPanel getPanSearch() {
		if (panSearch == null) {
			panSearch = new JPanel();
			GroupLayout gl_panSearch = new GroupLayout(panSearch);
			gl_panSearch.setHorizontalGroup(
				gl_panSearch.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panSearch.createSequentialGroup()
						.addComponent(getStSearch())
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getEfSearch(), GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
						.addContainerGap())
			);
			gl_panSearch.setVerticalGroup(
				gl_panSearch.createParallelGroup(Alignment.LEADING)
					.addGroup(Alignment.TRAILING, gl_panSearch.createSequentialGroup()
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(gl_panSearch.createParallelGroup(Alignment.BASELINE)
							.addComponent(getStSearch())
							.addComponent(getEfSearch(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
			);
			panSearch.setLayout(gl_panSearch);
		}
		return panSearch;
	}
	private JLabel getStSearch() {
		if (stSearch == null) {
			stSearch = new JLabel("Word to search:");
		}
		return stSearch;
	}
	private JTextField getEfSearch() {
		if (efSearch == null) {
			efSearch = new JTextField();
			efSearch.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					getEfSearch().selectAll();
				}
			});
			efSearch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					search();
				}
			});
			efSearch.setColumns(10);
		}
		return efSearch;
	}
	private JButton getPbSearch() {
		if (pbSearch == null) {
			pbSearch = new JButton("Cercar");
			pbSearch.setVisible(false);
			pbSearch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					search();
				}
			});
			pbSearch.setMnemonic('c');
		}
		return pbSearch;
	}
	private JScrollPane getSpTabResults() {
		if (spTabResults == null) {
			spTabResults = new JScrollPane();
			spTabResults.setViewportView(getTabResults());
		}
		return spTabResults;
	}
	private GenericTable getTabResults() {
		if (tabResults == null) {
			tabResults = new GenericTable();
			tabResults.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}
						int selRow = getTabResults().getSelectedRow();
						if (selRow == -1) {
							return;
						}
						
						String translation = (String) getTabResults().getValueAt(selRow, 1);
						StringSelection selection = new StringSelection(translation);
					    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					    clipboard.setContents(selection, selection);
					}
				}
			);
		}
		return tabResults;
	}
	private JMenuBar getMenuBar_1() {
		if (menuBar == null) {
			menuBar = new JMenuBar();
			menuBar.add(getMenFile());
			menuBar.add(getMenHelp());
		}
		return menuBar;
	}
	private JMenu getMenFile() {
		if (menFile == null) {
			menFile = new JMenu("Fitxer");
			menFile.setMnemonic('f');
			menFile.add(getMiExit());
		}
		return menFile;
	}
	private JMenuItem getMiExit() {
		if (miExit == null) {
			miExit = new JMenuItem("Sortir");
			miExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closeApp();
				}
			});
			miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		}
		return miExit;
	}
	private JMenu getMenHelp() {
		if (menHelp == null) {
			menHelp = new JMenu("Ajuda");
			menHelp.setMnemonic('a');
			menHelp.add(getMiHelp());
			menHelp.add(getSepHelpAbout());
			menHelp.add(getMiAbout());
		}
		return menHelp;
	}
	private JMenuItem getMiHelp() {
		if (miHelp == null) {
			miHelp = new JMenuItem("Ajuda");
			miHelp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					help();
				}
			});
			miHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		}
		return miHelp;
	}
	private JMenuItem getMiAbout() {
		if (miAbout == null) {
			miAbout = new JMenuItem("Quant a");
			miAbout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					about();
				}
			});
		}
		return miAbout;
	}
	private JSeparator getSepHelpAbout() {
		if (sepHelpAbout == null) {
			sepHelpAbout = new JSeparator();
		}
		return sepHelpAbout;
	}
}
