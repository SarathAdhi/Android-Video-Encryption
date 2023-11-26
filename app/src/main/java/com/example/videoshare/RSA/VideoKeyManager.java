package com.example.videoshare.RSA;


import javax.crypto.Cipher;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class VideoKeyManager {

    private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static String encrypt(String message, String publicKey) {
        try {
            byte[] messageToBytes = message.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            PublicKey decodedKey = decodePublicKey(publicKey);
//            System.out.println("IN VIDEOKEYMANAGER: " + decodedKey);


            cipher.init(Cipher.ENCRYPT_MODE, decodedKey);
            byte[] encryptedBytes = cipher.doFinal(messageToBytes);
            return encode(encryptedBytes).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String encryptedMessage, String privateKey) {
        try {
            byte[] encryptedBytes = decode(encryptedMessage);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            cipher.init(Cipher.DECRYPT_MODE, decodePrivateKey(privateKey));
            byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
            return new String(decryptedMessage, "UTF8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PrivateKey decodePrivateKey(String privateEncodedKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] privateDecodedKeyBytes = Base64
                    .getDecoder()
                    .decode(privateEncodedKey);

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                    privateDecodedKeyBytes
            );

            return keyFactory.generatePrivate(privateKeySpec);
        } catch (Exception ignored) {
        }

        return null;
    }

    public static PublicKey decodePublicKey(String publicEncodedKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] publicDecodedKeyBytes = Base64
                    .getDecoder()
                    .decode(publicEncodedKey);

            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                    publicDecodedKeyBytes
            );


            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return null;
    }
}