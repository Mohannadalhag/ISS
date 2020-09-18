import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.Fingerprint;
import sun.security.krb5.Credentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;

public class Sign_Certificates {
    KeyPair keyPair;

    public Sign_Certificates(){
        try {
            keyPair = generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
        return keyPairGenerator.generateKeyPair();
    }



    public X509Certificate sign(PKCS10CertificationRequest inputCSR)
            throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchProviderException, SignatureException, IOException,
            OperatorCreationException, CertificateException {

        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

        AsymmetricKeyParameter foo = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
        SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(getpublic(inputCSR).getEncoded());

        //PKCS10CertificationRequestHolder pk10Holder = new PKCS10CertificationRequestHolder(inputCSR.getEncoded());
        //in newer version of BC such as 1.51, this is
        PKCS10CertificationRequest pk10Holder = new PKCS10CertificationRequest(inputCSR.getEncoded());

        X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(
                new X500Name("CN=issuer"), new BigInteger("1"), new Date(
                System.currentTimeMillis()), new Date(
                System.currentTimeMillis() + 30 * 365 * 24 * 60 * 60 * 1000), pk10Holder.getSubject(), keyInfo);

        ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                .build(foo);

        X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
        Certificate eeX509CertificateStructure = holder.toASN1Structure();
        //in newer version of BC such as 1.51, this is
        //org.spongycastle.asn1.x509.Certificate eeX509CertificateStructure = holder.toASN1Structure();

        //CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        CertificateFactory cf = new CertificateFactory();
        // Read Certificate
        InputStream is1 = new ByteArrayInputStream(eeX509CertificateStructure.getEncoded());
        //X509Certificate theCert = (X509Certificate) cf.generateCertificate(is1);
        X509Certificate theCert = (X509Certificate) cf.engineGenerateCertificate(is1);
        is1.close();
        return theCert;
        //return null;
    }


    public PublicKey getpublic(PKCS10CertificationRequest inputCSR) {
        SubjectPublicKeyInfo pkInfo = inputCSR.getSubjectPublicKeyInfo();
        RSAKeyParameters rsa = null;
        try {
            rsa = (RSAKeyParameters) PublicKeyFactory.createKey(pkInfo);
            RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(rsa.getModulus(), rsa.getExponent());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey rsaPub = kf.generatePublic(rsaSpec);
            return rsaPub;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        System.err.println("Error in getpublic");
        return null;
    }
}


