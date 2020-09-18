package sample;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Random;

public class PGP {
    AES aes;
    DSA dsa;
    public PGP()
    {
        aes = new AES();
        dsa = new DSA();
    }


    /**
     * The message is created like so:
     *    - Generates a random KeyPair
     *    - Encrypt the message with the private key from the generated key pair
     *    - Encrypt the generated public key with given public key
     *
     * @param message The message to encrypt
     * @param key The key to encrypt with
     * @return The encrypted message
     * @throws GeneralSecurityException
     */
    public byte[] encrypt(byte[] message, PublicKey key) throws GeneralSecurityException {
        byte [] session‌_key_bytes = new byte[16];
        new Random().nextBytes(session‌_key_bytes);
        String session‌_key = new String(session‌_key_bytes);
        aes.setKeyValue(session‌_key);
        String encryptedMessage = null;
        try {
            encryptedMessage = aes.encrypt(new String(message));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedPublicKey = cipher.doFinal(session‌_key_bytes);

        ByteBuffer buffer = ByteBuffer.allocate((encryptedPublicKey.length + encryptedMessage.getBytes().length) + 4);
        buffer.putInt(encryptedPublicKey.length);
        buffer.put(encryptedPublicKey);
        buffer.put(encryptedMessage.getBytes());
        return buffer.array();
    }



}
