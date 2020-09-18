package sample;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * Code written by P. Gajland
 * https://github.com/GaPhil
 *
 * IMPORTANT:
 * This code is for educational and demonstrative purpose only.
 * If you need to do serious encryption for "production" it is
 * recommended to investigate more traditional libraries and
 * gain some specific knowledge on cryptography and security.
 */


public class AES {

    private static final String ALGO = "AES";
    private byte[] keyValue;

    public AES(){
        keyValue = new byte[]{'T','h','e','B','e','s','t','S','e','c','r','e','t','K','e','y'};
    }
    public AES(String keyValue) {
        this.keyValue = keyValue.getBytes();
    }

    public void setKeyValue(String keyValue) {
        for (int i = 0; i<16; i++)
            this.keyValue[i]= (byte) keyValue.charAt(i%keyValue.length());
    }

    /**
     * Decrypt a string with AES algorithm.
     *
     * @param encryptedData is a string
     * @return the decrypted string
     */
    public String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        return new String(decValue);
    }

    /**
     * Generate a new encryption key.
     */
    private Key generateKey() throws Exception {
        return new SecretKeySpec(keyValue, ALGO);
    }
}


