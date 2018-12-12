 

import java.util.Arrays;

public class TestSaveDataToFile {
    public static void test(String inputPath){
        int[] data = new int[8];
        for (int i=0;i<8;i++) data[i]=0;
        data[2]=data[3]=data[4]=1;
        data[7]=1;
        SaveDataToFile.saveDataToFile(inputPath,data);
    }
}
