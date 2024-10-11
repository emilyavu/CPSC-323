import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataProcessor {

    // Keywords, operators, and separators arrays
    static String[] keywords = {"public", "class", "static", "void", "int", "for", "if", "else", "return"};
    static String[] operators = {"=", "+", "-", "*", "/", "<", ">", "++", "."};
    static String[] separators = {"{", "}", "(", ")", ";", ":"};

    // This method is used to read and remove extra spaces from the input
    public static List<String> readFile(String filePath, List<String> commentsList) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // For single line comments
                if (line.contains("//")) {
                    commentsList.add(line.substring(line.indexOf("//")).trim());
                    line = line.substring(0, line.indexOf("//")).trim();
                }
                // For multi line comments
                if (line.contains("/*")) {
                    StringBuilder multiLineComment = new StringBuilder(line.substring(line.indexOf("/*")).trim());
                    commentsList.add(multiLineComment.toString());
                    while (!line.contains("*/")) {
                        line = reader.readLine();
                        if (line == null) break;
                        multiLineComment.append(" ").append(line.trim());
                    }
                    commentsList.add(multiLineComment.toString());
                    continue;  // Skip processing the rest of the comment block
                }

                line = line.trim();  // Cut extra spaces
                if (!line.isEmpty()) {
                    lines.add(line);  // Add the cleaned code line
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return lines;
    }

    // This method is used to tokenize the code and categorize it
    public static void tokenize(List<String> codeLines, List<String> commentsList) {
        List<String> keywordsList = new ArrayList<>();
        Set<String> identifiersList = new HashSet<>();
        List<String> operatorsList = new ArrayList<>();
        List<String> separatorsList = new ArrayList<>();
        List<String> literalsList = new ArrayList<>();

        boolean insideStringLiteral = false;
        StringBuilder currentLiteral = new StringBuilder();
        boolean insideForLoop = false;

        for (String line : codeLines) {
            if (line.contains("for")) {
                insideForLoop = true;
            }

            // Tokenize the remaining line
            String[] tokens = line.split("(?=[{}()=;.+<>])|(?<=[{}()=;.+<>])|\\s+");

            for (String token : tokens) {
                token = token.trim();
                if (token.isEmpty()) continue;

                // Handle string literals
                if (insideStringLiteral || token.startsWith("\"")) {
                    insideStringLiteral = true;
                    currentLiteral.append(token);

                    if (token.endsWith("\"")) {
                        insideStringLiteral = false;
                        literalsList.add(currentLiteral.toString().trim());
                        currentLiteral.setLength(0);
                    } else {
                        currentLiteral.append(" ");
                    }
                    continue;
                }

                // Skip numbers inside for-loop as literals
                if (insideForLoop && isNumeric(token)) {
                    continue;  // Ignore numeric tokens inside the for loop
                }

                // Check for keywords, operators, separators, literals, and identifiers
                if (Arrays.asList(keywords).contains(token)) {
                    keywordsList.add(token);
                } else if (Arrays.asList(operators).contains(token)) {
                    operatorsList.add(token);
                } else if (Arrays.asList(separators).contains(token)) {
                    separatorsList.add(token);
                } else if (isNumeric(token)) {
                    literalsList.add(token);
                } else {
                    identifiersList.add(token);  // Anything else is an identifier
                }
            }

            // End for loop context after encountering the closing parenthesis
            if (line.contains(")")) {
                insideForLoop = false;  // Exit for loop context
            }
        }

        // Print categorized tokens
        System.out.println("Keywords: " + keywordsList);
        System.out.println("Identifiers: " + new ArrayList<>(identifiersList));  // Convert Set back to List for printing
        System.out.println("Operators: " + operatorsList);
        System.out.println("Separators: " + separatorsList);
        System.out.println("Literals: " + literalsList); 
        System.out.println("Comments: " + commentsList); 

        // Calculate total token count
        int totalTokens = keywordsList.size() + identifiersList.size() + operatorsList.size() +
                          separatorsList.size() + literalsList.size() + commentsList.size();
        System.out.println("Total tokens: " + totalTokens);
    }

    // This method is used to check if a string is numeric
    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // This main method is used to run the program
    public static void main(String[] args) {
        String filePath = "example_codes_1.txt";
        List<String> commentsList = new ArrayList<>();
        List<String> fileContent = readFile(filePath, commentsList);  // Step 1: Read and clean the file
        tokenize(fileContent, commentsList);  // Step 2: Tokenize and print the tokens
    }
}
