package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import vars.Vars;

public class SQLOperations {

    private static String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://" + Vars.ip + ":3306/AdMachine";
    private static String user = Vars.sqlName;
    private static String password = Vars.sqlPasswd;
    private static Connection conn;
    private static Statement statement;
    private static ResultSet rSet;

    public static boolean isConnected() {
        if (conn == null)
            return false;
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ��ʼ��
     *
     */

    public static synchronized boolean init() {
        try {
            disconnect();
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);

            if(!conn.isClosed())
                System.out.println("Succeeded connecting to the Database!");

            statement = conn.createStatement();

            return true;
        } catch(ClassNotFoundException e) {
            System.out.println("Sorry,can`t find the Driver!");
            e.printStackTrace();
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static synchronized int runUpdateStatement(String phrase) throws SQLException {
        return statement.executeUpdate(phrase);
    }

    public static synchronized ResultSet runQueryStatement(String phrase) throws SQLException {
        return statement.executeQuery(phrase);
    }


    /**
     * �Ͽ�����
     * @throws SQLException
     */
    public static synchronized void disconnect () throws SQLException {
        if(conn != null && !conn.isClosed())
            conn.close();
        if(rSet != null && !rSet.isClosed())
            rSet.close();
    }

    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
        disconnect();
    }
}
