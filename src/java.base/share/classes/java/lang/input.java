package java.lang;

import java.util.Scanner;

public class input {
    // Create a single Scanner instance to use for all inputs
    private static final Scanner scanner = new Scanner(System.in);

    // Method to input an integer
    public static int Int() {
        return scanner.nextInt();
    }

    // Method to input a double
    public static double Double() {
        return scanner.nextDouble();
    }

    // Method to input a float
    public static float Float() {
        return scanner.nextFloat();
    }

    // Method to input a long
    public static long Long() {
        return scanner.nextLong();
    }

    // Method to input a string (a single token, like one word)
    public static String String() {
        return scanner.next();
    }

    // Method to input a full line of text
    public static String Line() {
        return scanner.nextLine();
    }

    // Method to input a boolean
    public static boolean Boolean() {
        return scanner.nextBoolean();
    }

    // Method to close the scanner (though it's not typically needed in this utility class)
    public static void close() {
        scanner.close();
    }
          }
