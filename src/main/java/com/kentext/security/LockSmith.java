package com.kentext.security;

import com.kentext.common.Common;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

final class LockSmith implements Common
{
    static protected final String CIPHER_ALGORITHM = "RSA";

    static protected final String PRIVATE_KEY = String.format(
            "%s%s%s%s%s",
            DATA_DIRECTORY, java.io.File.separator, "keys", java.io.File.separator, "private.key"
    );
    static protected final String PUBLIC_KEY = String.format(
            "%s%s%s%s%s",
            DATA_DIRECTORY, java.io.File.separator, "keys", java.io.File.separator, "public.key"
    );

    private final int KEY_SIZE = 1024;
//    private final int KEY_SIZE = 9999;

    private KeyPairGenerator keyGen;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Instantiate the LockSmith
     *
     * @throws NoSuchAlgorithmException Missing Cipher algorithm
     */
    protected LockSmith() throws NoSuchAlgorithmException
    {
        this.keyGen = KeyPairGenerator.getInstance(CIPHER_ALGORITHM);
        this.keyGen.initialize(KEY_SIZE);

        File privateKeyFile = new File(PRIVATE_KEY);
        File publicKeyFile = new File(PUBLIC_KEY);

        if (!publicKeyFile.exists() || !privateKeyFile.exists())
        {
            /*
             * Create the necessary file directory structures to hold the keys
             */
            publicKeyFile.getParentFile().mkdirs();
            privateKeyFile.getParentFile().mkdirs();

            try
            {
                createKeys();
                writeToFile(PUBLIC_KEY, getPublicKey().getEncoded());
                writeToFile(PRIVATE_KEY, getPrivateKey().getEncoded());
            }
            catch (IOException ex)
            {
                LOGGER.severe(ex.getMessage());

                /*
                 * We would not be able to encrypt the data. There is no need to proceed
                 */
                
                // DELETE The silos
                File dataDirectory = new File(DATA_DIRECTORY);
                dataDirectory.delete();
                
                System.exit(1);
            }
        }
    }

    /**
     * Create the private and public keys, storing them into local files
     */
    private void createKeys()
    {
        LOGGER.info("Generating encryption keys");
        // TODO: Once the keys are generated, deleting them causes a decryption error. Fix this

        KeyPair pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();

        LOGGER.info("Encryption keys generated successfully");
    }

    /**
     * Extracts the private key from the key pair
     *
     * @return The private key
     */
    private PrivateKey getPrivateKey()
    {
        return this.privateKey;
    }

    /**
     * Extracts the public key from the key pair
     *
     * @return The public key
     */
    private PublicKey getPublicKey()
    {
        return this.publicKey;
    }

    /**
     * Write data to file
     *
     * @param path The file into which the data is to be written
     * @param data The data to be written
     * @throws IOException Exception during the file writing operation
     */
    private void writeToFile(String path, byte[] data) throws IOException
    {
        File file = new File(path);
        file.getParentFile().mkdirs();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file))
        {
            fileOutputStream.write(data);
            fileOutputStream.flush();
        }
    }
}
