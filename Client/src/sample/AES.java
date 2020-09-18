package sample;

import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;



public class AES {

    private static final String ALGO = "AES";
    private byte[] keyValue;

    public AES(){
        keyValue = new byte[]{'T','h','e','B','e','s','t','S','e','c','r','e','t','K','e','y'};
    }

    public void setKeyValue(String keyValue) {
        for (int i = 0; i<16; i++)
            this.keyValue[i]= (byte) keyValue.charAt(i%keyValue.length());
    }


    public String encrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes());
        return new BASE64Encoder().encode(encVal);
    }

    public String encrypt(byte[] data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data);
        return new BASE64Encoder().encode(encVal);
    }

    /**
     * Generate a new encryption key.
     */
    private Key generateKey() throws Exception {
        return new SecretKeySpec(keyValue, ALGO);
    }
}


