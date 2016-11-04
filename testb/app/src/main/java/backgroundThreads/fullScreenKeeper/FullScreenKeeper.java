package backgroundThreads.fullScreenKeeper;

import android.app.ExpandableListActivity;
import android.os.Handler;
import android.os.Message;

/**
 * Created by SunBird on 2016/4/29.
 */
public class FullScreenKeeper extends Thread{
    Handler handler;
    public FullScreenKeeper(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Message mag = new Message();
                mag.what = 0xffff;
                handler.sendMessage(mag);
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
