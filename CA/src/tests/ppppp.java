package tests;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

public class ppppp {
    public static void main(String []args) throws NoSuchAlgorithmException, CertificateException, OperatorCreationException, IOException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X509Certificate cer = selfSign(keyPair, "C=DE,O=Organiztion");
        System.out.println(cer);
        System.out.println(fingerprint(cer));
    }
    static String fingerprint(X509Certificate c)
            throws IOException, CertificateEncodingException {
        byte[] der = c.getEncoded();
        byte[] sha1 = new SHA256Digest(der).getEncodedState();
        byte[] hexBytes = Hex.encode(sha1);
        String hex = new String(hexBytes, "ASCII").toUpperCase();

        StringBuffer fp = new StringBuffer();
        int i = 0;
        fp.append(hex.substring(i, i + 2));
        while ((i += 2) < hex.length())
        {
            fp.append(':');
            fp.append(hex.substring(i, i + 2));
        }
        return fp.toString();
    }
    public static X509Certificate selfSign(KeyPair keyPair, String subjectDN) throws OperatorCreationException, CertificateException, IOException
    {
        BouncyCastleProvider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        X500Name dnName = new X500Name(subjectDN);
        BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1); // <-- 1 Yr validity

        Date endDate = calendar.getTime();

        String signatureAlgorithm = "SHA256WithRSA"; // <-- Use appropriate signature algorithm based on your keyPair algorithm.

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

        // Extensions --------------------------

        // Basic Constraints
        BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for tests.CA, false for EndEntity

        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

        // -------------------------------------

        return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
    }
}
