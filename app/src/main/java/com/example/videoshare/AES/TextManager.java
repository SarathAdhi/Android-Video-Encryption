package com.example.videoshare.AES;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TextManager {

  private static String encode(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }

  private static byte[] decode(String data) {
    return Base64.getDecoder().decode(data);
  }

  public static String encrypt(String message, String encodedKey) {
    try {
      Cipher cipher = Cipher.getInstance("AES");
      byte[] customKey = encodedKey.getBytes(StandardCharsets.UTF_8);

      SecretKey secretKey = new SecretKeySpec(customKey, "AES");

      cipher.init(Cipher.ENCRYPT_MODE, secretKey);

      byte[] messageToBytes = message.getBytes();
      byte[] encryptedBytes = cipher.doFinal(messageToBytes);

      return encode(encryptedBytes);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public static String decrypt(String encryptedMessage, String encodedKey) {
    try {
      Cipher cipher = Cipher.getInstance("AES");
      byte[] decodedKey = Base64.getDecoder().decode(encodedKey);

      Key originalKey = new SecretKeySpec(
        decodedKey,
        0,
        decodedKey.length,
        "AES"
      );

      cipher.init(Cipher.DECRYPT_MODE, originalKey);

      byte[] encryptedBytes = decode(encryptedMessage);
      byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
      return new String(decryptedMessage, "UTF8");
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
