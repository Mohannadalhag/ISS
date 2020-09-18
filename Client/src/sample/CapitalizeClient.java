package sample;

import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class CapitalizeClient {
    private Socket socket;
    private Scanner in;
    private BufferedReader clientIn;
    private ObjectOutputStream out;
    private AES aes = new AES();
    private PGP pgp = new PGP();
    private DSA dsa = new DSA();
    private String Encrypt_Type = "";
    private int[] nums;
    public CapitalizeClient(){
        try {
            socket = new Socket("localhost", 59893);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new Scanner(socket.getInputStream());
            clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            nums = new int[10000];

            Random randomGenerator = new Random();

            for (int i = 0; i < nums.length; ++i){
                nums[i] = randomGenerator.nextInt(10000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send_Encrypt_Type(String Encrypt_Type){
        this.Encrypt_Type = Encrypt_Type;
        try {
            out.writeObject(Encrypt_Type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAes_Key(String private_key) {
        this.aes.setKeyValue(private_key);
    }

    public void close_socket() {
        try {
            out.writeObject("close");
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean send_info(String name, String password, String Number) throws Exception {
        JSONObject jsonObject = new JSONObject();
        name = aes.encrypt(name);
        password = aes.encrypt(password);
        Number = aes.encrypt(Number);
        jsonObject.put("name",name);
        jsonObject.put("password",password);
        jsonObject.put("id",Number);
        out.writeObject(jsonObject);
        String output = in.nextLine();
        return output.compareToIgnoreCase("OK")==0;
    }

    public boolean send(String id, String Amount, String Reason) throws Exception {
        Random randomGenerator = new Random();
        if (this.Encrypt_Type.compareToIgnoreCase("AES")==0) return sendAES(id,Amount, Reason,nums[randomGenerator.nextInt(9999)]);
        else if (this.Encrypt_Type.compareToIgnoreCase("PGP")==0) return sendPGP(id,Amount,Reason,nums[randomGenerator.nextInt(9999)]);
        return false;
    }

    private void sendDSA(String id, String amount, String reason, JSONObject jsonObject1, int id_trans) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        KeyPair DSAPair = dsa.buildKeyPair();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("Amount",amount);
        jsonObject.put("Reason", reason);
        jsonObject.put("id_trans",id_trans);

        byte[] sign = dsa.sign(DSAPair.getPrivate(), jsonObject.toString().getBytes("utf-8"));

        jsonObject1.put("sign", Base64.getEncoder().encodeToString((sign)));
        jsonObject1.put("Key", Base64.getEncoder().encodeToString(DSAPair.getPublic().getEncoded()));
        try {
            jsonObject1.put("Cer", Base64.getEncoder().encodeToString(loadCertificateFromFile().getEncoded()));
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
    }

    private boolean sendAES(String id, String Amount, String Reason, int id_trans) throws Exception {
        JSONObject jsonObject = new JSONObject();
        sendDSA(id,Amount,Reason, jsonObject, id_trans);
        id = aes.encrypt(id);
        Amount = aes.encrypt(Amount);
        Reason = aes.encrypt(Reason);
        jsonObject.put("id",id);
        jsonObject.put("Amount",Amount);
        jsonObject.put("Reason", Reason);
        jsonObject.put("id_trans", id_trans);
        out.writeObject(jsonObject);
        String output = in.nextLine();
        return output.compareToIgnoreCase("OK")==0;
    }


    private boolean sendPGP(String id, String Amount, String Reason, int id_trans) throws IOException, GeneralSecurityException {
        JSONObject jsonObject = new JSONObject();
        sendDSA(id,Amount,Reason, jsonObject, id_trans);
        out.writeObject("1");

        //receive public key
        int len = Integer.parseInt(clientIn.readLine());
        byte[] serverPubKeyBytes = new byte[len];
        socket.getInputStream().read(serverPubKeyBytes,0,len);
        X509EncodedKeySpec ks = new X509EncodedKeySpec(serverPubKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey serverPubKey = kf.generatePublic(ks);
        byte[] encrypt_id = pgp.encrypt(id.getBytes(), serverPubKey);
        byte[] encrypt_Amount = pgp.encrypt(Amount.getBytes(), serverPubKey);
        byte[] encrypt_Reason = pgp.encrypt(Reason.getBytes(), serverPubKey);
        jsonObject.put("id",encrypt_id);
        jsonObject.put("Amount",encrypt_Amount);
        jsonObject.put("Reason", encrypt_Reason);
        jsonObject.put("id_trans",id_trans);

        ////send encrypt message
        out.writeObject(jsonObject);

        String output = in.nextLine();

        return output.compareToIgnoreCase("OK")==0;
    }
    private X509Certificate loadCertificateFromFile(){
        try {
            FileInputStream fin = new FileInputStream("Certificate.ser");
            ObjectInputStream iin = new ObjectInputStream(fin);
            X509Certificate cert = (X509Certificate) iin.readObject();
            return cert;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}