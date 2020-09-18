package sample;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.*;

public class PGP {
    AES aes;
    public PGP()
    {
        aes = new AES();
    }


    /**
     * The message is decrypted like so:
     *    - Read the encrypted public key
     *    - Decrypt the public key with the private key
     *    - Read the encrypted message
     *    - Use the decrypted public key to decrypt the encrypted message
     *
     * @param message The encrypted message
     * @param key The private key
     * @return The decrypted message
     * @throws GeneralSecurityException
     */
    public byte[] decrypt(byte[] message, PrivateKey key) throws GeneralSecurityException {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        int keyLength = buffer.getInt();
        byte[] encyptedPublicKey = new byte[keyLength];
        buffer.get(encyptedPublicKey);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] encodedPublicKey = cipher.doFinal(encyptedPublicKey);

        aes.setKeyValue(new String(encodedPublicKey));
        byte[] encryptedMessage = new byte[buffer.remaining()];
        buffer.get(encryptedMessage);
        String decrypt_message = "";
        try {
            decrypt_message = aes.decrypt(new String(encryptedMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypt_message.getBytes();
    }


    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
        return keyPairGenerator.generateKeyPair();
    }


}
