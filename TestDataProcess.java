 

public class TestDataProcess {

    public static void test(){
        DataProcess dataProcess = new DataProcess();
        dataProcess.totData = new int[10];
        for (int i=0;i<10;i++) dataProcess.totData[i]=i;
        dataProcess.inBytePerFrame = 3 ;
        dataProcess.totLength = 10;
        int[] buffer = new int[3];
        int length = dataProcess.getNewFrame(buffer);
        System.out.println(length);
        for (int i=0;i<length;i++) {System.out.print(buffer[i]);System.out.print(" ");}
        System.out.println();
        length = dataProcess.getNewFrame(buffer);
        System.out.println(length);
        for (int i=0;i<length;i++) {System.out.print(buffer[i]);System.out.print(" ");}
        System.out.println();
        length = dataProcess.getNewFrame(buffer);
        System.out.println(length);
        for (int i=0;i<length;i++) {System.out.print(buffer[i]);System.out.print(" ");}
        System.out.println();
        length = dataProcess.getNewFrame(buffer);
        System.out.println(length);
        for (int i=0;i<length;i++) {System.out.print(buffer[i]);System.out.print(" ");}
        System.out.println();
    }


}
