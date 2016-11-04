package backgroundThreads.repeatKeeper;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import vars.Vars;

public class RepeatKeeper extends Thread{

    Handler handler;
    public RepeatKeeper(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                if (Vars.repeatAble) {
                    Thread.sleep(10000);
                    if (Thread.interrupted())
                        return;
                    if (Vars.repeatbreak || !Vars.repeatAble) {
                        Vars.repeatbreak = false;
                        continue;
                    }
                    Message mag = new Message();
                    mag.what = 0xf;
                    handler.sendMessage(mag);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}