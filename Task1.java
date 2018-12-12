public class Task1 {

    public static void task1(String[] recordPath,String[] playPath, long time) {

        //--------------------开启播音----------------------
        //task1Play timeCounter = new task1Play(playPath,time);
        //Thread timeCounterThread = new Thread(timeCounter);
        Thread timeCounterThread = new task1Play(playPath,time);
        timeCounterThread.start();
        //--------------------开启播音----------------------

        MyRecordAudio.recordAudio(recordPath, time); //开启录音

    }


}


class task1Play extends Thread {

    private long totTime;
    private String[] path;
    public task1Play(String[] path,long totTime) {
        this.totTime = totTime;
        this.path = path;
    }
    public void run() {
        MyPlayAudio.playAudio(path,totTime);
    }
}