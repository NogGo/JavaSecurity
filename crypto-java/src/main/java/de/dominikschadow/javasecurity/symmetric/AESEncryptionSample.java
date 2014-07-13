/*
 * Copyright (C) 2014 Dominik Schadow, dominikschadow@gmail.com
 *
 * This file is part of the Java Security project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dominikschadow.javasecurity.symmetric;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Symmetric encryption sample with plain Java. Loads the AES key from the sample keystore, encrypts and decrypts sample text with it.
 * Note that the <code>INITIALIZATION_VECTOR</code> is not stored. One possibility to store it is to prepend it to the encrypted
 * message with a delimiter (all in Base64 encoding): <code>Base64(IV) + DELIMITER + Base64(ENCRYPTED MESSAGE)</code>
 * <p>
 * Uses Google Guava to Base64 print the encrypted message as readable format.
 *
 * @author Dominik Schadow
 */
public class AESEncryptionSample {
    private static final Logger logger = LoggerFactory.getLogger(AESEncryptionSample.class);
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEYSTORE_PATH = "/samples.ks";
    /** Non-secret initialization vector with 16 bytes (publicly exchanged between participants), may be a random number changed every time or a counter. */
    private static final byte[] INITIALIZATION_VECTOR = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9, 3};

    public static void main(String[] args) {
        AESEncryptionSample ses = new AESEncryptionSample();
        final String initialText = "AES encryption sample text";
        final char[] keystorePassword = "samples".toCharArray();
        final String keyAlias = "symmetric-sample";
        final char[] keyPassword = "symmetric-sample".toCharArray();
        final IvParameterSpec iv = new IvParameterSpec(INITIALIZATION_VECTOR);

        try {
            KeyStore ks = ses.loadKeystore(KEYSTORE_PATH, keystorePassword);
            Key key = ses.loadKey(ks, keyAlias, keyPassword);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
            byte[] ciphertext = ses.encrypt(secretKeySpec, iv, initialText);
            byte[] plaintext = ses.decrypt(secretKeySpec, iv, ciphertext);

            ses.printReadableMessages(initialText, ciphertext, plaintext);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException |
                KeyStoreException | CertificateException | UnrecoverableKeyException | InvalidAlgorithmParameterException |
                InvalidKeyException | IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private KeyStore loadKeystore(String keystorePath, char[] keystorePassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        InputStream keystoreStream = getClass().getResourceAsStream(keystorePath);

        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(keystoreStream, keystorePassword);

        return ks;
    }

    private Key loadKey(KeyStore ks, String keyAlias, char[] keyPassword) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        if (!ks.containsAlias(keyAlias)) {
            throw new RuntimeException("Secret key " + keyAlias + " not found in keystore");
        }

        Key key = ks.getKey(keyAlias, keyPassword);

        return key;
    }

    private byte[] encrypt(SecretKeySpec secretKeySpec, IvParameterSpec initialVector, String initialText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, BadPaddingException,
            IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initialVector);
        byte[] encryptedMessage = cipher.doFinal(initialText.getBytes("UTF-8"));

        return encryptedMessage;
    }

    private byte[] decrypt(SecretKeySpec secretKeySpec, IvParameterSpec initialVector, byte[] ciphertext)
            throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initialVector);
        byte[] decryptedTextBytes = cipher.doFinal(ciphertext);

        return decryptedTextBytes;
    }

    private void printReadableMessages(String initialText, byte[] ciphertext, byte[] plaintext) {
        logger.info("initialText: {}", initialText);
        logger.info("cipherText as byte[]: {}", new String(ciphertext));
        logger.info("cipherText as Base64: {}", BaseEncoding.base64().encode(ciphertext));
        logger.info("plaintext: {}", new String(plaintext));
    }
}
