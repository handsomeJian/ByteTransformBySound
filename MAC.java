 
import java.io.*;
import java.util.Date;

import static java.lang.Math.min;

public class MAC extends Thread {

    static final int RECEIVER = 0;
    static final int SENDER = 1;
    static final int PING = 2;

    static final int FRADETE = 0;
    static final int RX = 1;
    static final int TX = 2;
    static final int BUFFER_CAN_WRITE = 0;
    static final int NOT_HAVE_FRAME = 0;
    static final int MEET_FRAME = 1;
    static final int allBit = 50000;
    static final int FRAMSIZE = 500;

    volatile static int state;
    volatile static int bufferState;
    volatile static int frameDetect;

    volatile static int[] RxBuffer;
    boolean shouldSendACK = false;

    int role;
    String inputFileString;
    String outputFileString;

    long lastSendTime = -1;
    long timeOutSeconds = 4000;

    int bufferSize = 11000;
    int[] MACBuffer;
    int MACBufferLength;

    Task3Mod sender;
    DataProcess inDataProcess;
    DataProcess outDataProcess;

    static int ACKDataLength = 8;
    int[] ACKRightData;
    int[] ACKFaultData;

    public MAC(String inFileString, String outFileString) {
        inputFileString = inFileString;
        outputFileString = outFileString;
        RxBuffer = new int[bufferSize];
        MACBuffer = new int[bufferSize];
    }

    public MAC(int initState, String tmpFileString, String tmpSrcIP, String tmpDstIP) {
        role = initState;
        if (role == SENDER) {
            state = TX;
            inputFileString = tmpFileString;
            inDataProcess = new DataProcess(inputFileString, 400, 1);  // !!!!!!
        } else if (role == RECEIVER) {
            state = FRADETE;
            outputFileString = tmpFileString;
            outDataProcess = new DataProcess(outputFileString, 400, 0);
        } else if (role == PING) {
            state = TX;
            outputFileString = tmpFileString;
            outDataProcess = new DataProcess(outputFileString, 400, 0);
        }
        srcIP = tmpSrcIP;
        dstIP = tmpDstIP;
        RxBuffer = new int[bufferSize];
        MACBuffer = new int[bufferSize];
    }

    private void makeACKData() {
        ACKRightData = new int[ACKDataLength];
        ACKFaultData = new int[ACKDataLength];
        for (int i = 0; i < ACKDataLength; ++i) {
            ACKRightData[i] = i % 2;
            ACKFaultData[i] = (i + 1) % 2;
        }
    }

    private void init() {
        //inDataProcess = new DataProcess(inputFileString, 1000, 50000);
        //MAC.state = FRADETE;
        //outDataProcess = new DataProcess(outputFileString, 0, 50000);
        MAC.bufferState = BUFFER_CAN_WRITE;
        MAC.frameDetect = NOT_HAVE_FRAME;

        Task3Mod mod = new Task3Mod();
        Thread Rx = new PHYRx(mod);
        Rx.start();

        sender = new Task3Mod();

        makeACKData();

        tmpSendBuffer = new int[bufferSize];
    }

    public void readBuffer() {
        MACBufferLength = bufferState;
        for (int i = 0; i < MACBufferLength; ++i) {
            MACBuffer[i] = RxBuffer[i];
        }
    }

    static final int NOTACK = 0;
    static final int ACKRIGHT = 1;
    static final int ACKFAULT = 2;
    public int readDataIsACK() {
        
        if (MACBufferLength != ACKDataLength)
            return NOTACK;
        boolean tmpState = true;
        for (int i = 0; i < MACBufferLength; ++i)
            if (MACBuffer[i] != ACKRightData[i])
                tmpState = false;
        if (tmpState) {
            return ACKRIGHT;
        }
        tmpState = true;
        for (int i = 0; i < MACBufferLength; ++i)
            if (MACBuffer[i] != ACKFaultData[i])
                tmpState = false;
        if (tmpState) {
            return ACKFAULT;
        }
        return NOTACK;
    }

    public boolean saveRecivedData() {
        int srcIPlength = 0;
        int dstIPlength = 0;
        char tmpChar = 0;
        for (int i = MACBufferLength - 1; i >= MACBufferLength - 8; --i)
            dstIPlength = (dstIPlength << 1) + MACBuffer[i];
        MACBufferLength -= 8;
        for (int i = MACBufferLength - 1; i >= MACBufferLength - 8; --i)
            srcIPlength = (srcIPlength << 1) + MACBuffer[i];
        MACBufferLength -= 8;
        MACBufferLength -= srcIPlength * 8 + dstIPlength * 8;
        boolean returnValue = outDataProcess.saveData(MACBuffer, MACBufferLength);

        if (!returnValue)
            return returnValue;

        FileOutputStream out;
        PrintStream printStream;
        try {
            out = new FileOutputStream("./src/recievedIP.txt");
            printStream = new PrintStream(out);
        } catch (Exception e) {
            System.out.println("HERE122");
            return returnValue;
        }

        for (int i = 0; i < srcIPlength; ++i) {
            tmpChar = 0;
            for (int j = 0; j < 8; ++j) {
                tmpChar += (MACBuffer[MACBufferLength + j] << j);
            }
            printStream.print(tmpChar);
            MACBufferLength += 8;
        }
        printStream.println();

        for (int i = 0; i < dstIPlength; ++i) {
            tmpChar = 0;
            for (int j = 0; j < 8; ++j) {
                tmpChar += (MACBuffer[MACBufferLength + j] << j);
            }
            printStream.print(tmpChar);
            MACBufferLength += 8;
        }
        printStream.println();


        printStream.close();
        return returnValue;
    }

    int ACKState = ACKRIGHT;
    public void sendACKData() {
        if (ACKState == ACKRIGHT)
            sender.mySender(ACKRightData, ACKDataLength);
        else if (ACKState == ACKFAULT)
            sender.mySender(ACKFaultData, ACKDataLength);
    }

    String srcIP;
    String dstIP;
    void addIP() {
        int tmpNum = 0;
        for (int i = 0; i < srcIP.length(); ++i) {
            tmpNum = srcIP.charAt(i);
            for (int j = 0; j < 8; ++j) {
                tmpSendBuffer[tmpSendFrameSize + i * 8 + j] = tmpNum & 1;
                tmpNum >>= 1;
            }
        }
        tmpSendFrameSize += srcIP.length() * 8;
        for (int i = 0; i < dstIP.length(); ++i) {
            tmpNum = dstIP.charAt(i);
            for (int j = 0; j < 8; ++j) {
                tmpSendBuffer[tmpSendFrameSize + i * 8 + j] = tmpNum & 1;
                tmpNum >>= 1;
            }
        }
        tmpSendFrameSize += dstIP.length() * 8;
        tmpNum = srcIP.length();
        for (int j = 0; j < 8; ++j) {
            tmpSendBuffer[tmpSendFrameSize + j] = tmpNum & 1;
            tmpNum >>= 1;
        }
        tmpSendFrameSize += 8;
        tmpNum = dstIP.length();
        for (int j = 0; j < 8; ++j) {
            tmpSendBuffer[tmpSendFrameSize + j] = tmpNum & 1;
            tmpNum >>= 1;
        }
        tmpSendFrameSize += 8;
    }

    int[] tmpSendBuffer;
    int tmpSendFrameSize;
    boolean sendPre = false;
    public boolean sendFileData() {
        System.out.println(sendPre);
        if (!sendPre)
            tmpSendFrameSize = inDataProcess.getNewFrame(tmpSendBuffer);
        System.out.println(tmpSendFrameSize);
        if (tmpSendFrameSize == 0)
            return false;
        if (!sendPre)
            addIP();
        System.out.println(tmpSendFrameSize);
        sender.mySender(tmpSendBuffer, tmpSendFrameSize);
        sendPre = false;
        return true;
    }
    public boolean sendPingData() {
        tmpSendFrameSize = 8;
        for (int i = 0; i < 8; ++i)
            tmpSendBuffer[i] = i % 2;
        addIP();
        sender.mySender(tmpSendBuffer, tmpSendFrameSize);
        return true;
    }

    long lastPingTime = 0;

    public void run(){
        init();
        //Date runStartDate = new Date();
        int saveFull = 0;
        int sendFull = 0;
        boolean haveSendACK = false;
        while (true) {
            
            if (state == FRADETE) {

                if (frameDetect == MEET_FRAME) {
                    state = RX;
                    frameDetect = NOT_HAVE_FRAME;
                    continue;
                }
                if (role == MAC.PING) {
                    if (saveFull == 1) {
                        Date nowDate = new Date();
                        long nowTime = nowDate.getTime();
                        System.out.print("Ping Latency = ");
                        System.out.println(nowTime - lastPingTime);
                        break;
                    }
                    if (lastPingTime != 0)
                        continue;
                }
                if (role == MAC.RECEIVER) {
                    if (saveFull == 1)
                        break;
                    continue;
                }
                if (sendFull == 1) {
                    break;
                }
                if (!haveSendACK || sendFull > 0) {
                    continue;
                }
                
                if (lastSendTime == -1) {
                    state = TX;
                    continue;
                } else if (lastSendTime != -1) {
                    Date nowDate = new Date();
                    long nowTime = nowDate.getTime();
                    if (nowTime - lastSendTime >= timeOutSeconds) {
                        System.out.println("link error");
                        break;
                    }
                }
            }
            

            if (state == RX) {

                //System.out.println("RX");

                while (bufferState == BUFFER_CAN_WRITE) {
                    continue;
                }

                System.out.println("DONE");
                readBuffer();
                bufferState = BUFFER_CAN_WRITE;

                int tmpACKState = readDataIsACK();
                if (tmpACKState == NOTACK) {
                    System.out.println("RX");
                    shouldSendACK = true;
                    if (MACBufferLength == -1) {
                        ACKState = ACKFAULT;
                    } else {
                        ACKState = ACKRIGHT;
                        if (saveRecivedData())
                            saveFull++;
                    }
                    state = TX;
                } else {
                    System.out.println("ACKRX");
                    if (tmpACKState == ACKFAULT) {
                        sendPre = true;
                    }


                    //Date nowDate = new Date();
                    //long nowTime = nowDate.getTime();
                    //System.out.print("Throughput = ");
                    //System.out.print(10000.0/(nowTime - lastSendTime));
                    //System.out.println("kbps/s");

                    lastSendTime = -1;
                    if (role == MAC.SENDER) state = TX;
                    else state = FRADETE;
                }
                continue;
            }

            if (state == TX) {

                //System.out.println("TX");

                if (shouldSendACK) {
                    System.out.println("ACKTX");
                    sendACKData();
                    shouldSendACK = false;
                    ACKState = ACKRIGHT;
                    if ((saveFull > 0) && (sendFull > 0)) {
                        Task3Mod.receiveContinue = false;
                        break;
                    }
                    haveSendACK = true;
                    state = FRADETE;
                }
                else {
                    Date nowDate = new Date();
                    lastSendTime = nowDate.getTime();
                    System.out.println("TX");
                    if (role == PING) {
                        if (lastPingTime != 0) {
                            state = FRADETE;
                            continue;
                        }
                        lastPingTime = lastSendTime;
                        sendPingData();
                        state = FRADETE;
                        continue;
                    }
                    if (!sendFileData()) {
                        sendFull++;
                        //Task3Mod.receiveContinue = false;
                        if (saveFull > 0) {
                            Task3Mod.receiveContinue = false;
                            break;
                        }
                    }
                    state = FRADETE;
                }
                continue;
            }
        }
        //Date runFinishDate = new Date();
        //System.out.println(runFinishDate.getTime() - runStartDate.getTime());
    }

}


class PHYRx extends Thread {

    Task3Mod mod;

    public PHYRx(Task3Mod mod) {
        this.mod = mod;
    }
    public void run() {
        mod.myReceiver();
    }
}