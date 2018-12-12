import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

class PlayTimeCounter extends Thread {

    private long beginDate;
    private long totTime;
    PlayTimeCounter(long totTime){
        this.totTime = totTime;
        MyPlayAudio.playEnough = false;
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
        MyPlayAudio.playEnough = true;
    }
}


public class MyPlayAudio {
    public static boolean playEnough = false;
    public static void playAudio(String[] audioPath,long totTime) {
        AudioFormat format = null;
        //--------------read audio------------------------------
        File file = new File(audioPath[0]);
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
            format = audioInputStream.getFormat();
            System.out.println("Channels: "+format.getChannels());
            System.out.println("Encoding: "+format.getEncoding());
            System.out.println("FrameRate: "+format.getFrameRate());
            System.out.println("FrameSize: "+format.getFrameSize());
            System.out.println("SampleRate: "+format.getSampleRate());
            System.out.println("SampleSizeInBits: "+format.getSampleSizeInBits());
            System.out.println("BigEndian: "+format.isBigEndian());
            System.out.println("SampleRate: "+format.getSampleRate());
            System.out.println("SampleRate: "+format.getSampleRate());

            System.out.println("此音频的格式为：" + audioInputStream.getFormat()
                    + "\n此音频的流长度为：" + audioInputStream.getFrameLength()
                    + "帧\n此文件共有 " + file.length() + " 字节");
        } catch (UnsupportedAudioFileException e) {
            System.out.print(e.getMessage() + " UnsupportedAudioFileException\n");
        } catch (IOException e) {
            System.out.print(e.getMessage() + '\n');
        }
        //--------------read audio------------------------------


        //-------------------check bytesFrame------------------------------
        int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
        if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
            // some audio formats may have unspecified frame size
            // in that case we may read any amount of bytes
            bytesPerFrame = 1;
        }
        //-------------------check bytesFrame------------------------------


        //-------------------check encoding------------------------------
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(), 16, format.getChannels(),
                    format.getChannels() * 2, format.getSampleRate(),
                    false);
            audioInputStream = AudioSystem.getAudioInputStream(format,
                    audioInputStream);
        }
        //-------------------check encoding------------------------------


        DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                format);
        SourceDataLine sourceDataLine = null;
        try {
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        if (sourceDataLine != null) {
            sourceDataLine.start();  // 允许某一数据行执行数据I/O
        }


        //-------------------define audio buffer------------------------------
        int numBytes = 1024 * bytesPerFrame;
        byte[] audioBytes = new byte[numBytes];
        //-------------------define audio buffer------------------------------

        int numBytesRead = 0;
        int numFramesRead = 0;
        int totalFramesRead = 0;

        //--------------------开启计时器----------------------
        PlayTimeCounter timeCounter = new PlayTimeCounter(totTime);
        Thread timeCounterThread = new Thread(timeCounter);
        timeCounterThread.start();
        //--------------------开启计时器----------------------

        try {
            while ((numBytesRead =
                    audioInputStream.read(audioBytes)) != -1 && (!playEnough)) {
                // Calculate the number of frames actually read.
                numFramesRead = numBytesRead / bytesPerFrame;
                totalFramesRead += numFramesRead;
                // Here, do something useful with the audio data that's
                // now in the audioBytes array...
                sourceDataLine.write(audioBytes, 0, numBytesRead);
            }
            sourceDataLine.drain();
            sourceDataLine.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            exit(0);
        }
    }

}
