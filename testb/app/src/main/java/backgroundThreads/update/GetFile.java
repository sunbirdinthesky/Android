package backgroundThreads.update;


import android.app.AlertDialog;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.zhilian.sunbird.demo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import ftpOperation.FtpOperations;
import sqlOperation.SqlOperation;
import vars.AdFile;
import vars.Vars;

/**
 * Created by SunBird on 2016/2/11.
 */

public class GetFile extends Thread {
    SqlOperation sqlOperation = new SqlOperation();

    public void run() {
        try {
            while (!Thread.interrupted()) {
                init();
                if (sqlOperation.isConnected() || sqlOperation.SqlInit()) {
                    try {
                        update();
                        makeConfigFiles();
                        checkLocalFiles();
                        upgrade();
                    } catch (Exception e) {
                        e.printStackTrace();
                        offLineCheckFiles();
                    }
                } else {
                    offLineCheckFiles();
                }

                Thread.sleep(60000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void update() throws Exception {
        //开始版本检测
        sqlOperation.SqlQuery("select * from versionCtrl");
        if (!sqlOperation.rSet.next()) {
            throw new Exception();
        }
        System.out.println(Vars.version);
        if (sqlOperation.rSet.getString("client").compareTo(Vars.version) > 0) {
            Vars.versionCorrect = false;
            throw new Exception();
        }


        //校验并注册本机
        System.out.println("开始校验并注册本机");
        sqlOperation.SqlQuery("select * from device where imei = \""
                + Vars.imei.substring(1) + "\"");
        if (!sqlOperation.rSet.next()) {
            sqlOperation.SqlUpdate("insert into device(imei) values(\""
                    + Vars.imei.substring(1) + "\")");
        } else {
            Vars.nItem = sqlOperation.rSet.getInt("nItem");
        }


        /*************************
         * create table ads (
         name char(32) not null,
         playCount int null default 0,
         md5 char(64) not null,
         begin char(12) null default "0000-00-00",
         end char(12) null default "0000-00-00",
         company char(32) null default "n/a"
         );
         *************************/

        //校验普通广告
        System.out.println("校验普通广告");
        Vars.ads.clear();
        sqlOperation.SqlQuery("select * from ads");
        while (sqlOperation.rSet.next()) {
            AdFile adFile = new AdFile();
            adFile.name = sqlOperation.rSet.getString("name");
            adFile.playCnt = sqlOperation.rSet.getInt("playCount");
            adFile.md5 = sqlOperation.rSet.getString("md5");
            adFile.begin = sqlOperation.rSet.getString("begin");
            adFile.end = sqlOperation.rSet.getString("end");
            adFile.company = sqlOperation.rSet.getString("company");
            Vars.ads.add(adFile);
        }

        for (AdFile tmp : Vars.ads) {
            sqlOperation.SqlQuery("select * from ads" + tmp.name
                    + " where imei = \"" + Vars.imei.substring(1)
                    + "\"");
//                System.out.println("begin :" + tmp.begin);
//                System.out.println("end :" + tmp.end);
//                System.out.println("now :" + GetNowDate());
//                System.out.println("result :" + GetNowDate().compareTo(tmp.begin));
//                System.out.println("result :" + GetNowDate().compareTo(tmp.end));

            if (!sqlOperation.rSet.next() || sqlOperation.rSet.getInt("playCount") <= 0) {
                Vars.ads.remove(tmp);
            }
        }

        //校验滚动广告
        System.out.println("校验滚动广告");
        Vars.repeats.clear();
        sqlOperation.SqlQuery("select * from repeats");
        while (sqlOperation.rSet.next()) {
            AdFile adFile = new AdFile();
            adFile.name = sqlOperation.rSet.getString("name");
            adFile.playCnt = sqlOperation.rSet.getInt("playCount");
            adFile.md5 = sqlOperation.rSet.getString("md5");
            adFile.begin = sqlOperation.rSet.getString("begin");
            adFile.end = sqlOperation.rSet.getString("end");
            adFile.company = sqlOperation.rSet.getString("company");
            Vars.repeats.add(adFile);
        }

        for (AdFile tmp : Vars.repeats) {
            sqlOperation.SqlQuery("select * from repeats" + tmp.name
                    + " where imei = \"" + Vars.imei.substring(1)
                    + "\"");
            if (!sqlOperation.rSet.next() ) {
                Vars.repeats.remove(tmp);
                continue;
            }

            if (GetNowDate().compareTo(tmp.begin) < 0
                    || GetNowDate().compareTo(tmp.end) > 0) {
                Vars.repeats.remove(tmp);
            }
        }
        System.out.println("校验完成");
    }

    void makeConfigFiles() throws Exception {
        System.out.println("创建本地配置文件");
        for (AdFile tmp : Vars.ads) {
            File cfg = new File(Vars.localPathRoot + "/AdMachine/adscfg/"
                    + tmp.name);
            if (cfg.exists()) {
                cfg.delete();
            }
            FileWriter fw = new FileWriter(cfg);
            fw.write(tmp.begin + '\n' + tmp.end);
            fw.flush();
            fw.close();
        }
        for (AdFile tmp : Vars.repeats) {
            File cfg = new File(Vars.localPathRoot + "/AdMachine/repeatscfg/"
                    + tmp.name);
            if (cfg.exists()) {
                cfg.delete();
            }
            FileWriter fw = new FileWriter(cfg);
            fw.write(tmp.begin + '\n' + tmp.end);
            fw.flush();
            fw.close();
        }
        System.out.println("创建完成");
    }

    public void checkLocalFiles() throws IOException {
        System.out.println("检查本地文件");
        File file = new File(Vars.localPathRoot + "/AdMachine/ads");
        File[] files = file.listFiles();
        if (files != null) {
            Vector<String> name = new Vector<>();
            Vector<String> md5 = new Vector<>();
            for (AdFile tmp : Vars.ads) {
                name.add(tmp.name);
                md5.add(tmp.md5);
            }

            for (File tmp : files) {
                if (!name.contains(tmp.getName())
                        || !md5.contains(getMd5ByFile(tmp))) {
                    deleteFile(tmp);
                } else {
                    Vars.ads.remove(name.indexOf(tmp.getName()));
                }
            }
        }

        file = new File(Vars.localPathRoot + "/AdMachine/repeats");
        files = file.listFiles();
        if (files != null) {
            Vector<String> name = new Vector<>();
            Vector<String> md5 = new Vector<>();
            for (AdFile tmp : Vars.repeats) {
                name.add(tmp.name);
                md5.add(tmp.md5);
            }

            for (File tmp : files) {
                if (!name.contains(tmp.getName())
                        || !md5.contains(getMd5ByFile(tmp))) {
                    deleteFile(tmp);
                } else {
                    Vars.repeats.remove(name.indexOf(tmp.getName()));
                }
            }
        }
        System.out.println("本地文件检查完毕");
    }

    void offLineCheckFiles() {
        System.out.println("网络已断开，开始离线检测");
        File file = new File(Vars.localPathRoot + "/AdMachine/ads");
        File[] files = file.listFiles();
        if (files != null) {
            for (File tmp : files) {
                File cfg = new File(Vars.localPathRoot
                        + "/AdMachine/adscfg/" + tmp.getName());
                try {
                    FileReader fr = new FileReader(cfg);
                    char[] contains = new char[25];
                    fr.read(contains);
                    fr.close();

                    String begin = new String();
                    String end = new String();
                    boolean mark = false;
                    for (char c : contains) {
                        if (c == '\n') {
                            mark = true;
                            continue;
                        }

                        if (!mark) {
                            begin += c;
                        } else {
                            end += c;
                        }
                    }

                    if (GetNowDate().compareTo(begin) < 0
                            || GetNowDate().compareTo(end) > 0) {
                        deleteFile(tmp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("配置文件丢失，将删除文件"
                            + tmp.getName());
                    deleteFile(tmp);
                }
            }
        }

        file = new File(Vars.localPathRoot + "/AdMachine/repeats");
        files = file.listFiles();
        if (files != null) {
            for (File tmp : files) {
                File cfg = new File(Vars.localPathRoot
                        + "/AdMachine/repeatscfg/" + tmp.getName());
                try {
                    FileReader fr = new FileReader(cfg);
                    char[] contains = new char[25];
                    fr.read(contains);
                    fr.close();

                    String begin = new String();
                    String end = new String();
                    boolean mark = false;
                    for (char c : contains) {
                        if (c == '\n') {
                            mark = true;
                            continue;
                        }

                        if (!mark) {
                            begin += c;
                        } else {
                            end += c;
                        }
                    }

                    if (GetNowDate().compareTo(begin) < 0
                            || GetNowDate().compareTo(end) > 0) {
                        deleteFile(tmp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("配置文件丢时，将删除文件"
                            + tmp.getName());
                    deleteFile(tmp);
                }
            }
        }
        System.out.println("离线检测完成");
    }

    void upgrade() {
        System.out.println("开始下载普通广告");
        FtpOperations ftp = new FtpOperations();
        for (AdFile tmp : Vars.ads) {
            File file = new File(Vars.localPathRoot
                    + "/AdMachine/ads/tmp_" + tmp.name);
            ftp.download(file, "/AdMachine/ads/", tmp.name);

            renameFile(file, tmp.name);
        }
        System.out.println("开始下载滚动广告");
        for (AdFile tmp : Vars.repeats) {
            File file = new File(Vars.localPathRoot
                    + "/AdMachine/repeats/tmp_" + tmp.name);
            ftp.download(file, "/AdMachine/repeats/", tmp.name);
            renameFile(file, tmp.name);
        }
        System.out.println("下载完成");
    }

    public String GetNowDate() {
        String temp_str = "";
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        temp_str = sdf.format(dt);
        return temp_str;
    }

    void renameFile(final File file, final String newName) {
        new Thread() {
            @Override
            public void run() {
                System.out.println("尝试更改临时文件名" + file.getAbsolutePath());
                while (!file.renameTo(new File(file.getParent() + '/' + newName))) {
                    try {
                        Thread.sleep(300);
                        System.out.println("更改临时文件名失败，准备重试");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ;
                System.out.println("更改临时文件名成功");
            }
        }.start();
    }

    void deleteFile(final File file) {
        new Thread() {
            @Override
            public void run() {
                while (!file.delete()) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ;
            }
        }.start();
    }

    public static String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }


    public static void init() {
        File adFloder = new File(Vars.localPathRoot + "/AdMachine/");
        if (!adFloder.exists())
            adFloder.mkdir();
        adFloder = new File(Vars.localPathRoot + "/AdMachine/ads/");
        if (!adFloder.exists())
            adFloder.mkdir();
        adFloder = new File(Vars.localPathRoot + "/AdMachine/repeats/");
        if (!adFloder.exists())
            adFloder.mkdir();

        adFloder = new File(Vars.localPathRoot + "/AdMachine/adscfg/");
        if (!adFloder.exists())
            adFloder.mkdir();
        adFloder = new File(Vars.localPathRoot + "/AdMachine/repeatscfg/");
        if (!adFloder.exists())
            adFloder.mkdir();
    }
}