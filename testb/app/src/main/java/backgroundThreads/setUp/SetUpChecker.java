package backgroundThreads.setUp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import vars.Vars;

/**
 * Created by SunBird on 2016/5/30.
 */
public class SetUpChecker extends Thread {
    public void run() {
        String driver = "com.mysql.jdbc.Driver";
        // URL指向要访问的数据库名******
        String url = "jdbc:mysql://" + Vars.ip + ":3306/AdMachine";
        // MySQL配置时的用户名
        String user = Vars.sqlName;
        // Java连接MySQL配置时的密码******
        String password = Vars.sqlPasswd;

        try {
            // 加载驱动程序
            Class.forName(driver);

            // 连续数据库
            Connection conn = DriverManager.getConnection(url, user, password);
            if (!conn.isClosed())
                System.out.println("Succeeded connecting to the Database!");

            // statement用来执行SQL语句
            Statement statement = conn.createStatement();
            while (true) {

                try {
                    Thread.sleep(1500);
                    ResultSet rs = statement.executeQuery("select setup from device where imei = "
                            + "\"" + Vars.imei.substring(1) + "\"");
                    if (rs.next() && rs.getInt("setup") == 1) {
                        Vars.set = true;
                    } else {
                        Vars.set = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    conn = DriverManager.getConnection(url, user, password);
                    statement = conn.createStatement();
                    System.out.println(0);
                    Vars.set = false;
                }
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Sorry,can`t find the Driver!");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
