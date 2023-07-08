package com.watcher.ffplayer.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListTest {
    public static void main(String[] args) {
        List a = new ArrayList<Integer>();
        List b = new ArrayList<Integer>(a);
        b.add(1);
        for(Object a0:a)
        {
            System.out.println("args = "+a0);
        }
    }
}
