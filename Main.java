 
import javax.sound.sampled.*;
import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

import static java.lang.System.exit;

public class Main {


    private static void printAudioInfo(){
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info: mixerInfos){
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = m.getSourceLineInfo();
            for (Line.Info lineInfo:lineInfos){
                System.out.println (info.getName()+"---"+lineInfo);
                Line line = null;
                try {
                    line = m.getLine(lineInfo);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
                System.out.println("\t-----"+line);
            }
            lineInfos = m.getTargetLineInfo();
            for (Line.Info lineInfo:lineInfos){
                System.out.println (m+"---"+lineInfo);
                Line line = null;
                try {
                    line = m.getLine(lineInfo);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
                System.out.println("\t-----"+line);

            }

        }
    }

    public static void main(String[] args) {
    // write your code here

/*    
        String[] recordPath = new String[1];
        recordPath[0] = "./src/sintest.wav";

        String[] playPath = new String[1];
        playPath[0] = "./src/sin.wav";

        Task1.task1(recordPath,playPath,5000);

        MyPlayAudio.playAudio(recordPath,100000);
*/
    
/*
        String[] sinPath = new String[1];
        sinPath[0] = "./src/sin.wav";

        MySinSound.generateSin(1000,sinPath);

        MyPlayAudio.playAudio(sinPath,100000);
*/

        String inputPath = new String("./src/input.txt");
        //String outputPath = new String("./src/output.txt");
        //Task3.task3(inputPath, outputPath);

        String dstIP = "192.168.1.1";
        String srcIP = "192.168.1.2";

        if (args.length == 1) {
            dstIP = args[0];
        }
        if (args.length == 2) {
            srcIP = args[1];
        }

        MAC sender = new MAC(MAC.SENDER, inputPath, srcIP, dstIP);

        sender.run();

        //MAC reciever = new MAC(MAC.RECEIVER, inputPath, srcIP, dstIP);

        //reciever.run();


        exit(0);

//        Task3.task3(inputPath,outputPath);
//        String inputPath1 = new String("./src/test.bin");
//        TestSaveDataToFile.test(inputPath1);
//        MacPerf.test();
    }
}



