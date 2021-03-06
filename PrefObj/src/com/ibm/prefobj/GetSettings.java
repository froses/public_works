// $Id$

package com.ibm.prefobj;

import java.awt.*;
import java.util.prefs.*;

/**
 * <p>
 * Sample class showing how to recover a Java Object previously saved in 
 * a Preferences node.
 * </p>
 * @author francesc
 *
 */
public class GetSettings {
	static public void main(String args[]) throws Exception {
		java.lang.Class clazz = GetSettings.class;
		Preferences prefs = Preferences.userNodeForPackage(clazz);

		Font font = (Font) PrefObj.getObject(prefs, "font");
		Color color = (Color) PrefObj.getObject(prefs, "color");

		System.out.println("Read " + font);
		System.out.println("Read " + color);
	}
}
