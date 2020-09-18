package sample;

import com.mysql.jdbc.PreparedStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DBConnection {
    protected Connection connection;
    protected final String driver = "com.mysql.jdbc.Driver";
    protected String userName = "mohannaddb";
    protected String password = "1234512345";
    protected final String url = "jdbc:mysql://localhost:3306/";
    protected String DatabaseName;
    //protected final String windowsAuth = "integratedSecurity=true";

    public DBConnection(String userName, String password, String databaseName){
        this.userName = userName;
        this.password = password;
        DatabaseName = databaseName;
    }

    public Connection getConnection() {
        return connection;
    }

    public abstract boolean Connect();

    public ResultSet ExecuteQuery(String query) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(query);
            return result;
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean ExecuteDMLQuery(String query) {
        try {
            Statement stmt = connection.createStatement();
            System.out.println(query);
            stmt.execute(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean Executeprepared(PreparedStatement stmt) {

        try {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
