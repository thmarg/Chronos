package tm.android.chronos.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Files.walkTreeFile and so on are available starting from api 26 !!!
 * So we do it by hand
 */
public class FilesUtils {
    private static final List<String> audioFilesExtensions;

    static {
        audioFilesExtensions = Collections.unmodifiableList(Arrays.asList(".flac",".mp3",".ogg",".wav",".wave",".opus"));
    }
    /**
     * Return all file inside dir and sub dirs, which names end with one of list "match", or all if the list is null or empty
     * @param directory, a file denoting a directory
     * @param match, list of accepted end of files names
     * @return the list of all files inside this directory, included files in sub dirs .... or an empty list
     */
    public static List<String> getFiles(File directory, List<String> match) {
        if (directory == null || !directory.isDirectory())
            return Collections.emptyList();

        List<String> list = new ArrayList<>(50);
        for (File file : directory.listFiles()) {
            if (file.isFile() && (match == null || match.isEmpty() || endWith(match, file.getName())))
                list.add(file.getAbsolutePath());
            else if (file.isDirectory()) {
                List<String> lst = getFiles(file, match);
                if (!lst.isEmpty())
                    list.addAll(lst);
            }

        }
        return list;
    }


    public static int getFilesCount(File directory, List<String> match) {
        if (directory == null || !directory.isDirectory())
            return 0;

        int count = 0;
        for (File file : directory.listFiles())
            if (file.isFile() && (match == null || endWith(match, file.getName())))
                count++;
            else if (file.isDirectory())
                count += getFilesCount(file, match);

        return count;
    }


    private static boolean endWith(List<String> match, String path) {
        for (String tok : match)
            if (path.endsWith(tok)) return true;
        return false;
    }


    public static List<String> getAudioFilesExtensions() {
        return audioFilesExtensions;
    }
}
