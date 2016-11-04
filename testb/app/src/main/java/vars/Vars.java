package vars;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.os.Handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.Vector;

import backgroundThreads.bluetooth.BluetoothChatService;
import backgroundThreads.bluetooth.SendMessage;
import backgroundThreads.keepOnline.Online;
import backgroundThreads.repeatKeeper.RepeatKeeper;
import backgroundThreads.setUp.SetUpChecker;
import backgroundThreads.update.GetFile;
import sql.SQLOperations;

/**
 * Created by SunBird on 2016/2/11.
 */
public class Vars {
    public static String imei;
    public static int nItem = 0;
    public static String ip = "192.168.1.40";//"119.29.68.158";//"192.168.1.44";
    public static int port = 21;
    public static String ftpName = "sunbird";
    public static String ftpPasswd = "a5018335686";
    public static String sqlName = "root";
    public static String sqlPasswd = "a5018335686";
    public static String localPathRoot;

    public static Vector<AdFile> ads = new Vector<>();
    public static Vector<AdFile> repeats = new Vector<>();

    public static Thread online = new Online();
    public static boolean send = false;
    public static Thread sendmessage = new SendMessage();
    public static Thread getfile = new GetFile();
    public static Thread repeat = new RepeatKeeper(new Handler());
    public static boolean repeatAble = false;
    public static boolean repeatbreak = false;
    public static Thread SetUp = new SetUpChecker();
    public static boolean set = false;

    public static Bitmap bitmap;
    public static Bitmap vcard;
    public static Bitmap launcher_ads;

    public static int adID = -1;
    public static int repeatID = -1;
    public static int bluetooth = 0;

    public static String version;
    public static boolean versionCorrect = true;
}
