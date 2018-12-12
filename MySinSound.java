import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

class SinSoundTimeCounter extends Thread {

    private long beginDate;
    private long totTime;
    SinSoundTimeCounter(long totTime){
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
        MySinSound.sinEnough = true;
    }
}


public class MySinSound {

    private static int sampleSizeInBits = 8;
    private static int channels = 1;
    private static int frameSize = sampleSizeInBits*channels/8;
    private static float frameRate = 44100.0F;
    private static boolean bigEndian = false;
    public static boolean recordEnough = false;
    private static int sampleRate = 44100;
    //public static double[] ans = new double[sampleRate];
    public static byte[] data = new byte[sampleRate];

    static AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,  sampleRate,
            sampleSizeInBits, channels, frameSize, frameRate, bigEndian);

    static boolean sinEnough = false;

    private static byte getByteValue(double angle) {
        float maxVol = 127f;
        return Integer.valueOf((int)Math.round(angle*maxVol)).byteValue();
    }


    public static void generateSin(long time,String[] audioPath){
        sinEnough = false;
        //--------------------开启计时----------------------
        SinSoundTimeCounter timeCounter = new SinSoundTimeCounter(time);
        Thread timeCounterThread = new Thread(timeCounter);
        timeCounterThread.start();
        //--------------------关闭计时------- ---------------
        for(int i=0; i<sampleRate; i++){
            double angel1 = (double)i*(double) 2000*Math.PI/(double)sampleRate;
            double angel2 = 0;
            double ans = (Math.sin(angel1)+Math.sin(angel2));
            data[i]= getByteValue(ans);
        }

//        int idx = 0;
//        for (final double dVal : ans) {
//            final short val = (short) ((dVal * 100f));
//            // in 16 bit wav PCM, first byte is the low order byte
//            data[idx++] = (byte) (val & 0x00ff);
//            data[idx++] = (byte) ((val & 0xff00) >>> 8);
//        }



        File fileOut = new File(audioPath[0]);
        byte audioData[] = data;
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
