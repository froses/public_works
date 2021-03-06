// $Id$

package com.ibm.prefobj;

import java.io.*;
import java.util.prefs.*;

/**
 * This class turns an Object into a byte array (and conversely). 
 * The Object can thus be stored using the Preferences API. By Greg Travis (https://www.ibm.com/developerworks/library/j-prefapi/index.html).
 * @author Greg Travis
 */
public class PrefObj
{
  // Max byte count is 3/4 max string length (see Preferences
  // documentation).
  static private final int pieceLength =
    ((3*Preferences.MAX_VALUE_LENGTH)/4);

  /**
   * <p>
   * Turns an Object into a Byte array,
   * </p>
   * <p>
   * The reason for doing this is simple: although the Preferences 
   * object does not handle objects, it does handle byte arrays.
   * </p>
   * @param o <code>Object</code> - Object to be converted.
   * @return <code>byte[]</code> - Byte array containing the object.
   * @throws IOException Error.
   */
  static private byte[] object2Bytes( Object o ) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream( baos );
    oos.writeObject( o );
    return baos.toByteArray();
  }

  /**
   * <p>
   * Breaks a Byte array containing an Object into pieces.
   * </p>
   * <p>
   * The Preferences API imposes a limit on the size of the data that you can store in it. 
   * In particular, strings are limited to MAX_VALUE_LENGTH characters. Byte arrays are 
   * limited in length to 75 percent of MAX_VALUE_LENGTH because byte arrays are stored 
   * by encoding them as strings.An object, on the other hand, can be of arbitrary size, 
   * so we need to break it into pieces.</br> 
   * Of course, the easiest way to do this is to first convert it to a byte array and 
   * then break the byte array into pieces. 
   * </p>
   * @param raw <code>byte[]</code> Byte array containing an Object.
   * @return <code>byte[][]</code> An array of array pieces.
   */
  static private byte[][] breakIntoPieces( byte raw[] ) {
    int numPieces = (raw.length + pieceLength - 1) / pieceLength;
    byte pieces[][] = new byte[numPieces][];
    for (int i=0; i<numPieces; ++i) {
      int startByte = i * pieceLength;
      int endByte = startByte + pieceLength;
      if (endByte > raw.length) endByte = raw.length;
      int length = endByte - startByte;
      pieces[i] = new byte[length];
      System.arraycopy( raw, startByte, pieces[i], 0, length );
    }
    return pieces;
  }

  static private void writePieces( Preferences prefs, String key,
      byte pieces[][] ) throws BackingStoreException {
    Preferences node = prefs.node( key );
    node.clear();
    for (int i=0; i<pieces.length; ++i) {
      node.putByteArray( ""+i, pieces[i] );
    }
  }

  static private byte[][] readPieces( Preferences prefs, String key )
      throws BackingStoreException {
    Preferences node = prefs.node( key );
    String keys[] = node.keys();
    int numPieces = keys.length;
    byte pieces[][] = new byte[numPieces][];
    for (int i=0; i<numPieces; ++i) {
      pieces[i] = node.getByteArray( ""+i, null );
    }
    return pieces;
  }

  /**
   * <p>
   * Combines an array of pieces into a single Byte array containing the original object. 
   * </p>
   * <p>
   * The Preferences API imposes a limit on the size of the data that you can store in it. 
   * In particular, strings are limited to MAX_VALUE_LENGTH characters. Byte arrays are 
   * limited in length to 75 percent of MAX_VALUE_LENGTH because byte arrays are stored by encoding them as strings.
   * An object, on the other hand, can be of arbitrary size, so we need to break it into pieces. Of course, 
   * the easiest way to do this is to first convert it to a byte array and then break the byte array into pieces.
   * </p>
   * @param pieces <code>byte[][]</code> - Set of pieces to be combined.
   * @return <code>byte[]</code> - The Byte array containing the original object.
   */
  static private byte[] combinePieces( byte pieces[][] ) {
    int length = 0;
    for (int i=0; i<pieces.length; ++i) {
      length += pieces[i].length;
    }
    byte raw[] = new byte[length];
    int cursor = 0;
    for (int i=0; i<pieces.length; ++i) {
      System.arraycopy( pieces[i], 0, raw, cursor, pieces[i].length );
      cursor += pieces[i].length;
    }
    return raw;
  }

  /**
   * <p>
   * Converts a Byte array containing an Object to the Object.
   * </p>
   * @param raw <code>byte[]</code> - Byte Array containing the Object.
   * @return <code>Object</code> - The byte array conversion resulting object.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  static private Object bytes2Object( byte raw[] )
      throws IOException, ClassNotFoundException {
    ByteArrayInputStream bais = new ByteArrayInputStream( raw );
    ObjectInputStream ois = new ObjectInputStream( bais );
    Object o = ois.readObject();
    return o;
  }

  /**
   * <p>
   * Method putObject() breaks the entire process into three steps:
   * </p>
   * <ol>
   * <li>Converts the object to a byte array (Listing 3).</li>
   * <li>Breaks the array into smaller arrays (Listing 5).</li> 
   * <li>Writes the pieces to the Preferences API.</li>
   * </ol>
   * @param prefs <code>Preferences</code> - Prefereces instance used to put in the object. 
   * @param key <code>String</code>  - Key used to put the object.
   * @param o <code>Object</code> - Object to be put into the specified Preferences instance.
   * @throws IOException
   * @throws BackingStoreException
   * @throws ClassNotFoundException
   */
  static public void putObject( Preferences prefs, String key, Object o )
      throws IOException, BackingStoreException, ClassNotFoundException {
    byte raw[] = object2Bytes( o );
    byte pieces[][] = breakIntoPieces( raw );
    writePieces( prefs, key, pieces );
  }

  static public Object getObject( Preferences prefs, String key )
      throws IOException, BackingStoreException, ClassNotFoundException {
    byte pieces[][] = readPieces( prefs, key );
    byte raw[] = combinePieces( pieces );
    Object o = bytes2Object( raw );
    return o;
  }
}
