import java.io.*;
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
    private static void PrintToFile(String path, ArrayList<Tokenizer.Token> tokens) throws IOException{
        File file = new File(path);
        file.createNewFile();
        FileWriter writer = new FileWriter(path);
        for(var token: tokens){
            switch(token.type){
                case PREPROCESSOR_DIRECTIVE:{
                    writer.write("<preprocessor directive> - " + token.value);
                }
                default:{
                    writer.write("<error> - " + token.value);
                }
            }
        }
        writer.close();

    }
    public static void main(String[] args) {
        while(true) {
            try {
                String file_path = Processing();
                if(file_path.equals(Stop_word)){
                    System.out.println("Bye");
                    return;
                }
                Tokenizer tokenizer = new Tokenizer();
                var text = ReadFile("Files/test1.txt");
                text = Preprocessor.Preprocess(text);
                var tokens = tokenizer.Analise(text);
                PrintToFile("output.txt", tokens);
            } catch (Exception ex) {
                System.out.println("An exception was occurred. Description: " + ex.getMessage());
            } finally {


            }
        }
    }
}