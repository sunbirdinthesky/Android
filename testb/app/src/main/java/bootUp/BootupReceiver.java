package bootUp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ui.Main;

/**
 * Created by SunBird on 2016/4/14.
 */
public class BootupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //better delay some time.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setClass(context, Main.class);
        context.startActivity(i);

    }

}
