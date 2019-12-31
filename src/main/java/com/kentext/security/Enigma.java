package com.kentext.security;

import com.kentext.common.Common;
import static com.kentext.security.LockSmith.CIPHER_ALGORITHM;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static java.nio.file.Files.readAllBytes;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;

/**
 * Encrypt and decrypt strings and texts
 */
public final class Enigma implements Common, AutoCloseable
{
    private final Cipher cipher;

    /**
     * Instantiate the class and encryption algorithm
     *
     * @throws NoSuchAlgorithmException An exception with the CIPHER_ALGORITHM in place
     * @throws NoSuchPaddingException An exception with the CIPHER_ALGORITHM in place
     */
    public Enigma() throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        /**
         * Initialize a LockSmith object in order to create any keys that may be missing
         */
        LockSmith lockSmith = new LockSmith();

        this.cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    }

    /**
     * Provide a PrivateKey from the encryption algorithm according to the details at
     * https://docs.oracle.com/javase/8/docs/api/java/security/spec/PKCS8EncodedKeySpec.html
     *
     * @return  a PrivateKey that can be used for data decryption
     * @throws IOException Input/output exception arising from reading the key file
     * @throws InvalidKeySpecException The key file not being a valid key file
     * @throws NoSuchAlgorithmException An exception with the CIPHER_ALGORITHM in place
     */
    private PrivateKey getPrivate()
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException
    {
        // The public key at any one time will be LockSmith.PRIVATE_KEY
        byte[] keyBytes = readAllBytes(new File(LockSmith.PRIVATE_KEY).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(CIPHER_ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }

    /**
     * Provide a PublicKey from the encryption algorithm according to the details at
     * https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
     *
     * @return a PublicKey for data encryption
     * @throws IOException Input/output exception arising from reading the key file
     * @throws NoSuchAlgorithmException An exception with the CIPHER_ALGORITHM in place
     * @throws InvalidKeySpecException The key file not being a valid key file
     */
    private PublicKey getPublic()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        // The public key at any one time will be LockSmith.PUBLIC_KEY
        byte[] keyBytes = readAllBytes(new File(LockSmith.PUBLIC_KEY).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(CIPHER_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }

    /**
     * Writes a given set of data into a specified file
     *
     * @param output  The file to be written
     * @param toWrite The data into which to write the file
     * @throws IOException Input/output exception arising from reading the key file
     */
    private void writeToFile(File output, byte[] toWrite)
            throws IOException
    {
        try (FileOutputStream fileOutputStream = new FileOutputStream(output))
        {
            fileOutputStream.write(toWrite);
            fileOutputStream.flush();
        }
    }

    /**
     * Encrypt a given string of text
     *
     * @param msg The string to be encrypted
     * 
     * @throws IllegalBlockSizeException Exception with the block size of the msg
     * @throws BadPaddingException Exception with the passing size of the msg
     * @throws InvalidKeyException The key file not being a valid key file
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     * 
     * @return The encrypted text
     */
    public String encryptText(String msg)
            throws IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, IOException
    {
        this.cipher.init(Cipher.ENCRYPT_MODE, getPrivate());

        return Base64.encodeBase64String(cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Decrypt a given cryptic text
     *
     * @param msg The cryptic text that is to be decrypted
     * 
     * @throws InvalidKeyException The key file not being a valid key file
     * @throws IllegalBlockSizeException Exception with the block size of the message
     * @throws BadPaddingException Exception with the padding size of the message
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     * @throws java.security.spec.InvalidKeySpecException
     * 
     * @return The decrypted text
     * 
     */
    public String decryptText(String msg)
            throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, IOException, InvalidKeySpecException
    {
        this.cipher.init(Cipher.DECRYPT_MODE, getPublic());
        return new String(cipher.doFinal(Base64.decodeBase64(msg)), StandardCharsets.UTF_8);
    }

    /**
     * Encrypt a file
     *
     * @param input  The file to be encrypted
     * @param output The encrypted file that is the output of the encryption process
     * @throws IOException Input/output exception arising from reading the key file
     * @throws GeneralSecurityException A general exception with the security during encryption
     */
    public void encryptFile(byte[] input, File output)
            throws IOException, GeneralSecurityException
    {
        this.cipher.init(Cipher.ENCRYPT_MODE, getPrivate());
        writeToFile(output, this.cipher.doFinal(input));
    }

    /**
     * Decrypt a file
     *
     * @param input  The file to be decrypted
     * @param output The decrypted file that is the output of the decryption process
     * @throws IOException Input/output exception arising from reading the key file
     * @throws GeneralSecurityException A general exception with the security during decryption
     */
    void decryptFile(byte[] input, File output)
            throws IOException, GeneralSecurityException
    {
        this.cipher.init(Cipher.DECRYPT_MODE, getPublic());
        writeToFile(output, this.cipher.doFinal(input));
    }

    @Override
    public void close() throws Exception
    {
//        throw new Exception("Error with encryption mechanism. Cannot proceed");
    }
}
