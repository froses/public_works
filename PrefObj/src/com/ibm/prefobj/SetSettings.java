// $Id$

package com.ibm.prefobj;

import java.awt.*;
import java.util.prefs.*;

public class SetSettings
{
  static public void main( String args[] ) throws Exception {
    Preferences prefs = Preferences.userNodeForPackage( SetSettings.class );

    Color color = new Color( 0xff, 0xcc, 0x99 );
    Font font = new Font( "Courier", Font.PLAIN, 24 );

    PrefObj.putObject( prefs, "font", font );
    PrefObj.putObject( prefs, "color", color );

    System.out.println( "Wrote "+font );
    System.out.println( "Wrote "+color );
  }
}
