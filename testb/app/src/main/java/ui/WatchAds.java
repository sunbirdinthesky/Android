package ui;

/**
 * Created by SunBird on 2016/2/1.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import com.zhilian.sunbird.demo.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

import backgroundThreads.fullScreenKeeper.FullScreenKeeper;
import customeView.MyVideoView;
import sql.SQLOperations;
import vars.Vars;


public class WatchAds extends AppCompatActivity {
    private View mContentView;
//    Random random = new Random(23333);
    public Handler handler;
    Thread keeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_ads);
        mContentView = findViewById(R.id.wcfullscreen_content);
        hide();

        ImageButton wSurvey = (ImageButton) findViewById(R.id.wSurvey);
        wSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WatchAds.this, Survey.class));
            }
        });

        ImageButton wScan = (ImageButton) findViewById(R.id.wScan);
        wScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WatchAds.this, Scan.class));
            }
        });

        ImageButton wReturn = (ImageButton) findViewById(R.id.wReturn);
        wReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton wRefresh = (ImageButton) findViewById(R.id.wRefresh);
        wRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBConnection();
            }
        });

        ImageButton wHome = (ImageButton) findViewById(R.id.wHome);
        wHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WatchAds.this, Main.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });

        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.buttons);

        final MyVideoView v = (MyVideoView)  findViewById(R.id.wAds);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                frameLayout.setVisibility(View.VISIBLE);
                Vars.repeatbreak = true;
                return false;
            }
        });
//全屏方法
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0xffff:
                        hide();
                        if (!Vars.repeatbreak)
                            frameLayout.setVisibility(View.INVISIBLE);
                        else
                            Vars.repeatbreak = false;
                        break;
                }
            }
        };

        if (keeper == null || !keeper.isAlive()) {
            keeper = new FullScreenKeeper(handler);
            keeper.start();
        }

//#################################################
        DBConnection();
    }

    void DBConnection() {
        MyVideoView iv = (MyVideoView)  findViewById(R.id.wAds);
        File pictures = new File(Vars.localPathRoot + "/AdMachine/ads/");
        final File[] file = pictures.listFiles();
        Vector<File> files = new Vector<>();


        if (file == null || file.length == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("您一个广告都没有了啦亲")
                    .setPositiveButton("偶知道啦", null)
                    .show();
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
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("您的广告正在缓存~请稍等")
                    .setPositiveButton("偶知道啦", null)
                    .show();
            return;
        }

        if (Vars.adID == files.size()-1)
            Vars.adID = 0;
        else
            Vars.adID++;
        int tmp = Vars.adID;


        final String name = files.get(tmp).getName();

        iv.stopPlayback();
        iv.setVideoURI(Uri.parse(files.get(tmp).getAbsolutePath()));
        iv.start();
        iv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
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


                            ResultSet rs = statement.executeQuery(
                                    "select playCount from ads" + name
                                            + " where imei = \""
                                            + Vars.imei.substring(1) + "\"");
                            if (rs.next()) {
                               if (rs.getInt("playCount") != 0) {
                                   statement.executeUpdate(
                                           "update ads set playCount = playCount-1 where name = \""
                                                   + name + "\"");
                                   statement.executeUpdate("update ads" + name
                                           + " set playCount = playCount-1 where imei = \""
                                           + Vars.imei.substring(1) + "\"");
                               }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        System.out.println("finish");

                    }
                }).start();

                startActivity(new Intent(WatchAds.this, Finish.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }


    public String GetNowTime () {
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time=format.format(date);
        return time;
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
