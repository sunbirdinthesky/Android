package backgroundThreads.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import java.util.Set;

import vars.Vars;

/**
 * Created by SunBird on 2016/4/24.
 */
public class SendMessage extends Thread {

    BluetoothAdapter mBluetoothAdapter;

    public void run() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println("蓝牙初始化");

        while (!mBluetoothAdapter.isEnabled()) {
            try {
                Thread.sleep(800);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("等待蓝牙开启");
            }
        }

        while (!Thread.interrupted()) {
            try {
                Thread.sleep(5000);
                ConnectBT();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ConnectBT () {
        boolean connected = false;
        BluetoothChatService bluetooth = new BluetoothChatService(null, null);
        bluetooth.start();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        while (pairedDevices.size() == 1) {
            if (bluetooth.getState() == 0 || bluetooth.getState() == 1) {
                System.out.println("蓝牙配对中");
                for (BluetoothDevice device : pairedDevices) {
                    try {
                        System.out.println(device.getName());
                        bluetooth.connect(device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (bluetooth.getState() == 3) {
                Vars.bluetooth = 1;
                if (!connected) {
                    System.out.println("蓝牙已连接");
                    connected = true;
                }
                if (Vars.send) {
                    byte[] out = new byte[2];
                    out[0] = (byte)0x55;
                    out[1] = (byte)0xff;
                    bluetooth.write(out);
                    Vars.send = false;
                    System.out.println("蓝牙：数据已发送");
                }
            }else {
                Vars.bluetooth = 0;
                if (connected) {
                    System.out.println("蓝牙连接已丢失，重新连接");
                    connected = false;
                }
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("蓝牙初始化失败，准备重新启动");
        bluetooth.stop();
    }
}
