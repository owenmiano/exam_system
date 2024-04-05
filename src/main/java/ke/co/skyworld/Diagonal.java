package ke.co.skyworld;

import java.util.Scanner;

public class Diagonal {
    // write code here
    public static void main(String[] args){
        inputThenPrintSumAndAverage();
    }

    public static void inputThenPrintSumAndAverage (){
        int sum = 0;
        int count = 0;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Asks for user input
            System.out.println("Enter a number:");
            String input = scanner.nextLine();
            try {
                int num=Integer.parseInt(input);
                sum += num;
                count++;
            } catch (NumberFormatException e) {
                long avg = count > 0 ? Math.round((double) sum / count) : 0;
                System.out.println("SUM = " + sum + " AVG = " + avg);
                return;
            }
        }
    }
}
