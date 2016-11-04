package ftpOperation;

import java.io.File;  
import java.io.IOException;  
import org.apache.commons.net.ftp.FTP;  
import org.apache.commons.net.ftp.FTPClient;  
import org.apache.commons.net.ftp.FTPFile;  
  
public class FtpOperations extends FTPCoreOperations {  
	String ip = vars.Vars.ip;
	int port = vars.Vars.port;
	String userName = vars.Vars.ftpName;  
    String password = vars.Vars.ftpPasswd; 

	/** 
     * 下载
     *  
     * @param localfile 
     * @param remotePathName 
     * @param remoteName 
     */  
    public boolean download(File file, String remotePathName, 
            String remoteName) {  
        FTPClient ftpClient = new FTPClient();  
        try {  
            ftpClient.connect(ip, port);  
            ftpClient.login(userName, password);  
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);  
            ftpClient.enterLocalPassiveMode();  
  
            download(ftpClient, file, remotePathName, remoteName);  
  
        } catch (Exception e) {  
            System.out.println("fail to download files : " + e.getMessage()); 
            return false;
        } finally {  
            if (ftpClient != null && ftpClient.isConnected()) {  
                try {  
                    ftpClient.disconnect();  
                    System.out.println("file downloaded");
                    return true;
                } catch (IOException e) {  
                    System.out.println("ftp fails to disconnect : "  
                            + e.getMessage());  
                    return false;
                }  
            }  
        }
		return false;  
    } 
  
    
}  
