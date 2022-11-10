import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tokenizer {
    public static <T> HashSet<T> newHashSet(T... objs) {
        HashSet<T> set = new HashSet<T>();
        Collections.addAll(set, objs);
        return set;
    }
    static private HashSet<String> keywords = newHashSet(
            "auto", "if", "unsigned", "break", "inline", "void", "case", "int", "volatile", "char", "long", "while",
            "const", "register", "_Alignas", "continue", "restrict", "_Alignof", "default", "return", "_Atomic", "do",
            "short", "_Bool", "double", "signed", "_Complex", "else", "sizeof", "_Generic", "enum", "static",
            "_Imaginary", "extern", "struct", "_Noreturn", "float", "switch", "_Static_assert", "for", "typedef",
            "_Thread_local", "goto", "union");

    static private  HashSet<String> directives = newHashSet(
            "#include", "#define", "#undef", "#if", "#ifdef", "#ifndef", "#error", "#pragma"
    );

    // token types
    public static String temp = "";
    public enum TokenType{
        COMMENT(Tokenizer.CommentStart),
        HEADER(Tokenizer.HEADERS),
        PREPROCESSOR_DIRECTIVE(Tokenizer.Preproc_directives),
        STRING_LITERAL(Tokenizer.STRING_LITERALS),
        CHAR_LITERALS(Tokenizer.Char_Literals),
        ERROR(Tokenizer.ERRORS),
        KEYWORD("temp"),
        NUMBER_LITERALS(Tokenizer.Number_Literal),
        IDENTIFIER(Tokenizer.IDENTIFIERS),
        OPERATORS(Tokenizer.Operator),
        PUNCTUATIONS(Tokenizer.PUNCTUATIONS);
        public String regex = "";
        TokenType(String line) {
            regex = line;
        }
    }
    private final static TokenType[] order_to_analise = {
            TokenType.COMMENT, TokenType.HEADER, TokenType.PREPROCESSOR_DIRECTIVE,
            TokenType.STRING_LITERAL, TokenType.CHAR_LITERALS, TokenType.NUMBER_LITERALS,
            TokenType.KEYWORD, TokenType.IDENTIFIER, TokenType.OPERATORS,
            TokenType.PUNCTUATIONS, TokenType.ERROR
                                            };

    // craete class Token
    public class Token{
        public TokenType type;
        public String value;
        public Token(){}
        public Token(TokenType token_type, String token_value){
            type = token_type;
            value = token_value;
        }
    }
    private final static String CommentStart = "(/\\*|//)";
    private final static String HEADERS = "(#include[\\s]+)(<[^>\\s]+>|\\\"[^\\s\\\"]+\\\")";
    private final static String Preproc_directives = "([#!][ \\t]*[A-z]{2,}[\\s]{1,}?([A-z]{2,}[\\s]{1,}?)?)([\\\\(]?[^\\s\\\\)]{1,}[\\\\)]?)?";
    private final static String STRING_LITERALS = "(\\\"[^\\\"]*\\\")";
    private final static String Char_Literals = "('[^']{0,1}')";

    private final static String Number_Literal = "(0x[A-Fa-f0-9]*)|([\\\\d]+[.]?[\\\\d]*)";
    private final static String PUNCTUATIONS = "(\\(|\\)|\\[|\\]|;|,|\\?|\\{|\\})";
    private final static String Operator = "(\\+|\\-|\\*|\\/|>=|<=|<>|&&|<<|>>|<|>|==|=|&|%|!=|!|\\\\|~|%|\\||\\^|:)";
    // regexes
    // I would like to look for identifiers in last turn
    private final static String ERRORS = "([^\\n\\s\\t\\r]+)";
    // I will do it by ALL_POS_WORDS
    private final static String IDENTIFIERS = "([a-zA-Z_][a-zA-Z0-9_]*)";
    // find header
    // ucn is also a part of escape sequences
    private static String ESCAPE_SEQUENCES = "('\\\\['\\\"\\\\abfnrtv]{1}')";
    // first find header
    // then all the string literals
    private static String Error = "[^\\n\\t\\r\\s]+";
    private ArrayList<Token> tokens = new ArrayList<>();


    private boolean inComment = false;
    /*
    */

    private void GetTokensFromString(String line){
        line = line.trim();
        if(inComment){
            int index_of_comment_end = line.indexOf("*/");
            if(index_of_comment_end == -1){
                return;
            }
            inComment = false;
            line = line.substring(index_of_comment_end + 1);
        }
        if(line.isEmpty()) return;
        Matcher matcher;
        Pattern pattern;
        for(var token_type: order_to_analise){
            boolean directive_chain = false, end_of_line = false;
            int index = 0;
            ArrayList<Integer> start_indexes = new ArrayList<>(), end_indexes = new ArrayList<>();
            String copy_line = line;
            pattern = Pattern.compile(token_type.regex);
            matcher = pattern.matcher(line);
            while(matcher.find()){
                String found_value = matcher.group();
                switch (token_type){
                    case COMMENT:
                        index = line.indexOf(found_value);
                        if(found_value.startsWith("//")){
                            start_indexes.add(index);
                            end_indexes.add(line.length());
                            end_of_line = true;
                            break;
                        }else if(found_value.endsWith("*/")){
                            start_indexes.add(index);
                            end_indexes.add(found_value.length());
                        }else{
                            inComment = true;
                            end_of_line = true;
                        }
                        start_indexes.add(index);
                        end_indexes.add(index + found_value.length());
                        break;
                    case HEADER:
                        index = line.indexOf(found_value);
                        tokens.add(new Token(TokenType.PREPROCESSOR_DIRECTIVE, found_value));
                        start_indexes.add(index);
                        end_indexes.add(found_value.length());
                        break;
                    case PREPROCESSOR_DIRECTIVE:
                        index = line.indexOf(found_value);
                        // check if we have found an include - it is an error then
                        if(found_value.startsWith("#include")){
                            tokens.add(new Token(TokenType.ERROR, found_value));
                        }else{
                            String directive = found_value.substring(0, found_value.indexOf(' '));
                            if(!directives.contains(directive)){
                                tokens.add(new Token(TokenType.ERROR, found_value));
                            }else{
                                tokens.add(new Token(TokenType.PREPROCESSOR_DIRECTIVE, found_value));
                            }
                        }
                        start_indexes.add(index);
                        end_indexes.add(index, found_value.length());
                    default:
                        break;
                }
            }
            StringBuilder builder = new StringBuilder();
            int curr_index = 0;
            for(int i = 0; i < start_indexes.size(); ++i){
                if(curr_index < start_indexes.get(i)){
                    builder.append(line.substring(curr_index, start_indexes.get(i)));
                }else{
                    curr_index = end_indexes.get(i);
                }
            }
            if(!start_indexes.isEmpty()) {
                line = builder.toString();
            }
        }
    }
    public ArrayList<Token> Analise(Stream<String> lines){
        lines.forEach(this::GetTokensFromString);
        return tokens;
    }
}