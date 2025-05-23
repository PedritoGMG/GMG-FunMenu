package core.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class DataManager {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File DATA_FILE = new File("appdata.json");

    public static void save(AppData data) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(DATA_FILE, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AppData load(AppData appData) {
        if (!DATA_FILE.exists()) return appData;
        try {
            return mapper.readValue(DATA_FILE, AppData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return AppData.getInstance();
        }
    }
}
