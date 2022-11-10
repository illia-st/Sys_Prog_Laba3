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
    static HashSet<String> keywords = newHashSet(
            "auto", "if", "unsigned", "break", "inline", "void", "case", "int", "volatile", "char", "long", "while",
            "const", "register", "_Alignas", "continue", "restrict", "_Alignof", "default", "return", "_Atomic", "do",
            "short", "_Bool", "double", "signed", "_Complex", "else", "sizeof", "_Generic", "enum", "static",
            "_Imaginary", "extern", "struct", "_Noreturn", "float", "switch", "_Static_assert", "for", "typedef",
            "_Thread_local", "goto", "union");
    static HashSet<String> preprocessor_directives = newHashSet(
      "#include", "#define", "#undef", "#if", "#ifdef", "#ifndef", "#error", "#pragma"
    );

    // token types
    public enum TokenType{

        STRING_LITERAL(Tokenizer.STRING_LITERALS),
        CHAR_LITERALS(Tokenizer.temp),
        ERROR(Tokenizer.ALL_POS_WORDS),
        DIGITS(Tokenizer.temp),
        PREPROCESSOR_DIRECTIVE(Tokenizer.temp),
        MACRO_FUNCTION(Tokenizer.temp),
        SINGLE_COMMENT(Tokenizer.temp),
        MULTI_COMMENT(Tokenizer.temp),
        KEYWORD(Tokenizer.temp),
        OPERATORS(Tokenizer.PUNCTUATORS),
        PUNCTUATOR(Tokenizer.PUNCTUATORS),
        IDENTIFIER(Tokenizer.IDENTIFIERS),
        ESCAPING_SEQUENCE(Tokenizer.ESCAPE_SEQUENCES),
        HEADER(Tokenizer.HEADERS);
        public Pattern pattern;
        TokenType(String line) {
            pattern = Pattern.compile(line);
        }

    }
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
    private static String PUNCTUATORS = "(\\[|]|\\(|\\)|{|}|\\.|->|\\+\\+|--|&|\\*|\\+|-|~|!|\\/|%|<<|>>|<|>|<\\=|>\\=|==|" +
            "!=|\\^|\\||&&|\\|\\||\\?|:|;|\\.\\.\\.|=|\\*=|\\/=|%=|\\+=|-=|<<=|>>=|&=|\\^=|\\|=|,|##|<:|:>|<%|%>|%:|%:%:)";

    // regexes
    private static String temp = "temp";
    // I would like to look for identifiers in last turn
    private static String ALL_POS_WORDS = "([^\\n\\s\\t\\r]+)";
    // I will do it by ALL_POS_WORDS
    private static String DIGITS = "";
    private static String IDENTIFIERS = "[a-zA-Z_][a-zA-Z0-9_]*";
    // find header
    // ucn is also a part of escape sequences
    private static String ESCAPE_SEQUENCES = "('\\\\['\\\"\\\\abfnrtv]{1}')";
    // first find header
    private static String HEADERS = "(<[^>\\s]+>|\\\"[^\\s\\\"]+\\\")";
    // then all the string literals
    private static String STRING_LITERALS = "(\\\"[^\\\"]*\\\")";
    private static String SingleLineComments = "(//)";
    private static String MultiLineCommentStarts = "(/\\*|\\*/)";
    private ArrayList<Token> tokens = new ArrayList<>();


    private boolean inComment = false;
    private void GetTokensFromString(String line){
        line = line.trim();
        if(line.isEmpty()) return;
        if(line.startsWith("#")){
            for(var directive: preprocessor_directives){
                if(TryToParseDirective(line, directive)){
                    return;
                }
            }
        }
        else if(line.startsWith("//")){
            return;
        }
        else if(line.startsWith("/*")){
            inComment = true;
            ParseString(line);
        }
        else{
            ParseString(line);
        }
    }
    private void ParseString(String line){
        if(inComment){
            int index = line.indexOf("*/");
            if(index == -1) return;
            inComment = false;
            line = line.substring(index + 2);
            // we have finished the comment
            // so we can start parsing proccedure with the string again
            GetTokensFromString(line);
            return;
        }
        line = line.trim();
        if(line.isEmpty()) return;
        StringBuilder lineCopy = new StringBuilder(line);
        Matcher res = TokenType.SINGLE_COMMENT.pattern.matcher(lineCopy);
        if(res.lookingAt()){
            int index_to_delete = lineCopy.indexOf(res.group());
            lineCopy.delete(index_to_delete, lineCopy.length());
        }
        if(lineCopy.isEmpty()) return;
        res = TokenType.MULTI_COMMENT.pattern.matcher(lineCopy);
        if(res.lookingAt()){
            String result = res.group();
            if(result.length() > 2 && !result.substring(result.length() - 2).equals("*/")){
                inComment = true;
            }
            int index_to_delete = lineCopy.indexOf(result);
            lineCopy.delete(index_to_delete, lineCopy.length());
        }
        if(lineCopy.isEmpty()) return;
        res = TokenType.STRING_LITERAL.pattern.matcher(lineCopy);
    }
    private boolean TryToParseDirective(String line, String directive){
        int index = line.indexOf(directive);
        if(index == -1) return false;
        StringBuilder lineCopy = new StringBuilder(line);
        Token t = new Token();
        lineCopy.delete(index, index + directive.length());
        switch (directive){
            case("#include"):{
                t.type = TokenType.PREPROCESSOR_DIRECTIVE;
                t.value = directive;
                tokens.add(t);
                int i = index;
                while(i < lineCopy.length() && Character.isWhitespace(lineCopy.charAt(i))){
                    ++i;
                }
                Pattern ptrn = Pattern.compile(TokenType.HEADER.toString());

                Matcher m = ptrn.matcher(lineCopy.toString());
                if(m.lookingAt()){
                    String temp = m.group();
                    index = lineCopy.indexOf(temp);
                    lineCopy.delete(index, index + temp.length());
                    tokens.add(new Token(TokenType.HEADER, temp));
                }
                break;
            }
            case ("#define"):{
                t.type = TokenType.MACRO_FUNCTION;
                t.value = directive;
                tokens.add(t);
                break;
            }
            default:{
                t.type = TokenType.PREPROCESSOR_DIRECTIVE;
                t.value = directive;
                tokens.add(t);
                break;
            }
        }
//        ParseString(lineCopy.toString(), false);
        return true;
    }

    public ArrayList<Token> Analise(Stream<String> lines){
        lines.forEach(this::GetTokensFromString);

        return tokens;
    }
}
