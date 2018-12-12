 

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.Vector;

public class ReadBinFile {
    static String[] s = new String[105];

    private static boolean check(int x, int y){
        if (s[x].length()>s[y].length()) return false;
        char[] t1 = s[x].toCharArray();
        char[] t2 = s[y].toCharArray();
        for (int i=0;i<s[x].length();i++)
            if (t1[i]!=t2[i]) return false;
        return true;
    }


    public static void test() {

        Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
        int t = in.nextInt();  // Scanner has functions to read ints, longs, strings, chars, etc.
        for (int io = 1; io <= t; ++io) {
            int n = in.nextInt();
            int p = in.nextInt();
            for (int i=1;i<=p;i++) {
                s[i] = in.next();
            }
            int[][] mp = new int[105][105];
            for (int i=1;i<=p;i++)
                for (int j=1;j<=p;j++) mp[i][j]=0;
            for (int i=1;i<=p;i++)
                for (int j=1;j<=p;j++)
                    if (check(i,j)) mp[i][j]=1;

        }
    }

}
