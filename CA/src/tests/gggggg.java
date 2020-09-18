package tests;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class gggggg {
   /* RSAPrivateKeySpec serPrivateSpec = new RSAPrivateKeySpec(
            new BigInteger(*//*val of pub key*//*String.valueOf(11111111)), new BigInteger(*//*val of pri key*//*"555555555555"));
    KeyFactory fact = KeyFactory.getInstance("RSA");
    PrivateKey serverPrivateKey = fact.generatePrivate(serPrivateSpec);

    private Object agentCL;
    RSAPublicKeySpec serPublicSpec = new RSAPublicKeySpec(
            new BigInteger(agentCL.getSerPubMod()), new BigInteger(agentCL.getSerPubExp()));
    PublicKey serverPublicKey = fact.generatePublic(serPublicSpec);

    keyStore = KeyStore.getInstance(IMXAgentCL.STORE_TYPE);
    keyStore.load(null, SOMEPWD.toCharArray());

Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    X509Certificate[] serverChain = new X509Certificate[1];
    X509V3CertificateGenerator serverCertGen = new X509V3CertificateGenerator();
    X500Principal serverSubjectName = new X500Principal("CN=OrganizationName");
serverCertGen.setSerialNumber(new BigInteger("123456789") throws NoSuchAlgorithmException, InvalidKeySpecException);
// X509Certificate caCert=null;
serverCertGen.setIssuerDN(somename);
serverCertGen.setNotBefore(new Date() throws NoSuchAlgorithmException, InvalidKeySpecException);
serverCertGen.setNotAfter(new Date() throws NoSuchAlgorithmException, InvalidKeySpecException);
serverCertGen.setSubjectDN(somename);
serverCertGen.setPublicKey(serverPublicKey);
serverCertGen.setSignatureAlgorithm("MD5WithRSA");
// certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,new
// AuthorityKeyIdentifierStructure(caCert));
serverCertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
            new SubjectKeyIdentifierStructure(serverPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException);
    serverChain[0] = serverCertGen.generateX509Certificate(serverPrivateKey, "BC"); // note: private key of CA

keyStore.setEntry("xyz",
        new KeyStore.PrivateKeyEntry(serverPrivateKey, serverChain),
            new KeyStore.PasswordProtection("".toCharArray() throws NoSuchAlgorithmException, InvalidKeySpecException));
*/
}
