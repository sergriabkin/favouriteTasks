package com.javarush.task.romanToInteger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class RomanToIntegerConvertation {

    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Input a roman number: ");
        String romanString = bufferedReader.readLine();
        System.out.println("Conversion result equals: " + romanToInteger(romanString));
        bufferedReader.close();
    }
// main idea: to go from end to begin and do "+" if more/equals or do "-" if it is less then previous

    public static int romanToInteger(String s) {
        int perviousNum = 0;
        int currentNum = 0;
        int result = 0;
        HashMap<Character, Integer> map = new HashMap<>();
        map.put( 'I' , 1 );
        map.put( 'V' , 5 );
        map.put( 'X' , 10 );
        map.put( 'L' , 50 );
        map.put( 'C' , 100 );
        map.put( 'D' , 500 );
        map.put( 'M' , 1000 );

        char[] str = s.toUpperCase().toCharArray();
        for (int i = str.length-1; i >=0 ; i--) {
            if (!map.containsKey(str[i])) {
                System.out.println("Invalid character at " + (i+1) +" position");
                return 0;
            }
            currentNum = map.get(str[i]);
            if (currentNum >= perviousNum)  result += currentNum;
            else result -= currentNum;

            perviousNum = currentNum;
        }
        return result;
    }

}

/*

{I, V, X, L, C, D, M} to decimal

example:
input - MCMLXXVI
output - 1976

 */
