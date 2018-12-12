 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Queue;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Reader;
import java.io.IOException;

import javax.sound.sampled.TargetDataLine;

import static java.lang.System.exit;

class Task3Mod {

    private static int samplePerBit = 10;
    private static int sampleRate = 44100;

    private static double sampleTime = 1.0 / (double)sampleRate;
    private static int tF = 10000;
    
    private static byte[] frameHead;
    private static int frameHeadLong = 100;
    private static int zeroBitNumber = 50;
    private static int zeroLimit = 6;

    private static int randomStartOff = 1000;

    private static int highAmp = 120;
    private static int lowAmp = 30;
    private double rHighAmp = 0;
    private double rLowAmp = 0;
    private static int ampSampleNum = samplePerBit * 2;
    private static int numberSample = 15;

    private static int frameLimit = 200000;

    private static int frameLengthLimit = 11000;

    private byte getByteValue(double angle, float maxVol) {
        return Integer.valueOf((int)Math.round(Math.sin(angle)*maxVol)).byteValue(); // Watch out!!!!!!
    }

    volatile static boolean receiveContinue = true;

    Task3Mod() {
        frameHead = new byte[frameHeadLong];
        for (int i = 0; i < frameHeadLong; ++i) {
            double angel = 2 * Math.PI * i * sampleTime * tF;
            //if ((i / 20) % 2 == 0) angel *= lF;
            //else angel *= hF;
            frameHead[i]= getByteValue(angel, 128.0f);
        }
    }

    private byte getByte(int value, int nowT) {
        int tmpM = 1;
        if (nowT >= samplePerBit / 2) tmpM = -1;
        if (value == 0) return Integer.valueOf(tmpM * lowAmp).byteValue(); //getByteValue(2 * Math.PI * nowT * ampF * sampleTime, lowAmp);
        else return Integer.valueOf(tmpM * highAmp).byteValue(); //getByteValue(2 * Math.PI * nowT * ampF * sampleTime, highAmp);
    }

    public void mySender(int[] data, int allBit) {
        /*
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        FileOutputStream out;
        PrintStream printStream;
        try {
            out =  new FileOutputStream("./src/intputTest.txt");
            printStream = new PrintStream(out);
        } catch (Exception e) {
            System.out.println("HERE122");
            return;
        }
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        */

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,  (float)sampleRate,
            8, 1, 1, (float)sampleRate, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

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

        int numBytes = (randomStartOff + frameHeadLong + zeroBitNumber + ampSampleNum*2 + (allBit + 3*numberSample) * samplePerBit) * 2;
        byte[] audioBytes = new byte[numBytes];
        numBytes = 0;

        System.out.println("Play Start");

        try {

                // Add some random or zero signal at the beginning to make a rest between frame.
                for (int j = 0; j < randomStartOff; ++j)
                    audioBytes[j] = 0;

                // Add the frame head
                for (int j = 0; j < frameHeadLong; ++j)
                    audioBytes[randomStartOff + j] = frameHead[j];
                numBytes = frameHeadLong + randomStartOff;
                
                // Add zero bits to fix the start of the frame
                for (int j = 0; j < zeroBitNumber; ++j)
                    audioBytes[numBytes + j] = 0;
                numBytes += zeroBitNumber;

                // Add highAmp sample and lowAmp sample
                for (int j = 0; j < ampSampleNum; ++j)
                    audioBytes[numBytes + j] = getByte(1, j % samplePerBit);
                numBytes += ampSampleNum;
                for (int j = 0; j < ampSampleNum; ++j)
                    audioBytes[numBytes + j] = getByte(0, j % samplePerBit);
                numBytes += ampSampleNum;
                
                // Add bit number of the frame
                int tmpAllBit = allBit;
                for (int j = 0; j < numberSample; ++j) {
                    int nowValue = tmpAllBit & 1;
                    tmpAllBit = tmpAllBit >> 1;
                    for (int k = 0; k < samplePerBit; ++k) {
                        audioBytes[numBytes + k] = getByte(nowValue, k);
                    }
                    numBytes += samplePerBit;
                }

                // Add data into the frame
                int zeroCount = 0;
                int oneCount = 0;
                for (int j = 0; j < allBit; ++j) {
                    int nowValue = data[j];
                    if (nowValue == 0) {
                        zeroCount += 1;
                    } else {
                        oneCount += 1;
                    }
                    for (int k = 0; k < samplePerBit; ++k) {
                        audioBytes[numBytes + k] = getByte(nowValue, k);
                    }
                    numBytes += samplePerBit;
                }

                // Add number of zero and one at the end of the frame
                tmpAllBit = oneCount;
                for (int j = 0; j < numberSample; ++j) {
                    int nowValue = tmpAllBit & 1;
                    tmpAllBit = tmpAllBit >> 1;
                    for (int k = 0; k < samplePerBit; ++k) {
                        audioBytes[numBytes + k] = getByte(nowValue, k);
                    }
                    numBytes += samplePerBit;
                }
                tmpAllBit = zeroCount;
                for (int j = 0; j < numberSample; ++j) {
                    int nowValue = tmpAllBit & 1;
                    tmpAllBit = tmpAllBit >> 1;
                    for (int k = 0; k < samplePerBit; ++k) {
                        audioBytes[numBytes + k] = getByte(nowValue, k);
                    }
                    numBytes += samplePerBit;
                }
                
                /*
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                for (int j = 0; j < numBytes; ++j) {
                    printStream.println(audioBytes[j]);
                }
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                */
                
                sourceDataLine.write(audioBytes, 0, numBytes);
                sourceDataLine.drain();
                sourceDataLine.close();
                

        } catch (Exception e) {
            System.out.println("HERE1");
        }

        System.out.println("Play Over");

        //printStream.close();
    }

    private int nowHeadPlace;
    private int nowFrameTmpSize;
    private byte[] frameHeadTmp;

    private int getRecrodByte(TargetDataLine line) {
        if (nowHeadPlace == nowFrameTmpSize) {
            nowHeadPlace = 0;
            nowFrameTmpSize = line.read(frameHeadTmp, 0, frameHeadTmp.length);
        }
        nowHeadPlace ++;
        //System.out.println(Byte.valueOf(frameHeadTmp[nowHeadPlace - 1]).intValue());
        return (int)frameHeadTmp[nowHeadPlace - 1];
    }

    private long getFrameHeadMult(Queue<Integer> queue) {
        long tmp = 0;
        int nowPlace = 0;
        for (Integer q : queue) {
            tmp += q.longValue() * (int)frameHead[nowPlace];
            nowPlace ++;
        }
        return tmp;
    }

    private int decodeNum(int[] data) {
        double tmp = 0;
        for (int i = 0; i < samplePerBit; ++i)
            tmp += Math.abs(data[i]);
        tmp /= samplePerBit;
        //System.out.print(tmp);
        if (Math.abs(tmp - rLowAmp) < Math.abs(tmp - rHighAmp)) return 0;
        else return 1;
    }

    public void myReceiver() {
        
        //FileOutputStream out;
        //PrintStream printStream;
        /*
// !!!!!!!!!!!!!!!!!!!!!!!
        FileOutputStream out2;
        PrintStream printStream2;
// !!!!!!!!!!!!!!!!!!!!!!!!

        try {
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            out2 =  new FileOutputStream("./src/outwave.txt");
            printStream2 = new PrintStream(out2);
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        } catch (Exception e) {
            System.out.println("HERE3");
            return;
        }*/
         

        AudioFormat Rformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,  (float)sampleRate,
            8, 1, 1, (float)sampleRate, false);
        TargetDataLine line = null;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, Rformat); // format is an AudioFormat object
        System.out.print("Record Start\n");
        if (!AudioSystem.isLineSupported(info)) {
            // Handle the error ...
            System.out.print("out");
            exit(0);
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(Rformat);
        } catch (LineUnavailableException ex) {
            // Handle the error ...
            System.out.print("Bad Audio");
        }
        line.start();

        nowFrameTmpSize = 0;
        nowHeadPlace = 0;
        frameHeadTmp = new byte[line.getBufferSize() / 5];
        

        /*Queue<Integer> headQueue = new LinkedList<Integer>();
        for (int i = 0; i < frameHeadLong; ++i)
            headQueue.offer(Integer.valueOf(getRecrodByte(line)));
        for (int i = 0; i < allBit * samplePerBit * 2; ++i) {
            printStream.println(getFrameHeadMult(headQueue));
            int tmp = getRecrodByte(line);
            printStream2.println(tmp);
            headQueue.poll();
            headQueue.offer(Integer.valueOf(tmp));
        }*/

        
        int[] sampleBitTmp = new int[samplePerBit];
        while (receiveContinue) {

            // Get the rough location of head frame
            Queue<Integer> headQueue = new LinkedList<Integer>();
            for (int i = 0; i < frameHeadLong; ++i)
                headQueue.offer(getRecrodByte(line));
            while (getFrameHeadMult(headQueue) < frameLimit) {
                headQueue.poll();
                headQueue.offer(Integer.valueOf(getRecrodByte(line)));
            }

            MAC.frameDetect = MAC.MEET_FRAME;
            
            // Using zeros to make the accurate place of start
            //System.out.print("frame = ");
            //System.out.println(frameNum);
            //int zeroCount = 0;
            int tmp233;
            for (int j = 0; j < zeroBitNumber; ++j) {
                tmp233 = getRecrodByte(line);
                //printStream2.println(tmp233);
                //System.out.print(tmp233);
                //System.out.print(" ");
            }
            tmp233 = getRecrodByte(line);
            while (Math.abs(tmp233) < zeroLimit) {
                //printStream2.println(tmp233);
                //zeroCount ++;
                tmp233 = getRecrodByte(line);
            }
            //System.out.println();
            //System.out.println(zeroCount);
            nowHeadPlace --;

            // Get the value of received highAmp and lowAmp
            rHighAmp = 0;
            rLowAmp = 0;
            for (int i = 0; i < ampSampleNum; ++i)
                rHighAmp += Math.abs(getRecrodByte(line));
            rHighAmp /= ampSampleNum;
            for (int i = 0; i < ampSampleNum; ++i)
                rLowAmp += Math.abs(getRecrodByte(line));
            rLowAmp /= ampSampleNum;

            //System.out.print("rHighAmp = ");
            //System.out.println(rHighAmp);
            //System.out.print("rLowAmp = ");
            //System.out.println(rLowAmp);


            // Get the number of bit in the frame
            int bitNumber = 0;
            for (int i = 0; i < numberSample; ++i) {
                for (int j = 0; j < samplePerBit; ++j) {
                    sampleBitTmp[j] = getRecrodByte(line);
                    //printStream2.println(sampleBitTmp[j]);  // !!!!!!!!!!!!!!!!
                }
                bitNumber = bitNumber + (decodeNum(sampleBitTmp) << i);
            }
            
            while (MAC.bufferState != MAC.BUFFER_CAN_WRITE) {
                continue;
            }

            if (bitNumber >= frameLengthLimit) {
                MAC.bufferState = -1;
                continue;
            }
            
            // Decode the data in the frame
            int oneCount = 0;
            int zeroCount = 0;
            for (int i = 0; i < bitNumber; ++i) {
                for (int j = 0; j < samplePerBit; ++j) {
                    sampleBitTmp[j] = getRecrodByte(line);
                    //printStream2.println(sampleBitTmp[j]);  // !!!!!!!!!!!!!!!!
                }
                MAC.RxBuffer[i] = decodeNum(sampleBitTmp);
                if (MAC.RxBuffer[i] == 0) {
                    zeroCount += 1;
                } else {
                    oneCount += 1;
                }
            }
            
            // Check the number of ones and zeros
            int oneNubmer = 0;
            int zeroNumber = 0;
            for (int i = 0; i < numberSample; ++i) {
                for (int j = 0; j < samplePerBit; ++j) {
                    sampleBitTmp[j] = getRecrodByte(line);
                    //printStream2.println(sampleBitTmp[j]);  // !!!!!!!!!!!!!!!!
                }
                oneNubmer = oneNubmer + (decodeNum(sampleBitTmp) << i);
            }
            for (int i = 0; i < numberSample; ++i) {
                for (int j = 0; j < samplePerBit; ++j) {
                    sampleBitTmp[j] = getRecrodByte(line);
                    //printStream2.println(sampleBitTmp[j]);  // !!!!!!!!!!!!!!!!
                }
                zeroNumber = zeroNumber + (decodeNum(sampleBitTmp) << i);
            }

            if (oneCount != oneNubmer || zeroCount != zeroNumber) {
                MAC.bufferState = -1;
            } else {
                MAC.bufferState = bitNumber;
            }
        }

        System.out.println("Record Over");

        line.stop();
        line.close();
        /*try {
            // !!!!!!!!!!!!!!!!!
            printStream2.close();
            out2.close();
            // !!!!!!!!!!!!!!!!!!!!
        } catch (Exception e) {
            System.out.println("HERE4");
        }*/
    }
}