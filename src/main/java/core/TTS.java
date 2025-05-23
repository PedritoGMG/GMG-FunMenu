package core;

import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.codec.digest.DigestUtils;

public class TTS {

    // Parámetros fijos del motor de voz
    private static final int ENGINE = 4;
    private static final int LANG = 1;
    private static final int VOICE = 5;
    private static final int ACC_ID = 5883747;
    private static final String SECRET = "uetivb9tb8108wfj";
    
    public static String getTTSUrl(String text) throws Exception {
        String encodedText = URLEncoder.encode(text, "UTF-8");
        String magic = ENGINE + "" + LANG + VOICE + text + "1mp3" + ACC_ID + SECRET;
        String checksum = DigestUtils.md5Hex(magic).toLowerCase();

        return String.format(
                "http://cache-a.oddcast.com/tts/gen.php?EID=%d&LID=%d&VID=%d&TXT=%s&IS_UTF8=1&EXT=mp3&FNAME=&ACC=%d&API=&SESSION=&CS=%s&cache_flag=3",
                ENGINE, LANG, VOICE, encodedText, ACC_ID, checksum
        );
    }
    
    public static File fetchAudioToFile(String text) throws Exception {
        String url = getTTSUrl(text);
        File tempFile = File.createTempFile("tts_audio", ".mp3");
        tempFile.deleteOnExit();

        try (InputStream in = new URL(url).openStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        return tempFile;
    }

    
}
