package com.example.videoshare.RSA;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

public class KeyStoreHelper {

  public static String PREFS_NAME = "MY_SECRET_KEY_PREFS";
  public static String PREFS_KEY_PRIVATE = "MY_SECRET_PRIVATE_KEY";
  public static String PREFS_KEY_PUBLIC = "MY_SECRET_PUBLIC_KEY";
  private static String myPublicKey;
  private static String myPrivateKey;

  public static void init(Context context) {
    try {
      // Generate RSA key pair (public and private keys)
      KeyPair keyPair = generateKeyPair();
      PublicKey publicKey = keyPair.getPublic();
      PrivateKey privateKey = keyPair.getPrivate();

      myPublicKey = encode(publicKey);
      myPrivateKey = encode(privateKey);

      SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString(PREFS_KEY_PRIVATE, myPrivateKey);
      editor.putString(PREFS_KEY_PUBLIC, myPublicKey);

      editor.apply();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void storeKeys(
    Context context,
    String publicKey,
    String privateKey
  ) {
    try {
      SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();

      editor.putString(PREFS_KEY_PRIVATE, privateKey);
      editor.putString(PREFS_KEY_PUBLIC, publicKey);

      editor.apply();
    } catch (Exception e) {}
  }

  // Generate an RSA key pair
  private static KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048); // Key size, adjust as needed
    return keyPairGenerator.generateKeyPair();
  }

  private static String encode(Key pKey) {
    return Base64.getEncoder().encodeToString(pKey.getEncoded());
  }

  public static String getPublicKey(Context context) {
    SharedPreferences pref = context.getSharedPreferences(
      KeyStoreHelper.PREFS_NAME,
      0
    );
    //        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    String publicKey = pref.getString(PREFS_KEY_PUBLIC, null);
    myPublicKey = publicKey;

    return myPublicKey;
  }

  public static String getPrivateKey(Context context) {
    SharedPreferences pref = context.getSharedPreferences(
      KeyStoreHelper.PREFS_NAME,
      0
    );

    //        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    String privateKey = pref.getString(PREFS_KEY_PRIVATE, null);
    myPrivateKey = privateKey;

    return myPrivateKey;
  }
}
