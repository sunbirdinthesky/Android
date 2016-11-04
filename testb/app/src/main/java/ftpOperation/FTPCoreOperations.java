package ftpOperation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FTPCoreOperations {
    void upload(FTPClient ftpClient, File file,  
            String remotePathName, String remoteName) throws Exception {  
        try {  
            changeDirectory(ftpClient, remotePathName);  
            uploadFile(ftpClient, file, remotePathName, remoteName);  
            backToRootDirectory(ftpClient);  
        } catch (IOException e) {  
        }  
    }  
    
    void rename(FTPClient ftpClient, String name,  
            String remotePathName, String remoteName) throws Exception {  
        try {  
            changeDirectory(ftpClient, remotePathName); 
        	ftpClient.rename(remoteName, name);
            backToRootDirectory(ftpClient);  
        } catch (IOException e) {  
        }  
    }   
    
    void download(FTPClient ftpClient, File file,  
            String remotePathName, String remoteName) throws Exception {  
        try {  
            changeDirectory(ftpClient, remotePathName);  
            downloadFile(ftpClient, file, remotePathName, remoteName);  
            backToRootDirectory(ftpClient);  
        } catch (IOException e) {  
        }  
    } 
  
    void changeDirectory(FTPClient ftpClient, String path)  
            throws IOException {  
        int nextSeperator = path.indexOf("/", 1);  
        String currentPath = null;  
        if (nextSeperator < 0) {  
            nextSeperator = path.length();  
            currentPath = path.substring(1, nextSeperator);  
            changeDirectory0(ftpClient, currentPath);  
            return;  
        } else {  
            currentPath = path.substring(1, nextSeperator);  
            changeDirectory0(ftpClient, currentPath);  
            changeDirectory(ftpClient, path.substring(nextSeperator));  
        }  
    }  
    
    void changeDirectory0(FTPClient ftpClient, String path)  
            throws IOException {  
        if (!ftpClient.changeWorkingDirectory(path)) {  
            ftpClient.makeDirectory(path);  
        }  
        ftpClient.changeWorkingDirectory(path);  
    }  
     
    void backToRootDirectory(FTPClient ftpClient)  
            throws IOException {  
        ftpClient.changeWorkingDirectory("/");  
    }  
  
    void uploadFile(FTPClient ftpClient, File file,  
            String remotePathName, String remoteName) throws Exception {  
        String localPathName = file.getAbsolutePath();  
        FTPFile[] files = ftpClient.listFiles(remoteName);  
        if (files.length == 1) {
            if (!ftpClient.deleteFile(remoteName)) {  
                throw new Exception("fail to delete remote file ["  
                        + remotePathName + "] before uploading");  
            }  
        }  
        File f = new File(localPathName);  
        InputStream is = new FileInputStream(f);  
        if (!ftpClient.storeFile(remoteName, is)) {  
            is.close();  
        }  
        is.close();  
    }
    
    static void downloadFile(FTPClient ftpClient, File file,  
            String remotePathName, String remoteName) throws Exception {  
        String localPathName = file.getAbsolutePath();  
        File local = new File(localPathName);
        if (local.exists()) {
        	local.delete();
        }
        local.createNewFile();
        OutputStream output;
        output = new FileOutputStream(local);
        ftpClient.retrieveFile(remoteName, output);
        output.close();
    }
}
