package com.watcher.ffplayer.test;


public class lenTest {
    static int byteToInt(int []a)
    {
        int res = 0;
        for (int i=0; i<4; i++) {
            res <<= 8;
            //这个运算和野生c的运算是不同的，会在符号位和数据间填0
            res |= a[i];
        }
        return res;
    }
    public static void main(String[] args) {
        int [] lenRecv = {-1,-1,-1,-1};
        int len = byteToInt(lenRecv);
        System.out.printf("len="+len);
    }
}
