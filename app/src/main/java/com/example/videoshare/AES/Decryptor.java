package com.example.videoshare.AES;

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

public class Decryptor {
    public static void decryption(File encryptedFile, String encodedKey, String androidDownloadPath, String name)
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

            cipher.init(Cipher.DECRYPT_MODE, originalKey);

            FileInputStream encryptedFileStream = new FileInputStream(
                    encryptedFile
            );

            File decryptFile = new File(androidDownloadPath + "/decrypted-" + name);

            FileOutputStream decryptedFileStream = new FileOutputStream(
                    decryptFile
            );

            System.out.println(decryptFile.getPath());

            try ( // Create a CipherInputStream to decrypt the video file
                  CipherInputStream cipherInputStream = new CipherInputStream(
                          encryptedFileStream,
                          cipher
                  )
            ) {
                // Read from the encrypted video file and write to the decrypted file
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                    decryptedFileStream.write(buffer, 0, bytesRead);
                }
            }

            // Close streams
            encryptedFileStream.close();
            decryptedFileStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
