package ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Random;
import java.util.Vector;

import backgroundThreads.fullScreenKeeper.FullScreenKeeper;
import sql.SQLOperations;
import vars.Vars;

/**
 * Created by SunBird on 2016/2/1.
 */

public class Scan extends AppCompatActivity {
    private View mContentView;
    Random random = new Random(233333);
    public Handler handler;
    Thread keeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
        mContentView = findViewById(R.id.scanBackground);
        hide();

        ImageButton scWatchAds = (ImageButton)findViewById(R.id.scWatchAds);
        scWatchAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Scan.this, WatchAds.class));
            }
        });

        ImageButton scSurvey = (ImageButton)findViewById(R.id.scSurvey);
        scSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Scan.this, Survey.class));
            }
        });

        ImageButton scReturn = (ImageButton)findViewById(R.id.scReturn);
        scReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton scRefresh = (ImageButton)findViewById(R.id.scRefresh);
        scRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        ImageButton scHome = (ImageButton)findViewById(R.id.scHome);
        scHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Scan.this, Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });


        //全屏方法
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0xffff:
                        hide();
                        break;
                }
            }
        };

        if (keeper == null || !keeper.isAlive()) {
            keeper = new FullScreenKeeper(handler);
            keeper.start();
        }
//##########################################
        refresh();
        initBitmap();
    }

    private void initBitmap () {
        ImageView iv = (ImageView)findViewById(R.id.scanBackground);
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

    private void refresh () {
        ImageView iv = (ImageView)findViewById(R.id.bin_picture);
        File pictures = new File(Vars.localPathRoot + "/AdMachine/bins/");
        File[] file = pictures.listFiles();
        Vector<File> files = new Vector<>();

        if (file == null || file.length == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("您一个广告都没有了啦亲")
                    .setPositiveButton("偶知道啦", null)
                    .show();
            return;
        }

        for (File f: file) {
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
        int tmp = Math.abs(random.nextInt()%files.size());

        iv.setBackground(null);
        iv.setImageURI(Uri.parse(files.get(tmp).getAbsolutePath()));
    }


    boolean DBConnection(String name){
        if (!SQLOperations.isConnected())
            SQLOperations.init();
        if (SQLOperations.isConnected()) {
            try {
                SQLOperations.runUpdateStatement(
                        "update bins set playCount = playCount+1 where name = \""
                                + name + "\"");
                return true;
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return false;
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
