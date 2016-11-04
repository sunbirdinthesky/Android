package ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.zhilian.sunbird.demo.R;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import backgroundThreads.fullScreenKeeper.FullScreenKeeper;
import sql.SQLOperations;
import vars.Vars;

/**
 * Created by SunBird on 2016/2/1.
 */
public class Finish extends AppCompatActivity{
    private View mContentView;
    public Handler handler;
    Thread keeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finish);
        mContentView = findViewById(R.id.finishBackgroung);
//        ImageView iv = (ImageView)findViewById(R.id.imageView2);
//        initBitmap(iv);

//        ImageButton fWatchAds = (ImageButton)findViewById(R.id.fWatchAds);
//        fWatchAds.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Finish.this, WatchAds.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                finish();
//            }
//        });
//
//        ImageButton fSurvey = (ImageButton)findViewById(R.id.fSurvey);
//        fSurvey.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Finish.this, Survey.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                finish();
//            }
//        });
//
//        ImageButton fReturn = (ImageButton)findViewById(R.id.fReturn);
//        fReturn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Finish.this, Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                finish();
//            }
//        });
//
//        ImageButton fScan = (ImageButton)findViewById(R.id.fScan);
//        fScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Finish.this, Scan.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                finish();
//            }
//        });
//
//        ImageButton fHome = (ImageButton)findViewById(R.id.fHome);
//        fHome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Finish.this, Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                finish();
//            }
//        });
        Vars.nItem--;
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBConnection();
            }
        }).start();
        Vars.send = true;

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0xffff:
                        hide();
                        break;
                    case 1:
                        System.out.println("go");
                        startActivity(new Intent(Finish.this, Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                }
            }
        };

        if (keeper == null || !keeper.isAlive()) {
            keeper = new FullScreenKeeper(handler);
            keeper.start();
        }
        hide();
        initBitmap();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("run");
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("run");
                Message.obtain(handler, 1).sendToTarget();
            }
        }).start();
    }
//
//    private void initBitmap (ImageView iv) {
//        if (Vars.vcard == null) {
//            try
//            {
//                InputStream in = null;
//                in = this.getResources().openRawResource(R.raw.vcard);
//                Vars.vcard = BitmapFactory.decodeStream(in);
//                in.close();
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
//        iv.setImageBitmap(Vars.vcard);
//    }

    private void initBitmap () {
        ImageView iv = (ImageView)findViewById(R.id.finishBackgroung);
        if(Vars.bitmap==null){
            InputStream in = null;
            try
            {
                in = this.getResources().openRawResource(R.raw.main_background);
                Vars.bitmap = BitmapFactory.decodeStream(in);
                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        iv.setImageBitmap(Vars.bitmap);
    }

    void DBConnection(){
        if (!SQLOperations.isConnected())
            SQLOperations.init();
        if (SQLOperations.isConnected()) {
            try {
                SQLOperations.runUpdateStatement("update device set nItem = " + Vars.nItem +
                        " where imei = \"" +
                        Vars.imei.substring(1) + "\"");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
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
