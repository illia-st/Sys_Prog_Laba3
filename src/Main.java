import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static String Stop_word = "EXIT";
    private static String punctuators = List.of("[", "]", "(", ")", "{", "}", ".", "->", "++", "--", "&", "*", "+", "-",
                    "~", "!", "/", "%", "<<", ">>", "<", ">", "<=", ">=", "==", "!=",  "^",  "|", "&&", "||", "?", ":", ";",
                    "...", "=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", "&=", "^=", "|=", ",", "#", "##",
                    "<:", ":>", "<%", "%>", "%:", "%:%:")
            .stream()
            .map(Pattern::quote)
            .collect(Collectors.joining("|", "(", ")"));
    private static String Processing() throws IOException {
        System.out.print("To close the application enter \"EXIT\". Enter the path to your file > ");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        return reader.readLine();
    }
    private static Stream<String> ReadFile(String path) throws IOException{
        Stream<String> text = null;
        Path path_ = Paths.get(path);
        if(Files.exists(path_)){
            text = Files.lines(path_);
            return text;
        }
        throw new IOException("The file doesn't exist");
    }
    public static void main(String[] args) {
        System.out.println(punctuators);
//        while(true) {
//            try {
//                String file_path = Processing();
//                if(file_path.equals(Stop_word)){
//                    System.out.println("Bye");
//                    return;
//                }
//                Tokenizer tokenizer = new Tokenizer();
//                var text = ReadFile(file_path);
//                text = Preprocessor.Preprocess(text);
//                var tokens = tokenizer.Analise(text);
//            } catch (Exception ex) {
//                System.out.println("An exception was occurred. Description: " + ex.getMessage());
//            } finally {
//
//            }
//        }

    }
}