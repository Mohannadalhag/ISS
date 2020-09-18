import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.Fingerprint;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Capitalize_CA {
    private static Certificate_database CA_database = new Certificate_database("mohannaddb","1234512345","DB_CA");

    public static void main(String []args) throws Exception {
        CA_database.Connect();
        try (ServerSocket listener = new ServerSocket(59895)) {
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
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Sign_Certificates sign_certificates = new Sign_Certificates();
                X509Certificate certificate = null;
                while (true)
                {
                    certificate = Receive_CSR(in, out, sign_certificates);
                    if (Send_Cer(in, out, certificate)){
                        CA_database.add_Certificate(get_name(certificate),certificate.getEncoded());
                        break;
                    }
                }
                if (get_name(certificate).compareToIgnoreCase("Server")==0)
                {
                    while (true)
                    {
                        break;
                    }
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

        private boolean Send_Cer(ObjectInputStream in, ObjectOutputStream out, X509Certificate certificate) {
            try {
                JSONObject JsonCer = new JSONObject();
                if (((String)in.readObject()).compareToIgnoreCase("OK")==0){
                    JsonCer.put("Cer",Base64.getEncoder().encodeToString(certificate.getEncoded()));
                    out.writeObject(JsonCer);
                    return true;
                }
                else {
                    JsonCer.put("Cer","Faild");
                    out.writeObject(JsonCer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
            return false;
        }

        public X509Certificate Receive_CSR(ObjectInputStream in, ObjectOutputStream out, Sign_Certificates sign_certificates)
        {
            X509Certificate certificate = null;
            try {
                /////receive CSR
                JSONObject jsonCSR = (JSONObject) in.readObject();
                PKCS10CertificationRequest CSR = new PKCS10CertificationRequest(Base64.getDecoder().decode((String) jsonCSR.get("CSR")));
                certificate = sign_certificates.sign(CSR);
                if(certificate!=null) {
                    JSONObject Finger_json = new JSONObject();
                    Fingerprint fingerprint = new Fingerprint(certificate.getEncoded());
                    Finger_json.put("Finger",Base64.getEncoder().encodeToString(fingerprint.getFingerprint()));
                    ////send fingerprint
                    out.writeObject(Finger_json);
                }
                else {
                    out.writeUTF("No");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return certificate;
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
    }
}
