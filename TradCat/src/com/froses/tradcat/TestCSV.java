package com.froses.tradcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

public class TestCSV {
	
	private Connection con = null;
	
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
	}
	
	public boolean createTable() {
		// 
		// CREATE TEXT TABLE <tablename> (<column definition> [<constraint definition>])
		// SET TABLE <tablename> SOURCE <quoted_filename_and_options> [DESC]
		if (getCon() == null) {
			return false;
		}
		String CT = "CREATE TABLE DICT (EN VARCHAR(1000) NOT NULL, CA VARCHAR(1000) NOT NULL)";
		String IX = "CREATE INDEX IX_DICT ON DICT (EN)"; 
		String INS = "INSERT INTO DICT (EN, CA) VALUES (?,?)";
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
		
		InputStream is = getClass().getResourceAsStream("/sc-glossary.csv");
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
		try {
			while (br.ready()) {
				String l = br.readLine();
				if (l == null) {
					break;
				}
				if (l.trim().length() == 0) {
					continue;
				}
				StringTokenizer st = new StringTokenizer(l, ",");
				String en = st.nextToken();
				String ca = st.nextToken();
				pstmt.setString(1, en);
				pstmt.setString(2, ca);
				pstmt.executeUpdate();
				getCon().commit();
			}
			System.out.println("Inserted!");
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
	}
	
	private boolean search(String enWord) {
		String sel = "SELECT EN, CA FROM DICT WHERE EN LIKE '%" + enWord.toLowerCase() + "%' ORDER BY 1";
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getCon().createStatement();
			rs = stmt.executeQuery(sel);
			while (rs.next()) {
				String e = rs.getString(1);
				String c = rs.getString(2);
				System.out.println(e + "\t" + c);
			}
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}   // search()
	
	public static void main(String[] args) {
		TestCSV app = new TestCSV();
		if (app.getCon() != null) {
			System.out.println("Connected!");
		}
		System.out.println(app.createTable());
		app.search("bo");
		app.disconnect();
	}   // main()

}
