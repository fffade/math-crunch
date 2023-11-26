/*
    Math Crunch
    Author: fffade
    Date: 11-26-23
 */

/* A simple math game where you solve math equations that get progressively harder */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


/* A list of all the operation types seen in the game */
enum Operation
{
    Addition,
    Subtraction,
    Multiplication,
    Division,
    Fraction
}

class Equation {
    public static final int OTHER_ANSWERS_COUNT = 3;

    // The correct answer for this equation
    public float correctAnswer;

    // Random 'other' answers
    public Float[] otherAnswers;

    // The equation as a whole represented as a string
    public String equation;

    private Operation[] usedOperations;

    public Equation(String equationStr, float correctAnswer, Operation[] usedOperations) {
        this.correctAnswer = correctAnswer;
        this.equation = equationStr;
        this.usedOperations = usedOperations;
        this.otherAnswers = randomPossibleAnswers();
    }

    /* Generates 'other' incorrect answers to this equation */
    private Float[] randomPossibleAnswers() {
        Float[] possibleAnswers = new Float[OTHER_ANSWERS_COUNT];

        int minValue = 0;
        int maxValue = 20;

        // Adjust minimum and maximum based on supported operations
        if(Arrays.stream(usedOperations).anyMatch(op -> op == Operation.Subtraction)) {
            minValue = -maxValue;
        }

        for (int i = 0; i < OTHER_ANSWERS_COUNT; i++) {

            int possibleAnswer;
            do {
                possibleAnswer = (int) Math.floor(Math.random() * (maxValue - minValue) + 1) + minValue;
            } while (possibleAnswer == correctAnswer);

            possibleAnswers[i] = (float) possibleAnswer;
        }

        return possibleAnswers;
    }

    /* Prints out all the answers to this equation */
    public void printPossibleAnswers() {
        // insert REAL answer randomly into other answers
        List<Float> allAnswers = new ArrayList<Float>(Arrays.asList(otherAnswers));

        allAnswers.add((int) Math.floor(Math.random() * allAnswers.size()), correctAnswer);

        for (int i = 0; i < allAnswers.size(); i++) {
            System.out.printf("%d. %.2f\n", i + 1, allAnswers.get(i));
        }
    }

    /* Returns if a given answer is VALID */
    public boolean isAnswerValid(float givenAnswer) {
        return givenAnswer == correctAnswer || Arrays.stream(otherAnswers).anyMatch(answer -> answer == givenAnswer);
    }

    /* Returns if a given answer is CORRECT */
    public boolean isAnswerCorrect(float givenAnswer)
    {
        return correctAnswer == givenAnswer;
    }
}


// Different levels are worth a different base amount of stars
// Levels support different operations, too
class Level {

    public int number;

    // Only these operation types will be seen in this level
    private Operation[] supportedOperations;

    // How many variables are in equations of this level
    private int equationLength;

    // Declares a new level
    public Level(int number, Operation[] operations, int equationLength) {
        this.number = number;
        this.supportedOperations = operations;
        this.equationLength = equationLength;
    }

    // Get a random digit 0-9
    public int randomDigit() {
        return (int)Math.floor(Math.random() * 10);
    }

    // Generate a unique question for this level given the length and operations
    public Equation generateEquation()
    {
        String fullEquation = "";
        float correctAnswer = 0; // Track the correct answer to this random equation

        Operation lastOperation = null;

        for(int i = 0; i < equationLength; i++) {

            int digit = randomDigit();

            // Add to correct answer if an operation existed before this digit
            if(lastOperation == null) {
                correctAnswer = digit;
            }
            else {
                switch(lastOperation) {
                    case Addition:
                        correctAnswer += digit;
                        break;

                    case Subtraction:
                        correctAnswer -= digit;
                        break;

                    case Multiplication:
                        correctAnswer *= digit;
                        break;

                    case Division:
                        correctAnswer /= digit;
                        break;
                }
            }

            fullEquation += String.valueOf(digit);

            // Keep adding operations ?
            if(i < equationLength - 1) {

                Operation nextOperation = supportedOperations[(int)(Math.random() * supportedOperations.length)];

                switch(nextOperation) {
                    case Addition:
                        fullEquation += " + ";
                        break;

                    case Subtraction:
                        fullEquation += " - ";
                        break;

                    case Multiplication:
                        fullEquation += " * ";
                        break;

                    case Division:
                        fullEquation += " / ";
                        break;
                }

                lastOperation = nextOperation;
            }
        }

        return new Equation(fullEquation, correctAnswer, supportedOperations);
    }
}

public class Main {

    public static final int QUESTIONS_PER_LEVEL = 5;

    /* Pre-declared levels */
    public static List<Level> levels = new ArrayList<Level>()
    {
        {
            add(new Level(1, new Operation[] {Operation.Addition}, 2));
            add(new Level(2, new Operation[] {Operation.Addition, Operation.Subtraction}, 3));
        }
    };


    /* Main method */
    public static void main(String[] args)
    {
        // Welcome message with rules
        System.out.println("Welcome to MATH CRUNCH!");

        System.out.println("\nThe rules are simple:\n" +
                            "- Solve multiple-choice math problems\n" +
                            "- Answer incorrectly and the game is over\n" +
                            "- Forfeit and the game is over\n" +
                            String.format("- Every %d questions, the difficulty is ramped up\n", QUESTIONS_PER_LEVEL) +
                            String.format("- After %d questions, you win\n", levels.size() * QUESTIONS_PER_LEVEL));

        Scanner scanner = new Scanner(System.in);


        /* Iterate through levels, asking multiple equations per one */
        int questionNum = 1;

        boolean isWinner = true;

        levels:
        for(Level level : levels)
        {
            for(int i = 0; i < QUESTIONS_PER_LEVEL; i++)
            {
                /* Create a new equation */
                Equation currentEquation = level.generateEquation();

                /* Display equation and possible answers */
                System.out.printf("Question #%d) %s%n", questionNum, currentEquation.equation);

                currentEquation.printPossibleAnswers();


                float validAnswer = -1f;

                // Wait for valid input from user
                do {

                    try {
                        System.out.print("\n>> ");
                        float input = Float.parseFloat(scanner.nextLine());

                        if(!currentEquation.isAnswerValid(input)) {
                            System.out.println("Please enter a selection from the multiple choice options.");
                            continue;
                        }

                        validAnswer = input;
                    }
                    catch(NumberFormatException e) {
                        System.out.println("Please enter a real number.");
                    }

                } while(validAnswer == -1f);

                // Continue if correct, end game if wrong
                if(currentEquation.isAnswerCorrect(validAnswer)) {
                    System.out.println("You are correct!\n");

                    questionNum++;
                    continue;
                }

                isWinner = false;

                System.out.println("That is wrong...");
                break levels;
            }
        }

        // End-game results
        if(!isWinner) {
            System.out.printf("\nYou lost on question #%d.", questionNum);
            return;
        }

        System.out.printf("\nCongratulations! You completed all %d questions!", levels.size() * QUESTIONS_PER_LEVEL);
    }
}
