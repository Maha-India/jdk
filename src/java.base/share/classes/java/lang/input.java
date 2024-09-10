package java.lang;

import java.util.Scanner;

public class input {
    // Create a single Scanner instance to use for all inputs
    private static final Scanner scanner = new Scanner(System.in);

    // Method to input an integer
    public static int intgr() {
        return scanner.nextInt();
    }

    // Method to input a double
    public static double bigfrac() {
        return scanner.nextDouble();
    }

    // Method to input a float
    public static float frac() {
        return scanner.nextFloat();
    }

    // Method to input a long
    public static long lng() {
        return scanner.nextLong();
    }

    // Method to input a string (a single token, like one word)
    public static String word) {
        return scanner.next();
    }

    // Method to input a full line of text
    public static String line() {
        return scanner.nextLine();
    }

    // Method to input a boolean
    public static boolean bool() {
        return scanner.nextBoolean();
    }

    //method to input a letter
    public static char ltr() {
        return scanner.next().charAt(0);
    }
    // Method to close the scanner (though it's not typically needed in this utility class)
    public static void close() {
        scanner.close();
    }
          }
