package com.zf;

import java.util.Arrays;

public class SortExam
{

  public static void main(String[] args)
  {
    SortExam s = new SortExam();
    int length = 50;
    int[] arr = ArrayGen.intGen(length);
    int[] arr1 = new int[length];
    System.arraycopy(arr, 0, arr1, 0, length);
    int[] count = insertSort(arr);
    int[] count1 = bubbleSort(arr1);

    System.out.println(Arrays.toString(arr));
    System.out.println(Arrays.toString(count));
    System.out.println(Arrays.toString(arr1));
    System.out.println(Arrays.toString(count1));
  }

  public static int[] insertSort(int[] arr)
  {
    int swap = 0;
    int compare = 0;
    int temp = 0;
    for (int index = 1; index < arr.length; index++)
    {
      for (int i = index - 1; i > -1; i--)
      {
        compare++;
        if (arr[index] < arr[i])
        {
          swap++;
          //swap
          temp = arr[i];
          arr[i] = arr[index];
          arr[index] = temp;
          index = i;

        }
        else
        {
          break;
        }

      }
    }
    return new int[] { compare, swap };
  }

  public static int[] bubbleSort(int[] arr)
  {
    int swap = 0;
    int compare = 0;
    int temp = 0;
    for (int i = 1; i < arr.length - 1; i++)
    {
      for (int j = 0; j < arr.length - i; j++)
      {
        compare++;
        if (arr[j] > arr[j + 1])
        {
          swap++;
          temp = arr[j];
          arr[j] = arr[j + 1];
          arr[j + 1] = temp;
        }
      }

    }
    return new int[] { compare, swap };
  }

}
