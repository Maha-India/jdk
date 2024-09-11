package java.lang;

import java.util.Scanner;

public class input {
    // Create a single Scanner instance to use for all inputs
    private static final Scanner scanner = new Scanner(System.in);

    // Method to input an integer
    public static int intgr() {
        int value = scanner.nextInt();
        scanner.nextLine(); // Clear the buffer
        return value;
    }

    // Method to input a double
    public static double bigfrac() {
        double value = scanner.nextDouble();
        scanner.nextLine(); // Clear the buffer
        return value;
    }

    // Method to input a float
    public static float frac() {
        float value = scanner.nextFloat();
        scanner.nextLine(); // Clear the buffer
        return value;
    }

    // Method to input a long
    public static long lng() {
        long value = scanner.nextLong();
        scanner.nextLine(); // Clear the buffer
        return value;
    }

    // Method to input a string (a single token, like one word)
    public static String word() {
        String value = scanner.next();
        scanner.nextLine(); // Clear the buffer
        return value;
    }

    // Method to input a full line of text
    public static String line() {
        return scanner.nextLine();
    }

    // Method to input a boolean
    public static boolean bool() {
        boolean value = scanner.nextBoolean();
        scanner.nextLine(); // Clear the buffer
        return value;
    }

    // Method to input a letter
    public static char ltr() {
        char value = scanner.next().charAt(0); // Directly getting the first character
        scanner.nextLine(); // Clear the buffer
        return value;
    }

    // Method to close the scanner (though it's not typically needed in this utility class)
    public static void close() {
        scanner.close();
    }
}
