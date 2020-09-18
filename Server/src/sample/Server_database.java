package sample;

import com.mysql.jdbc.PreparedStatement;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

public class Server_database extends DBConnection{

    public Server_database(String userName, String password, String databaseName) {
        super(userName, password, databaseName);
    }
    public boolean login(int id , String Client_name, String password){
        ResultSet res ;
        String query = "select * from users where id=" +id+
                " and password ='"+password+"'";
        res = ExecuteQuery(query);
        try {
            if (!res.next()) return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true ;
    }
    public boolean check_Client(int id){
        ResultSet res ;
        String query = "select * from users where id="+id;
        res = ExecuteQuery(query);
        try {
            if (!res.next()) return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  true ;
    }

    public String get_private_key(int id){
        ResultSet res ;
        if(check_Client(id)){
            String query = "select private_key from users where id="+id ;
            res = ExecuteQuery(query);
            try {
                res.next();
                return res.getString("private_key");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public KeyPair get_Keys_pair(int id){
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

    public boolean add_keys_pair(int id, byte[] private_key, byte[] public_key) {

        try {
            String query = "INSERT INTO `client_keys`(`id`, `private_key`, `public_key`) VALUES (?,?,?)";
            PreparedStatement stmt = (PreparedStatement) connection.prepareStatement(query);
            stmt.setInt(1,id);
            stmt.setBytes(2,private_key);
            stmt.setBytes(3,public_key);
            return Executeprepared(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean check_keys(int id){
        ResultSet res ;
        String query = "select * from client_keys where id="+id ;
        res = ExecuteQuery(query);
        try {
            if (!res.next()) return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  true ;
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

    private boolean createCertificatetable() {
        String query = "create table Certificates("
                + "id int primary key AUTO_INCREMENT,"
                + "name varchar(255),"
                + "Certificate Blob unique not null"
                + ")";
        return ExecuteDMLQuery(query);
    }




    public boolean check_amount(int id , float amount ) {
        ResultSet res ;
        float temp;
        if(check_Client(id)){
            String query = "select amount from users where id="+id ;
            res = ExecuteQuery(query);
            try {
                res.next();
                temp = res.getFloat("amount");
                if(amount<=temp){
                    return true ;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false ;
    }
    public boolean transfer(int id_send, int id_resiver, float amount, String reason, int id_trans){
        if(id_send==-1) {
            String query2 = "update users set amount = amount+"+amount+" where id = "+id_resiver ;
            ExecuteDMLQuery(query2);
            return true;
        }
        else if(check_Client(id_resiver) && check_Client(id_send) && check_amount(id_send,amount)){
            String query2 = "update users set amount = amount+"+amount+" where id = "+id_resiver ;
            String query1 = "update users set amount = amount-"+amount+" where id = "+id_send ;
            String query3 = "INSERT INTO transfer(send_id,receive_id, id_trans, amount, description) VALUES ("+id_send+","+id_resiver+","+id_trans+","+amount+",'"+reason+"')";
            ExecuteDMLQuery(query1);
            ExecuteDMLQuery(query2);
            ExecuteDMLQuery(query3);
            System.out.println(query3);
            return true ;
        }
        return false ;
    }
    public boolean add_Client (int id , String username , String password,int amount, String Client_Key){
        if(!check_Client(id)){
            String query = "insert into users values ("+id+",'"+username+"','"+password+"',"+amount+",'"+Client_Key+"'"+")";
            ExecuteDMLQuery(query);
            return true ;
        }
        return false ;
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

    private boolean createUsersTable() {
        String query = "create table users("
                + "id int primary key ,"
                + "name nvarchar(255) unique not null,"
                + "password nvarchar(255) not null,"
                + "amount float not null,"
                + "private_Key nvarchar(255) unique not null"
                + ")";
        return ExecuteDMLQuery(query);
    }

    private boolean createKeysTable() {
        String query = "create table client_keys("
                + "id int primary key ,"
                + "private_key Blob unique not null,"
                + "public_key Blob unique not null,"
                + "foreign key(id) references users(id)"
                + ")";
        return ExecuteDMLQuery(query);
    }

    private boolean creatTransferTable(){
        String query = "create table transfer("
                + "id int primary key AUTO_INCREMENT,"
                + "send_id int not null,"
                + "receive_id int not null,"
                + "id_trans int unique not null,"
                + "amount float not null,"
                + "description varchar(255) not null,"
                + "foreign key(send_id) references users(id),"
                + "foreign key(receive_id) references users(id)"
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
        if (!selectDataBase() || !createUsersTable() || !creatTransferTable() || !createKeysTable() || !createCertificatetable()) {
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
