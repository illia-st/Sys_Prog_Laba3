import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Tokenizer {
    private static <T> HashSet<T> newHashSet(T... objs) {
        HashSet<T> set = new HashSet<T>();
        Collections.addAll(set, objs);
        return set;
    }
    static private  HashSet<String> directives = newHashSet(
            "#include", "#define", "#undef", "#if", "#ifdef", "#ifndef", "#error", "#pragma", "#endif"
    );
    // token types
    public enum TokenType{
        COMMENT(Tokenizer.CommentStart),
        HEADER(Tokenizer.HEADERS),
        PREPROCESSOR_DIRECTIVE(Tokenizer.Preproc_directives),
        WRONG_STRING_LITERALS(Tokenizer.Wrong_String_literals),
        WRONG_NUMBER_LITERALS(Tokenizer.Wrong_number_Literals),
        STRING_LITERAL(Tokenizer.STRING_LITERALS),
        CHAR_LITERALS(Tokenizer.Char_Literals),
        ERROR(Tokenizer.ERRORS),
        KEYWORD(Tokenizer.Keywords),
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
            TokenType.WRONG_STRING_LITERALS, TokenType.STRING_LITERAL, TokenType.CHAR_LITERALS,
            TokenType.WRONG_NUMBER_LITERALS, TokenType.KEYWORD,
            TokenType.IDENTIFIER, TokenType.NUMBER_LITERALS, TokenType.OPERATORS,
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
    private final static String HEADERS = "(<[^>]+>)";
    private final static String Preproc_directives = "(#[a-z|\\S]{2,})";
    private final static String STRING_LITERALS = "(\\\"[^\\\"]*\\\")";
    private final static String Char_Literals = "('[^']{0,1}')";
    private final static String Number_Literal = "((0x[A-Fa-f0-9]*)|([\\d]+[.]?[\\d]*))";
    private final static String Wrong_number_Literals = "((" + Number_Literal + "[A-z]+" + Number_Literal + ")|(" + Number_Literal + "[A-z]+))";
    private final static String Wrong_String_literals =
            "([A-z|0-9]+" + STRING_LITERALS + "[A-z|0-9]+|[A-z|0-9]+" + STRING_LITERALS + "|" + STRING_LITERALS + "[A-z|0-9]+)";
    private final static String Keywords = "(and)|(auto)|(bool)|(break)|(case)|(catch)|(char)|(class)|(const)|(continue)" +
            "|(decltype)|(default)|(delete)|(do)|(double)|(dynamic_cast)|(else)|(enum)|(explicit)|(extern)" +
            "|(false)|(float)|(for)|(friend)|(goto)|(if)|(inline)|(int)|(long)|(main)|(mutable)|(namespace)" +
            "|(new)|(nullptr)|(operator)|(or)|(private)|(protected)|(public)|(register)|(reinterpret_cast)" +
            "|(return)|(short)|(signed)|(sizeof)|(static)|(static_cast)|(struct)|(switch)|(template)|(this)" +
            "|(throw)|(true)|(try)|(typedef)|(typeid)|(typename)|(union)|(unsigned)|(using)|(virtual)|(void)" +
            "|(volatile)|(while)";
    private final static String PUNCTUATIONS = "(\\(|\\)|\\[|\\]|;|,|.|\\?|\\{|\\})";
    private final static String Operator = "(\\+|\\-|\\*|\\/|->|>=|<=|<>|&&|<<|>>|<|>|==|=|&|%|!=|!|\\\\|~|%|\\||\\^|::|:)";
    private final static String ERRORS = "([^\\n\\s\\t\\r]+)";
    private final static String IDENTIFIERS = "([a-zA-Z_][a-zA-Z0-9_]*)";
    private ArrayList<Token> tokens = new ArrayList<>();


    private boolean inComment = false;
    /*
    */

    private String GetTokensFromString(String line, TokenType token_to_find){
        if(token_to_find == TokenType.WRONG_STRING_LITERALS){
            int i = 10;
        }
        line = line.trim();
        if(inComment){
            int index_of_comment_end = line.indexOf("*/");
            if(index_of_comment_end == -1){
                return "";
            }
            inComment = false;
            line = line.substring(index_of_comment_end + 1);
        }
        if(line.isEmpty()) return "";
        int start = 0, end = 0, index = 0;
        Pattern pattern = Pattern.compile(token_to_find.regex);
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()){
            String found_value = matcher.group();
            switch (token_to_find){
                case COMMENT:
                    index = line.indexOf(found_value);
                    if(found_value.startsWith("//")) {
                        start = index;
                        end = line.length();
                        break;
                    }
                    if(found_value.endsWith("*/")){
                        start = index;
                        end = Math.min(index + found_value.length(), line.length());
                    }
                    else{
                        inComment = true;;
                        start = index;
                        end = line.length();
                    }
                    break;
                case HEADER:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.HEADER, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case PREPROCESSOR_DIRECTIVE:
                    index = line.indexOf(found_value);
                    if(!directives.contains(found_value)){
                        tokens.add(new Token(TokenType.ERROR, found_value));
                    }else{
                        tokens.add(new Token(TokenType.PREPROCESSOR_DIRECTIVE, found_value));
                    }
                    start = index;
                    end = index + found_value.length();
                    break;
                case WRONG_STRING_LITERALS:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.ERROR, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case STRING_LITERAL:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.STRING_LITERAL, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case CHAR_LITERALS:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.CHAR_LITERALS, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case WRONG_NUMBER_LITERALS:
                    index = line.indexOf(found_value);
                    if(index != 0 && ((line.charAt(index - 1) < 65 && line.charAt(index - 1) > 90
                        || (line.charAt(index - 1) < 97 && line.charAt(index - 1) > 122)))) {
                        tokens.add(new Token(TokenType.ERROR, found_value));
                        start = index;
                        end = index + found_value.length();
                    }
                    break;
                case NUMBER_LITERALS:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.NUMBER_LITERALS, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case KEYWORD:
                    index = line.indexOf(found_value);
                    if((index != 0 && Character.isLetterOrDigit(line.charAt(index - 1)))
                        || (index + found_value.length() != line.length() && Character.isLetterOrDigit(line.charAt(index + found_value.length())))){
                        break;
                    }
                    tokens.add(new Token(TokenType.KEYWORD, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case IDENTIFIER:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.IDENTIFIER, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case OPERATORS:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.OPERATORS, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case PUNCTUATIONS:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.PUNCTUATIONS, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                case ERROR:
                    index = line.indexOf(found_value);
                    tokens.add(new Token(TokenType.ERROR, found_value));
                    start = index;
                    end = index + found_value.length();
                    break;
                default:
                    break;
            }
            if(start != 0 || end != 0){
                line = GetTokensFromString(line.substring(0, start).concat(line.substring(end)), token_to_find);
            }
        }
        return line;
    }
    public void GetTokensFromString(String line){
        for(var token_type: order_to_analise){
            // if line is empty - return from the function and get another string
            line = GetTokensFromString(line, token_type);
            if(line.isEmpty()){
                return;
            }
        }
    }
    public void Clear(){
       tokens.clear();
    }
    public ArrayList<Token> Analise(Stream<String> lines){
        lines.forEach(this::GetTokensFromString);
        return tokens;
    }
}