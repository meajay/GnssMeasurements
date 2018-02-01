package testme.java.com.gpsdatalogger.utility;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

import testme.java.com.gpsdatalogger.Constants;

/**
 * Created by achau on 31-01-2018.
 */

public  class DeleteFile implements FileFilter {
    private final List<File> deleteableFiles ;

    public DeleteFile(File... retainedFiles) {
        this.deleteableFiles = Arrays.asList(retainedFiles);
    }


    @Override
    public boolean accept(File pathname) {
        if (pathname == null || !pathname.exists()) {
            return false;
        }
        if (deleteableFiles.contains(pathname)) {
            return false;
        }
        return pathname.length() < Constants.MINIMUM_USABLE_BYTES;
    }
}
