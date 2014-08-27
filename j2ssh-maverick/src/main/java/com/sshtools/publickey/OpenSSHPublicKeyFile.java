
package com.sshtools.publickey;

import java.io.IOException;

import com.sshtools.ssh.SshException;
import com.sshtools.ssh.components.SshPublicKey;
import com.sshtools.util.Base64;

public class OpenSSHPublicKeyFile
   implements SshPublicKeyFile {

  byte[] formattedkey;
  String comment;

  OpenSSHPublicKeyFile(byte[] formattedkey)
     throws IOException {
    this.formattedkey = formattedkey;
    toPublicKey(); // To validate
  }

  OpenSSHPublicKeyFile(SshPublicKey key, String comment)
     throws IOException {

    try {
      String formatted = key.getAlgorithm() + " " +
         Base64.encodeBytes(key.getEncoded(), true);

      if(comment != null) {
        formatted += (" " + comment);
      }

      formattedkey = formatted.getBytes();
    }
    catch(SshException ex) {
      throw new IOException("Failed to encode public key");
    }
  }

  public String toString() {
    return new String(formattedkey);
  }

  public byte[] getFormattedKey() {
    return formattedkey;
  }

  public SshPublicKey toPublicKey()
     throws IOException {

    String temp = new String(formattedkey);

    // Get the algorithm name end index
    int i = temp.indexOf(" ");

    if(i > 0) {
      String algorithm = temp.substring(0, i);

      
        // Get the keyblob end index
        int i2 = temp.indexOf(" ", i + 1);

        String encoded;
        if(i2 !=-1) {
          encoded = temp.substring(i + 1, i2);

          if(temp.length() > i2) {
            comment = temp.substring(i2 + 1).trim();
          }

          return SshPublicKeyFileFactory.decodeSSH2PublicKey(algorithm, Base64.decode(encoded));

        }
		encoded = temp.substring(i + 1);
		return SshPublicKeyFileFactory.decodeSSH2PublicKey(algorithm, Base64.decode(encoded));
    }

    throw new IOException("Key format not supported!");
  }

  public String getComment() {
    return comment;
  }

}