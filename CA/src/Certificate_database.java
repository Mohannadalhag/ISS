import com.mysql.jdbc.PreparedStatement;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

public class Certificate_database extends DBConnection{

    public Certificate_database(String userName, String password, String databaseName) {
        super(userName, password, databaseName);
    }

    public X509Certificate get_certificate(String name){
        ResultSet res ;
        String query = "select name,Certificate from Certificates where name like"+ name;
        res = ExecuteQuery(query);
        try {
            res.next();
            byte []Certificate_bytes = res.getBytes("Certificate");
            CertificateFactory certFactory = new CertificateFactory();
            InputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(Certificate_bytes));
            X509Certificate cert = (X509Certificate)certFactory.engineGenerateCertificate(in);
            return cert;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean add_Certificate(String name, byte[] certificate) {

        try {
            String query = "INSERT INTO `Certificates`(`name`, `Certificate`) VALUES (?,?)";
            PreparedStatement stmt = (PreparedStatement) connection.prepareStatement(query);
            stmt.setString(1,name);
            stmt.setBytes(2,certificate);
            return Executeprepared(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean ConnectToServer() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, userName, password);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private boolean createCertificatetable() {
        String query = "create table Certificates("
                + "id int primary key AUTO_INCREMENT,"
                + "name varchar(255),"
                + "Certificate Blob unique not null"
                + ")";
        return ExecuteDMLQuery(query);
    }


    private boolean ConnectToDataBase() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url + DatabaseName, userName, password);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean Connect() {
        if (!firstConnection())
            if (!ConnectToDataBase()) {
                return false;
            }
        return true;
    }

    private boolean firstConnection() {
        try {
            if (ConnectToServer()) {
                connection.setAutoCommit(false);
                if (!createDataBase() ||
                        !createTables()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    ExecuteQuery("use master;" +
                            " ALTER DATABASE " + DatabaseName + " SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
                            "DROP DATABASE " + DatabaseName + ";");
                    return false;
                }
                connection.commit();
                connection.setAutoCommit(true);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    private boolean createDataBase() {
        try {
            Statement stmt = connection.createStatement();
            String query = "create database " + DatabaseName;
            stmt.execute(query);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean createTables() {
        if (!selectDataBase() || !createCertificatetable()) {
            return false;
        }
        return true;
    }

    private boolean selectDataBase() {
        try {
            Statement stmt = connection.createStatement();
            String query = "use " + DatabaseName;
            stmt.execute(query);
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

}
