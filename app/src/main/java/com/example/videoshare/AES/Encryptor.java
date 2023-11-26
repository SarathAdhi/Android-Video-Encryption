package com.example.videoshare.AES;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {
    public static String generateKey() {
        try {
            KeyGenerator keyg = KeyGenerator.getInstance("AES");
            Key key = keyg.generateKey();

            String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());

            return encodedKey;
        } catch (Exception e) {
            return "";
        }
    }

    public static void encryption(File filesDir, File myFile, String uuid, String encodedKey)
//            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
    {
        try {
            Cipher cipher = Cipher.getInstance("AES");

            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);

            Key originalKey = new SecretKeySpec(
                    decodedKey,
                    0,
                    decodedKey.length,
                    "AES"
            );

            cipher.init(Cipher.ENCRYPT_MODE, originalKey);


            FileInputStream inputFileStream = new FileInputStream(myFile);

            FileOutputStream encryptedFileStream = new FileOutputStream(
                    new File(filesDir + "/" + uuid + ".txt")
            );

            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    encryptedFileStream,
                    cipher
            );

            // Read from the input video file and write to the encrypted file
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputFileStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }


            // Close streams
            inputFileStream.close();
            cipherOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
