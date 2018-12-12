 

import java.io.*;

public class ReadDataFromFile {

    public static int readDataFromFile(String inputPath, int[] data) {
        //int[] data = new int[MAC.allBit];
        InputStream reader = null;
        int nowReadNum = 0;
        try {
            reader = new FileInputStream(inputPath);
            int tmpChar;
            while ((tmpChar = reader.read()) != -1) {
                for (int i=7;i>=0;i--) {
                    data[nowReadNum] = ((tmpChar & (1<<i))>>i);
                    nowReadNum ++;
                }
                    if (nowReadNum == MAC.allBit)
                        break;
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("ReadDataFromFile HERE ");
        }
        return nowReadNum;
    }

}
