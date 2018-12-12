 

import java.io.*;
import java.util.Arrays;

public class SaveDataToFile {

    public static void saveDataToFile(String inputPath, int[] data) {
        try {
            OutputStream outputStream = new FileOutputStream(inputPath, true);
            for (int i = 0; i < data.length; i += 8) {
                int ans = 0;
                for (int j = 0; j < 8; j++) ans = (ans << 1) + data[i + j];
                outputStream.write(ans);
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
