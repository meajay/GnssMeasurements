package testme.java.com.gpsdatalogger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.compat.*;
import android.support.v4.*;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import testme.java.com.gpsdatalogger.utility.DeleteFile;
import tutorial.Gnss;

import static android.provider.Telephony.ThreadsColumns.ERROR;

/**
 * Created by achau on 31-01-2018.
 */

public class SaveFile extends FileProvider {

    private Context context;
    private GpsLoggerActivity.UIComponent component;
    private BufferedWriter fWriter;
    private File super_file ;
    private String fPath , fileName ;


    public SaveFile(Context context) {
        this.context = context ;
    }

    public void saveFileLog() {
        if (externalMemoryAvailable()) {
                if(getAvailableExternalMemorySize().length()>3){
                   createFile();
                }
                else{
                    Toast.makeText(context, "MEMORY NOT AVAILABLE", Toast.LENGTH_SHORT).show();
                }

        }
    }

    public void createFile(){
       File  file = new File(Environment.getExternalStorageDirectory(), Constants.FILE_NAME);
        file.mkdirs();
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyy_MM_dd_HH_mm_ss");
        fileName = String.format("%s_%s.txt", Constants.FILE_NAME, formatter.format(now));
        File currentFile = new File(file, fileName);
        fPath = currentFile.getAbsolutePath();
        BufferedWriter currentFileWriter;

        try {
            currentFileWriter = new BufferedWriter(new FileWriter(currentFile));
            currentFileWriter.write("----------FILE STARTED-----------");
            currentFileWriter.newLine();
            currentFileWriter.write("##");
            currentFileWriter.newLine();
            currentFileWriter.write("Manufacturer : " + Build.MANUFACTURER);
            currentFileWriter.newLine();
            currentFileWriter.write("Model : " + Build.MODEL);
            currentFileWriter.newLine();
            currentFileWriter.write("Version : " + Build.VERSION.RELEASE);
            currentFileWriter.newLine();
            currentFileWriter.write("##");


        } catch (IOException e) {
            Toast.makeText(context, "NOT ABLE TO OPEN FILE",Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            Gnss.Location gnssLocation = Gnss.Location.newBuilder().
                    setIsLocationChanged(false)
                    .setIsLocationStatusChanged(false)
                    .setIsProviderEnabled(true)
                    .setAccuracy(19.9f)
                    .setAltitude(12.25f)
                    .setLatitude(19.90f)
                    .setLatitude(65.43f).build();



           currentFileWriter.write(gnssLocation.toByteString().toString());
        }
        catch (IOException ex){
            Toast.makeText(context, "ERROR IN WRITING FILE",Toast.LENGTH_SHORT).show();
            return;
        }

        super_file = file ;

        fWriter = currentFileWriter ;

//        if (currentFileWriter != null) {
//            try {
//                currentFileWriter.close();
//                fileOutputStream.close();
//            } catch (IOException e) {
//               Toast.makeText(context,"UNABLE TO CLOSE FILE WRITER", Toast.LENGTH_SHORT) ;
//                return;
//            }
//        }


        //deleteAllEmptyFiles(super_file);
    }


    private  boolean externalMemoryAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Toast.makeText(context, "CANNOT WRITE IN STORAGE", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(context, "CANNOT READ STORAGE", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private String getAvailableExternalMemorySize() {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return formatSize(availableBlocks * blockSize);
        }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public void deleteAllEmptyFiles(File file){
        DeleteFile filter = new DeleteFile(file);
        for (File existingFile : file.listFiles(filter)) {
            existingFile.delete();
        }
        // - Trim the number of files with data
        File[] existingFiles = file.listFiles();
        int filesToDeleteCount = existingFiles.length - Constants.MAX_FILES_STORED;
        if (filesToDeleteCount > 0) {
            Arrays.sort(existingFiles);
            for (int i = 0; i < filesToDeleteCount; ++i) {
                existingFiles[i].delete();
            }
        }
    }

    public void send() {
        if (super_file == null) {
            return;
        }

        try {
            fWriter.write("----------FILE ENDED----------- \n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.setType("*/*");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "gnss_log");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        Toast.makeText(context, "FILE OPENED " + fPath , Toast.LENGTH_SHORT).show();

        Uri fileURI =
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", super_file);
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileURI);
        getComponent().startActivity(emailIntent);

        if (fWriter != null) {
            try {
                fWriter.flush();
                fWriter.close();
                fWriter = null;
            } catch (IOException e) {
                 Toast.makeText(context ,"UNABLE TO CLOSE ALL STREAMS" , Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    public synchronized GpsLoggerActivity.UIComponent getComponent() {
        return component;
    }

    public synchronized void setComponent(GpsLoggerActivity.UIComponent component) {
        this.component = component;
    }

}
