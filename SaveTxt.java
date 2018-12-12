 

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class SaveTxt {

        public static void saveTxt(String inputPath,int[] data) {
            try {
                String output = Arrays.toString(data);
                output = output.substring(1,output.length()-1);
                output = output.replace(",","");
                output = output.replace(" ","");
                FileWriter writer = new FileWriter(inputPath, true);
                writer.write(output);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
}