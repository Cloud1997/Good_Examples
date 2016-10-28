package com.zf;

import java.util.Arrays;

public class ArrayGen
{
  public static void main(String a[])
  {
    //    System.out.println(Runtime.getRuntime().totalMemory());
    //    intGen(100);
    System.out.println(Long.MAX_VALUE);
  }

  public static int[] intGen(int length)
  {
    //    long start = System.currentTimeMillis();
    int[] temp = new int[length];
    for (int i = 0; i < length; i++)
    {
      temp[i] = i;
    }
    for (int i = 0; i < length; i++)
    {

      int swap = (int)(Math.random() * length);

      int middle = temp[i];
      temp[i] = temp[swap];
      temp[swap] = middle;
    }
    //    System.out.println(Runtime.getRuntime().totalMemory());
    //    System.out.println(System.currentTimeMillis() - start);
    System.out.println(Arrays.toString(temp));

    return temp;
  }

}
