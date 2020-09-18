package sample;

import java.io.File;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class DSA {
  PublicKey PublicKey;
  byte[] signature;

  public boolean verify(byte[] message)
          throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature verifyAlgorithm = Signature.getInstance("DSA");

    verifyAlgorithm.initVerify(PublicKey);
    verifyAlgorithm.update(message);

    return !verifyAlgorithm.verify(signature);
  }

  public void setPublic(byte []keyBytes) throws Exception {
    X509EncodedKeySpec ks = new X509EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("DSA");
    PublicKey = kf.generatePublic(ks);
  }

  public void setSignature(byte[] signature) {
    this.signature = signature;
  }
}
