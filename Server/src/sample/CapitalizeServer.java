package sample;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CapitalizeServer {
    private static Server_database server_database = new Server_database("mohannaddb", "1234512345", "DBServer");
    private static CapitalizeServer_CA capitalizeServer_ca = new CapitalizeServer_CA();

    public static void main(String[] args) throws Exception {
        if (!capitalizeServer_ca.isCertificate())
        {
            try {
                capitalizeServer_ca.send_CSR("Server");
                capitalizeServer_ca.Receive_Cer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        server_database.Connect();
        try (ServerSocket listener = new ServerSocket(59893)) {
            System.out.println("The capitalization server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Capitalizer(listener.accept()));
            }
        }
    }

    public static class Capitalizer implements Runnable { // runnable interface has the run method that threads execute
        private Socket socket;

        Capitalizer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Connected: " + socket);
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                AES aes = new AES();
                DSA dsa = new DSA();
                int Client_number = login(in,out,aes);

                String Encrypt_Type = (String) in.readObject();
                if(Encrypt_Type.compareToIgnoreCase("AES")==0) aes.setKeyValue(server_database.get_private_key(Client_number));
                while (true){
                    if(Encrypt_Type.compareToIgnoreCase("AES")==0) receive_AES(in,out,Client_number, aes, dsa);
                    else if(Encrypt_Type.compareToIgnoreCase("PGP")==0) receive_PGP(in,out,Client_number, dsa);
                }
            } catch (Exception e) {
                System.out.println("Error:" + socket);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                System.out.println("Closed: " + socket);
            }
        }

        private boolean receive_DSA(DSA dsa, JSONObject jsonObject) throws IOException {
            byte[] signBytes;
            byte[] KeyBytes;
            byte[] Certificate;
            try {
                signBytes = Base64.getDecoder().decode((String) jsonObject.get("sign"));
                KeyBytes = Base64.getDecoder().decode((String) jsonObject.get("Key"));
                Certificate = Base64.getDecoder().decode((String) jsonObject.get("Cer"));
                dsa.setPublic(KeyBytes);
                dsa.setSignature(signBytes);
                jsonObject.remove("sign");
                jsonObject.remove("Key");
                jsonObject.remove("Cer");
                return dsa.verify(jsonObject.toString().getBytes("utf-8"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        private boolean receive_PGP(ObjectInputStream in, PrintWriter out, int Client_number, DSA dsa) {
            PGP pgp = new PGP();
            KeyPair pair = null;
            try {
                ///send public key
                in.readObject();
                while (!server_database.check_keys(Client_number)){
                    pair = pgp.generateKeyPair();
                    server_database.add_keys_pair(Client_number, pair.getPrivate().getEncoded(), pair.getPublic().getEncoded());
                }
                if(pair == null) pair = server_database.get_Keys_pair(Client_number);
                out.println(pair.getPublic().getEncoded().length);
                socket.getOutputStream().write(pair.getPublic().getEncoded());
                socket.getOutputStream().flush();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            ///receive Encrypted_message
            try {
                JSONObject jsonObject = (JSONObject) in.readObject();
                int id_recv = Integer.parseInt(new String(pgp.decrypt((byte[]) jsonObject.get("id"),pair.getPrivate())));
                int Amount = Integer.parseInt(new String(pgp.decrypt((byte[]) jsonObject.get("Amount"),pair.getPrivate())));
                int id_trans = Integer.parseInt(new String(pgp.decrypt((byte[]) jsonObject.get("id"),pair.getPrivate())));
                String Reason = new String(pgp.decrypt((byte[]) jsonObject.get("Reason"),pair.getPrivate()));
                jsonObject.replace("id",Integer.toString(id_recv));
                jsonObject.replace("Amount",Integer.toString(Amount));
                jsonObject.replace("Reason",Reason);
                jsonObject.replace("id_trans",id_trans);
                if(receive_DSA(dsa,jsonObject)){
                    if(server_database.transfer(Client_number,id_recv,Amount,Reason, id_trans))out.println("OK");
                    else out.println("No");
                }
                else out.println("No");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        public boolean receive_AES(ObjectInputStream in, PrintWriter out, int Client_number, AES aes, DSA dsa)
        {
            JSONObject jsonObject;
            try {
                jsonObject = (JSONObject) in.readObject();
                int id_recv = Integer.parseInt(aes.decrypt((String) jsonObject.get("id")));
                int Amount = Integer.parseInt(aes.decrypt((String) jsonObject.get("Amount")));
                int id_trans = Integer.parseInt(aes.decrypt((String) jsonObject.get("id_trans")));
                String Reason = aes.decrypt((String) jsonObject.get("Reason"));
                jsonObject.replace("id",Integer.toString(id_recv));
                jsonObject.replace("Amount",Integer.toString(Amount));
                jsonObject.replace("Reason",Reason);
                if(receive_DSA(dsa,jsonObject)){
                    if(server_database.transfer(Client_number,id_recv,Amount,Reason, id_trans))out.println("OK");
                    else out.println("No");
                }
                else out.println("No");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        public int login(ObjectInputStream in, PrintWriter out, AES aes)
        {
            int Client_number;
            while (true){
                try {
                    String Client_name;
                    JSONObject jsoninfo = (JSONObject) in.readObject();
                    Client_name = aes.decrypt((String) jsoninfo.get("name"));
                    String password = aes.decrypt((String) jsoninfo.get("password"));
                    Client_number = Integer.parseInt(aes.decrypt((String) jsoninfo.get("id")));
                    if(server_database.login(Client_number, Client_name,password)) {
                        out.println("OK");
                        break;
                    }
                    else {
                        out.println("No");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return Client_number;
        }

        public String get_name(X509Certificate cert){
            X500Name x500name = null;
            try {
                x500name = new JcaX509CertificateHolder(cert).getSubject();
                RDN cn = x500name.getRDNs(BCStyle.CN)[0];
                return IETFUtils.valueToString(cn.getFirst().getValue());
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
            return "mohannad";
        }
        public X509Certificate get_Certificate(byte[] cer){
            CertificateFactory certFactory = new CertificateFactory();
            InputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(cer));
            try {
                X509Certificate cert = (X509Certificate)certFactory.engineGenerateCertificate(in);
                return cert;
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
