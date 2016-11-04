package sqlOperation;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlOperation {
    private String driver = "com.mysql.jdbc.Driver";
    private String url = "jdbc:mysql://" + vars.Vars.ip + ":3306/AdMachine";
    private String user = "root";
    private String password = "a5018335686";
    private Connection conn;
    private Statement statement;
    public ResultSet rSet;

    public  boolean isConnected() {
        if (conn == null)
            return false;
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean SqlInit() {
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);

            if(!conn.isClosed())
                System.out.println("连接数据库成功!");

            statement = conn.createStatement();

            return true;
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    public synchronized boolean SqlUpdate (String phrase) {
    	try {
    		statement.executeUpdate(phrase);
    		return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return false;
    }

    public synchronized boolean SqlQuery(String phrase) {
        try {
        	rSet =  statement.executeQuery(phrase);
    		return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return false;
    }
    
    public synchronized void disconnect () throws SQLException {
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
