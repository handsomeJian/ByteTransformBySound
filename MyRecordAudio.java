import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

class RecordTimeCounter extends Thread {

    private long beginDate;
    private long totTime;
    RecordTimeCounter(long totTime){
        this.totTime = totTime;
        MyRecordAudio.recordEnough = false;
    }

    private void TimeCounterStart(){
        this.beginDate = System.currentTimeMillis();
    }

    public void run() {
        TimeCounterStart();
        long nowDate;
        while (true) {
            nowDate = System.currentTimeMillis();
            if (nowDate - beginDate >= totTime) break;
        }
        MyRecordAudio.recordEnough = true;
    }
}


public class MyRecordAudio {

    private static float sampleRate = 44100.0F;
    private static int sampleSizeInBits = 8;
    private static int channels = 1;
    private static int frameSize = sampleSizeInBits*channels/8;
    private static float frameRate = 44100.0F;
    private static boolean bigEndian = false;
    public static boolean recordEnough = false;
    static AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,  sampleRate,
            sampleSizeInBits, channels, frameSize, frameRate, bigEndian);


    public static void recordAudio(String[] audioPath, long totTime){
        TargetDataLine line = null;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
        System.out.print("start\n");
        if (!AudioSystem.isLineSupported(info)) {
            // Handle the error ...
            System.out.print("out");
            exit(0);
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            // Handle the error ...
            System.out.print("Bad Audio");
        }


        // Assume that the TargetDataLine, line, has already
        // been obtained and opened.
        ByteArrayOutputStream out  = new ByteArrayOutputStream();
        int numBytesRead;
        byte[] data = new byte[line.getBufferSize() / 5];


        // Begin audio capture.
        line.start();
        //--------------------开启计时器----------------------
        RecordTimeCounter timeCounter = new RecordTimeCounter(totTime);
        Thread timeCounterThread = new Thread(timeCounter);
        timeCounterThread.start();
        //--------------------开启计时器----------------------

        // Here, stopped is a global boolean set by another thread.
        while (!recordEnough) {
            // Read the next chunk of data from the TargetDataLine.
            numBytesRead =  line.read(data, 0, data.length);
//            System.out.println(data[1234]);
//            System.out.println(line.getBufferSize()+"  buffer");
//            System.out.println(data.length+"  lenth");
//            System.out.println(numBytesRead+ " bytes");
            // Save this chunk of data.
            out.write(data, 0, numBytesRead);
        }
        line.stop();
        line.close();

        File fileOut = new File(audioPath[0]);
        byte audioData[] = out.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream ais = new AudioInputStream(bais,format, audioData.length / format.getFrameSize());

        AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
        if (AudioSystem.isFileTypeSupported(fileType,
                ais)) {
            try {
                AudioSystem.write(ais, fileType, fileOut);
            } catch (IOException e) {
                e.printStackTrace();
                exit(0);
            }
        }

    }

}
