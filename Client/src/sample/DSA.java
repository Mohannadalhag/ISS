package sample;

import java.security.*;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

public class DSA {
  public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("DSA");
    keyGenerator.initialize(1024);
    return keyGenerator.genKeyPair();
  }

  public static byte[] sign(PrivateKey privateKey, byte[] message)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signAlgorithm = Signature.getInstance("DSA");
    
    signAlgorithm.initSign(privateKey);
    signAlgorithm.update(message);
    
    return signAlgorithm.sign();
  }
  public void setPublic(byte []keyBytes) throws Exception {
    X509EncodedKeySpec ks = new X509EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("DSA");
    PublicKey PublicKey = kf.generatePublic(ks);
  }

}
