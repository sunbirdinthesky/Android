package backgroundThreads.keepOnline;

import android.database.SQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import vars.Vars;

/**
 * Created by SunBird on 2016/4/24.
 */
public class Online extends Thread{
    @Override
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
                    System.out.println("online" + Vars.bluetooth);
                    statement.executeUpdate("update device set time = now(), bluetooth = \""
                            + String.valueOf(Vars.bluetooth) + "\" where imei = \""
                            + Vars.imei.substring(1) + "\"");
                } catch (Exception e) {
                    e.printStackTrace();
                    conn = DriverManager.getConnection(url, user, password);
                    if (!conn.isClosed())
                        System.out.println("Database reconnected");
                    statement = conn.createStatement();
                    System.out.println("offline" + Vars.bluetooth);
                }
            }
        } catch  (Exception e) {
            e.printStackTrace();
        }
    }
}
