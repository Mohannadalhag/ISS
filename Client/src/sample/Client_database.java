package sample;

import com.mysql.jdbc.PreparedStatement;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Client_database extends DBConnection{

    public Client_database(String userName, String password, String databaseName) {
        super(userName, password, databaseName);
    }

    public KeyPair get_certificate(int id){
        ResultSet res ;
        String query = "select private_key,public_key from client_keys where id="+id ;
        res = ExecuteQuery(query);
        KeyPair keyPair = null;
        try {
            res.next();
            byte []public_key_bytes = res.getBytes("public_key");
            byte []private_key_bytes = res.getBytes("private_key");
            X509EncodedKeySpec ks = new X509EncodedKeySpec(public_key_bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey public_key = kf.generatePublic(ks);
            PKCS8EncodedKeySpec ks1 = new PKCS8EncodedKeySpec(private_key_bytes);
            PrivateKey private_key = kf.generatePrivate(ks1);
            keyPair = new KeyPair(public_key,private_key);
            return keyPair;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean add_Certificate(String name, byte[] public_key, byte[] certificate) {

        try {
            String query = "INSERT INTO `Certificates`(`name`, `public_key`, `Certificate`) VALUES (?,?,?)";
            PreparedStatement stmt = (PreparedStatement) connection.prepareStatement(query);
            stmt.setString(1,name);
            stmt.setBytes(2,public_key);
            stmt.setBytes(3,certificate);
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
                + "public_key Blob unique not null,"
                + "Certificate Blob unique not null,"
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
