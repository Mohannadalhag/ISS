package sample;

import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.json.simple.JSONObject;

import javax.security.auth.x500.X500Principal;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CapitalizeClient_CA {
    private Socket socket;
    //private Scanner in;
    private ObjectInputStream inputStream;
    //private BufferedReader clientIn;
    private ObjectOutputStream out;
    private KeyPair keyPair;
    private boolean Certificate = false;

    public CapitalizeClient_CA() {
        try {
            File f = new File("Certificate.ser");
            if(f.exists() && !f.isDirectory()) {
                Certificate = true;
                return;
            }
            socket = new Socket("localhost", 59895);
            out = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            try {
                keyPair = generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void close_socket(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCertificate() {
        return Certificate;
    }

    public PublicKey getPublic() {
        return keyPair.getPublic();
    }

    public String send_CSR(String name) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("CSR", Base64.getEncoder().encodeToString(makeCSR(name).getEncoded()));
        /////send CSR
        out.writeObject(jsonObject);

        //receive finger print
        JSONObject jsonFinger = (JSONObject) inputStream.readObject();
        return (String) jsonFinger.get("Finger");
    }

    public void Receive_Cer(){
        try {
            out.writeObject("OK");
            JSONObject json_Cer = (JSONObject) inputStream.readObject();
            CertificateFactory certFactory = new CertificateFactory();
            InputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(((String)json_Cer.get("Cer"))));
            X509Certificate cert = (X509Certificate)certFactory.engineGenerateCertificate(in);
            FileOutputStream fout = new FileOutputStream("Certificate.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(cert);
            Certificate = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    public PKCS10CertificationRequest makeCSR(String name){
//        Security.addProvider(new BouncyCastleProvider());
        try{
            PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                    new X500Principal("CN="+name), getPublic());
            JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
            ContentSigner signer = csBuilder.build(getPrivate());
            PKCS10CertificationRequest csr = p10Builder.build(signer);
            return csr;
        }
        catch(Exception e){
            return null;
        }
    }


    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
        return keyPairGenerator.generateKeyPair();
    }

    public PrivateKey getPrivate() {
        return keyPair.getPrivate();
    }

    public void Resend_Cer() {
        try {
            out.writeObject("NO");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
