package ui;

/**
 * Created by SunBird on 2016/2/1.
 */

import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.zhilian.sunbird.demo.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import backgroundThreads.bluetooth.SendMessage;
import backgroundThreads.fullScreenKeeper.FullScreenKeeper;
import backgroundThreads.keepOnline.Online;
import backgroundThreads.repeatKeeper.RepeatKeeper;
import backgroundThreads.setUp.SetUpChecker;
import backgroundThreads.update.GetFile;
import customeView.MyVideoView;
import sql.SQLOperations;
import vars.Vars;


public class Main extends AppCompatActivity {

    private View mContentView;
    public Handler handler;
    Thread keeper;
//    Random random = new Random(23333);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mContentView = findViewById(R.id.fullBackgroung);
        hide();

        BluetoothAdapter mBluetoothAdapter;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }


        final Button set = (Button) findViewById(R.id.setup);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vars.repeatAble = false;
                startActivity(new Intent(Main.this, Setup.class));
            }
        });

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0xffff:
                        hide();
                        if (!Vars.versionCorrect) {
                            Toast.makeText(Main.this, "请更新新版本应用", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 0xf://滚动播放广告
                        refresh(false);
                        break;
                }
            }
        };


        final ImageButton mWatchAds = (ImageButton) findViewById(R.id.mWatchAds);
        mWatchAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, WatchAds.class));
                Vars.repeatAble = false;
            }
        });

        final ImageButton mSurvey = (ImageButton) findViewById(R.id.mSurvey);
        mSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, Survey.class));
                Vars.repeatAble = false;
            }
        });

        final ImageButton mScan = (ImageButton) findViewById(R.id.mScan);
        mScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, Scan.class));
                Vars.repeatAble = false;
            }
        });


        final VideoView v = (VideoView) findViewById(R.id.repeat_ads);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                v.setVisibility(View.GONE);
                Vars.repeatbreak = true;
                v.stopPlayback();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mWatchAds.setClickable(true);
                        mSurvey.setClickable(true);
                        mScan.setClickable(true);
                    }
                }).start();
                startActivity(new Intent(Main.this, WatchAds.class));
                Vars.repeatAble = false;
                set.setVisibility(View.VISIBLE);
                return false;
            }
        });


        Vars.imei = getImei();
        Vars.localPathRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        Vars.version = getVersionName();

        KeyguardManager keyguardManager
                = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);

        lock.disableKeyguard();

        checkThread();
        initBitmap();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Vars.repeatAble = true;
        Vars.repeatbreak = true;
    }

    private void initBitmap() {
        ImageView iv = (ImageView) findViewById(R.id.fullBackgroung);
        if (Vars.bitmap == null) {
            InputStream in = null;
            try {
                in = this.getResources().openRawResource(R.raw.launcher_ads);
                Vars.launcher_ads = BitmapFactory.decodeStream(in);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                in = this.getResources().openRawResource(R.raw.main_background);
                Vars.bitmap = BitmapFactory.decodeStream(in);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        iv.setImageBitmap(Vars.bitmap);

        ImageButton mWatchAds = (ImageButton) findViewById(R.id.mWatchAds);
        mWatchAds.setScaleType(ImageView.ScaleType.FIT_XY);
        mWatchAds.setImageBitmap(Vars.launcher_ads);

    }

    private void checkThread() {
        keeper = new FullScreenKeeper(handler);
        if (!keeper.isAlive()) {
            keeper.start();
        }
        if (!Vars.getfile.isAlive()) {
            Vars.getfile = new GetFile();
            Vars.getfile.start();
        }
        if (!Vars.sendmessage.isAlive()) {
            Vars.sendmessage = new SendMessage();
            Vars.sendmessage.start();
        }
        if (!Vars.online.isAlive()) {
            Vars.online = new Online();
            Vars.online.start();
        }
        if (!Vars.SetUp.isAlive()) {
            Vars.SetUp = new SetUpChecker();
            Vars.SetUp.start();
        }
        Vars.repeatAble = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Vars.repeat.interrupt();
                while (Vars.repeat.isAlive()) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Vars.repeat = new RepeatKeeper(handler);
                Vars.repeat.start();
            }
        }).start();
    }

    private void refresh(boolean loop) {
        MyVideoView iv = (MyVideoView) findViewById(R.id.repeat_ads);
        ImageButton mWatchAds = (ImageButton) findViewById(R.id.mWatchAds);
        ImageButton mSurvey = (ImageButton) findViewById(R.id.mSurvey);
        ImageButton mScan = (ImageButton) findViewById(R.id.mScan);

        if (iv.getVisibility() == View.VISIBLE && !loop)
            return;

        final Button set = (Button) findViewById(R.id.setup);
        File pictures = new File(Vars.localPathRoot + "/AdMachine/repeats/");
        File[] file = pictures.listFiles();
        Vector<File> files = new Vector<>();


        if (file == null || file.length == 0) {
            return;
        }

        for (File f : file) {
            if (f.getName().length() <= 4)
                files.add(f);
            else if (!f.getName().substring(0, 4).equals("tmp_")) {
                files.add(f);
            }
        }

        if (files.size() == 0) {
            return;
        }

        if (Vars.repeatID == files.size() - 1)
            Vars.repeatID = 0;
        else
            Vars.repeatID++;
        int tmp = Vars.repeatID;


        mWatchAds.setClickable(false);
        mSurvey.setClickable(false);
        mScan.setClickable(false);
        set.setVisibility(View.GONE);
        iv.setVisibility(View.VISIBLE);
        iv.stopPlayback();
        iv.setVideoURI(Uri.parse(files.get(tmp).getAbsolutePath()));
        iv.start();
        final String name = files.get(tmp).getName();
        iv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                refresh(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DBConnection(name);
                    }
                }).start();
            }
        });
    }


    void DBConnection(String name) {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://" + Vars.ip + ":3306/AdMachine";
        String user = Vars.sqlName;
        String password = Vars.sqlPasswd;

        try {
            File log = new File(Vars.localPathRoot + "/AdMachine/logs/");
            if (!log.exists())
                log.createNewFile();
            FileWriter writer = new FileWriter(log, true);
            writer.write(Vars.imei.substring(1) + " "
                    + name + " " + GetNowTime() + "\n");
            writer.flush();
            writer.close();

            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement statement = conn.createStatement();
            statement.executeUpdate("insert into log values("
                            + "\"" + Vars.imei.substring(1) + "\","
                            + "\"" + name + "\","
                            + "\"" + GetNowTime() + "\")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("finish");

    }


    public String GetNowTime() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(date);
        return time;
    }

    public String getImei() {
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String m_szImei = TelephonyMgr.getDeviceId();
        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits
        String m_szAndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
        BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String m_szBTMAC = m_BluetoothAdapter.getAddress();

        String m_szLongID = m_szImei + m_szDevIDShort
                + m_szAndroidID + m_szWLANMAC + m_szBTMAC;
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = new String();
        for (int i = 0; i < p_md5Data.length; i++) {
            int b = (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF)
                m_szUniqueID += "0";
            // add number to string
            m_szUniqueID += Integer.toHexString(b);
        }
        // hex string to uppercase
        m_szUniqueID = m_szUniqueID.toUpperCase();
        String imei = "/" + m_szUniqueID;
        System.out.println("imei is " + imei);
        System.out.println(imei);
        return imei;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */

    private String getVersionName() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
            String version = packInfo.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_CALL:
                return true;
            case KeyEvent.KEYCODE_SYM:
                return true;
            case KeyEvent.KEYCODE_STAR:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
