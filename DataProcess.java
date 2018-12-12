 
import java.io.File;

import static java.lang.Math.min;

class DataProcess {

    private int cnt=0;
    int inBytePerFrame;
    int[] totData;
    private String filePath;
    int totLength;
    private int haveSavedNumber;

    DataProcess() {this.cnt=0;} // for test DataProcess

    DataProcess(String InputString, int inBytePerFrame, int type) {
        this.filePath = InputString;
        this.cnt = 0;
        this.inBytePerFrame = inBytePerFrame;
        if (type == 0) {
            File file = new File(InputString);
            file.delete();
            //haveSavedNumber = 0;
            //this.totLength = inAllByteNumber;
        } else {
            this.totData = new int[MAC.allBit];
            this.totLength = ReadDataFromFile.readDataFromFile(InputString, this.totData);
        }
    }

    int getNewFrame(int[] buffer) {
        if (this.cnt == -1) {
            this.cnt = 0;
            return 0;
        }
        if (buffer.length<this.inBytePerFrame) {
            System.out.println("Buffer's length wrong");
        }
        int cnt = this.cnt;
        if ((cnt+1)*this.inBytePerFrame<this.totLength) {
            System.arraycopy(this.totData, cnt * this.inBytePerFrame,
                    buffer, 0,
                    this.inBytePerFrame);
            this.cnt++;
            return this.inBytePerFrame;
        }
        int length = this.totLength - cnt*this.inBytePerFrame;
        System.arraycopy(this.totData, cnt * this.inBytePerFrame, buffer, 0, length);
        this.cnt = -1;
        return length;
    }

    boolean saveData(int[] buffer, int bufferSize) {
        haveSavedNumber += bufferSize;
        int[] data = new int[bufferSize];
        System.arraycopy(buffer, 0, data, 0, bufferSize);
        SaveDataToFile.saveDataToFile(this.filePath,data);
        //SaveTxt.saveTxt(this.filePath,data);
        if (bufferSize < this.inBytePerFrame) {
            return true;
        } else {
            return false;
        }
    }
}