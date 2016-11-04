package ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zhilian.sunbird.demo.R;

import sqlOperation.SqlOperation;
import vars.Vars;

public class Setup extends AppCompatActivity {
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mContentView = findViewById(R.id.setupfullBackgroung);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        hide();

        final EditText passwd = (EditText) findViewById(R.id.passwd);
        final EditText item = (EditText) findViewById(R.id.item);
        final EditText pos = (EditText) findViewById(R.id.pos);

        final Button ok = (Button) findViewById(R.id.ok);
        final Toast tmp = Toast.makeText(this, "", Toast.LENGTH_LONG);
        Button posok = (Button)findViewById(R.id.posOk);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        tmp.setText("信息填写错误，物品数量错误？");
                        tmp.show();
                        break;
                    case 2:
                        tmp.setText("设备信息已更新");
                        tmp.show();
                        break;
                    case 3:
                        tmp.setText("密码错误，未更改");
                        tmp.show();
                        break;
                    case 4:
                        tmp.setText("连接失败，未更改");
                        tmp.show();
                        break;
                    case 5:
                        tmp.setText("服务器没有授权");
                        tmp.show();
                        break;
                    case 6:
                        tmp.setText("信息填写错误，地址中有中文字符或者特殊字符？");
                        tmp.show();
                        break;
                }
            }
        };

        posok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < pos.getText().toString().length(); i++) {
                    char ch = pos.getText().toString().charAt(i);
                    if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                            || (ch >= '0' && ch <= '9') || (ch == '_')) {
                    } else {
                        Message.obtain(handler, 6).sendToTarget();
                        return;
                    }
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SqlOperation sqlOperation = new SqlOperation();
                        if (sqlOperation.isConnected() || sqlOperation.SqlInit()) {
                            try {
                                sqlOperation.SqlQuery("select setup from device where imei = \""
                                        + Vars.imei.substring(1) + "\"");
                                if (sqlOperation.rSet.next() && sqlOperation.rSet
                                        .getString("setup").equals("1")) {
                                    sqlOperation.SqlUpdate("update device set location = \""
                                                    + pos.getText().toString() + "\" where imei = \""
                                                    + Vars.imei.substring(1) + "\""
                                    );
                                    Message.obtain(handler, 2).sendToTarget();
                                } else {
                                    Message.obtain(handler, 5).sendToTarget();
                                }
                                return;
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        Message.obtain(handler, 4).sendToTarget();
                    }
                }).start();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
                try {
                    Integer.valueOf(item.getText().toString());
                } catch (Exception e) {
                    Message.obtain(handler, 1).sendToTarget();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SqlOperation sqlOperation = new SqlOperation();
                        if (sqlOperation.isConnected() || sqlOperation.SqlInit()) {
                            try {
                                sqlOperation.SqlQuery("select passwd from device where imei = \""
                                        + Vars.imei.substring(1) + "\"");
                                if (sqlOperation.rSet.next() && passwd.getText().toString().equals(sqlOperation.rSet.getString("passwd"))) {
                                    sqlOperation.SqlUpdate("update device set nItem = "
                                                    + item.getText().toString() + " where imei = \""
                                                    + Vars.imei.substring(1) + "\""
                                    );
                                    Vars.nItem = Integer.valueOf(item.getText().toString());
                                    Message.obtain(handler, 2).sendToTarget();
                                } else {
                                    Message.obtain(handler, 3).sendToTarget();
                                }
                                return;
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        Message.obtain(handler, 4).sendToTarget();
                    }
                }).start();
            }
        });




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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        hide();
        return super.onKeyUp(keyCode, event);
    }
}
