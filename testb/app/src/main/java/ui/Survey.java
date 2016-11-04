package ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zhilian.sunbird.demo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Random;
import java.util.Vector;

import backgroundThreads.fullScreenKeeper.FullScreenKeeper;
import sql.SQLOperations;
import vars.Vars;

public class Survey extends AppCompatActivity {
    private View mContentView;
    Random random = new Random(23333);
    public Handler handler;
    Thread keeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey);
        mContentView = findViewById(R.id.surveyBackground);

        initBitmap();
        hide();

        ImageButton suWatchAds = (ImageButton)findViewById(R.id.suWatchAds);
        suWatchAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Survey.this, WatchAds.class));
            }
        });

        ImageButton suScan = (ImageButton)findViewById(R.id.suScan);
        suScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Survey.this, Scan.class));
            }
        });

        ImageButton suReturn = (ImageButton)findViewById(R.id.suReturn);
        suReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton suRefresh = (ImageButton)findViewById(R.id.suRefresh);
        suRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    questionInit ();
                }catch (IOException e) {

                }
            }
        });

        ImageButton suHome = (ImageButton)findViewById(R.id.suHome);
        suHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Survey.this, Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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

//#####################################
        try {
            questionInit();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void initBitmap () {
        ImageView iv = (ImageView)findViewById(R.id.surveyBackground);
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

    private void questionInit () throws IOException{
        final Button finish = new Button(this);
        LinearLayout ques = (LinearLayout)findViewById(R.id.questionlist);
        ques.removeAllViews();
        String name;
        Vector <TextView> questions = new Vector<>();
        final Vector <RadioGroup> radioGroups = new Vector<>();
        Vector <RadioButton> ans = new Vector<>();
        Vector <Integer> AnsPerQues = new Vector<>();
        int nQues = 0;
        int nRadioGroups = 0;
        int nAns = 0;
        int cnt = 0;

        File bins = new File(Vars.localPathRoot + "/AdMachine/ques/");
        File[] file = bins.listFiles();
        Vector <File> files = new Vector<>();


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

        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(files.get(tmp)), "Unicode"));

        System.out.println("file reading");
        while((name = br.readLine())!=null && name != null) {
            Log.w("contain", name);
            switch (name.charAt(0)) {
                case 'q':
                    questions.add(new TextView(this));
                    questions.get(nQues).setText(name.substring(1));
                    ques.addView(questions.get(nQues++),
                            ViewGroup.LayoutParams.FILL_PARENT, 100);
                    nRadioGroups++;
                    if (cnt != 0) {
                        AnsPerQues.add(cnt);
                        cnt = 0;
                    }
                    break;
                case 'a':
                    if (cnt == 0) {
                        radioGroups.add(new RadioGroup(this));
                        ques.addView(radioGroups.get(nRadioGroups-1),
                                ViewGroup.LayoutParams.FILL_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                    ans.add(new RadioButton(this));
                    ans.get(nAns).setText(name.substring(1));
                    radioGroups.get(nRadioGroups-1).addView(ans.get(nAns++),
                            ViewGroup.LayoutParams.FILL_PARENT, 100);
                    cnt++;
                    break;
                case 'b':
                    if (cnt == 0) {
                        radioGroups.add(new RadioGroup(this));
                        ques.addView(radioGroups.get(nRadioGroups-1),
                                ViewGroup.LayoutParams.FILL_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                    ans.add(new RadioButton(this));
                    ans.get(nAns).setText(name.substring(1));
                    radioGroups.get(nRadioGroups-1).addView(ans.get(nAns++),
                            ViewGroup.LayoutParams.FILL_PARENT, 100);
                    cnt++;
                    break;
                case 'c':
                    if (cnt == 0) {
                        radioGroups.add(new RadioGroup(this));
                        ques.addView(radioGroups.get(nRadioGroups-1),
                                ViewGroup.LayoutParams.FILL_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                    ans.add(new RadioButton(this));
                    ans.get(nAns).setText(name.substring(1));
                    radioGroups.get(nRadioGroups-1).addView(ans.get(nAns++),
                            ViewGroup.LayoutParams.FILL_PARENT, 100);
                    cnt++;
                    break;
                case 'd':
                    if (cnt == 0) {
                        radioGroups.add(new RadioGroup(this));
                        ques.addView(radioGroups.get(nRadioGroups-1),
                                ViewGroup.LayoutParams.FILL_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                    ans.add(new RadioButton(this));
                    ans.get(nAns).setText(name.substring(1));
                    radioGroups.get(nRadioGroups-1).addView(ans.get(nAns++),
                            ViewGroup.LayoutParams.FILL_PARENT, 100);
                    cnt++;
                    break;
                default:
                    break;
            }
        }
        br.close();

        final String fileName = files.get(tmp).getName();
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean answered = true;
                String ans = new String();
                for (int i = 0; i < radioGroups.size(); i++) {
                    Log.w("childs", String.valueOf(radioGroups.get(i).getChildCount()));
                    Log.w("checked ID", String.valueOf(radioGroups.get(i).getCheckedRadioButtonId()));
                    if (radioGroups.get(i).getCheckedRadioButtonId() == -1) {
                        answered = false;
                    }
                    else {
                        ans += String.valueOf((radioGroups.get(i).getCheckedRadioButtonId()
                            +3)%radioGroups.get(i).getChildCount());
                    }
                }
                final String finalAns = ans;
                if (answered) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DBConnection(fileName, finalAns);
                        }
                    }).start();
                    startActivity(new Intent(Survey.this, Finish.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                }
                else {
                    new AlertDialog.Builder(Survey.this)
                            .setTitle("提示")
                            .setMessage("请先把问卷回答完偶~^.^")
                            .setPositiveButton("偶知道了", null)
                            .show();
                }
            }
        });
        finish.setText("提交");
        ques.addView(finish, ViewGroup.LayoutParams.FILL_PARENT, 150);
    }

    boolean DBConnection(String name, String ans){
        if (!SQLOperations.isConnected())
            SQLOperations.init();
        if (SQLOperations.isConnected()) {
            try {
                SQLOperations.runUpdateStatement("insert ans" + name + " values(\"" + ans +
                        "\")");
                SQLOperations.runUpdateStatement("update " +
                        "ques set playCount = playCount+1 where name = \"" + name + "\"");
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
