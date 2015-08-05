package nl.uva.lucenefacility;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

/**
 *
 * @author Mostafa Dehghani
 */
public class MyAnalyzer {

    private String eol = System.getProperty("line.separator");
    private Integer tokenMinLength = Integer.MIN_VALUE;
    private Integer tokenMaxLength = Integer.MAX_VALUE;
    

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyAnalyzer.class.getName());
    private CharArraySet stopList = null;
    private Boolean steming;
    private Boolean stopwordRemooving;

    public MyAnalyzer(Boolean steming, ArrayList<String> stopCollection) { //In case of stopword removing
        stopList = new CharArraySet(stopCollection, true);
        this.stopwordRemooving = true;
        this.tokenMinLength = 2;
        this.steming = steming;
    }

    public MyAnalyzer(Boolean steming) { //In case of no stopword removing
        this.stopwordRemooving = false;
        this.steming = steming;
    }

    public Analyzer ArbitraryCharacterAsDelimiterAnalyzer(final Character delimiter) {

        return new Analyzer() {
//            @Override
//            protected Analyzer getWrappedAnalyzer(String string) {
//                return new WhitespaceAnalyzer();
//            }
            @Override
            protected Analyzer.TokenStreamComponents createComponents(String string) {
                Tokenizer tokenizer = new CharTokenizer() {
                    @Override
                    protected boolean isTokenChar(final int character) {
                        return delimiter != character;
                    }
                };
                TokenStream filter = new LowerCaseFilter(tokenizer);
                return new TokenStreamComponents(tokenizer, filter);
            }

        };
    }

    /////////////
    public Analyzer MyNgramAnalyzer() {

        return new AnalyzerWrapper(Analyzer.PER_FIELD_REUSE_STRATEGY) {

            int wordDelimiterConfig = WordDelimiterFilter.GENERATE_WORD_PARTS;

            @Override
            protected Analyzer getWrappedAnalyzer(String fieldName) {
                return new StandardAnalyzer();
            }

            @Override
            protected Analyzer.TokenStreamComponents wrapComponents(
                    String fieldName, Analyzer.TokenStreamComponents tsc) {
                TokenStream tokenStream = new WordDelimiterFilter(new StandardFilter(
                        tsc.getTokenStream()), wordDelimiterConfig, null);

                tokenStream = new LowerCaseFilter(tokenStream);

                return new StandardAnalyzer.TokenStreamComponents(
                        tsc.getTokenizer(), tokenStream);
            }
        };
    }

    ///////////
    public Analyzer MyEnglishAnalizer() {

        if (steming && stopwordRemooving) {
            return new AnalyzerWrapper(Analyzer.PER_FIELD_REUSE_STRATEGY) {
                @Override
                protected Analyzer getWrappedAnalyzer(String string) {
                    return new StandardAnalyzer();
                }

                @Override
                protected Analyzer.TokenStreamComponents wrapComponents(String fieldName, Analyzer.TokenStreamComponents tsc) {
                    TokenStream tokenStream = new StandardFilter(tsc.getTokenStream());
                    tokenStream = getStandardTokenStream(tokenStream);
                    tokenStream = new PorterStemFilter(tokenStream);
                    tokenStream = new StopFilter(tokenStream, stopList);
                    return new StandardAnalyzer.TokenStreamComponents(tsc.getTokenizer(), tokenStream);
                }
            };
        } else if (!steming && stopwordRemooving) {
            return new AnalyzerWrapper(Analyzer.PER_FIELD_REUSE_STRATEGY) {
                @Override
                protected Analyzer getWrappedAnalyzer(String string) {
                    return new StandardAnalyzer();
                }

                @Override
                protected Analyzer.TokenStreamComponents wrapComponents(String fieldName, Analyzer.TokenStreamComponents tsc) {
                    TokenStream tokenStream = new StandardFilter(tsc.getTokenStream());
                    tokenStream = getStandardTokenStream(tokenStream);
                    tokenStream = new StopFilter(tokenStream, stopList);
                    return new StandardAnalyzer.TokenStreamComponents(tsc.getTokenizer(), tokenStream);
                }
            };
        } else if (steming && !stopwordRemooving) {
            return new AnalyzerWrapper(Analyzer.PER_FIELD_REUSE_STRATEGY) {
                @Override
                protected Analyzer getWrappedAnalyzer(String string) {
                    return new WhitespaceAnalyzer();
                }

                @Override
                protected Analyzer.TokenStreamComponents wrapComponents(String fieldName, Analyzer.TokenStreamComponents tsc) {

                    TokenStream tokenStream = new StandardFilter(tsc.getTokenStream());
                    tokenStream = getStandardTokenStream(tokenStream);
                    tokenStream = new PorterStemFilter(tokenStream);
                    return new WhitespaceAnalyzer.TokenStreamComponents(tsc.getTokenizer(), tokenStream);
                }
            };
        }
        return new WhitespaceAnalyzer();
    }
    
    private TokenStream getStandardTokenStream(TokenStream tokenStream){
        tokenStream = new LowerCaseFilter(tokenStream);
        tokenStream = new WordDelimiterFilter(tokenStream, WordDelimiterFilter.GENERATE_NUMBER_PARTS, null);
        tokenStream = new WordDelimiterFilter(tokenStream, WordDelimiterFilter.GENERATE_WORD_PARTS, null);
        tokenStream = new LengthFilter(tokenStream, tokenMinLength, tokenMaxLength);
        tokenStream = new RemoveNumberFilter(tokenStream);
        return  tokenStream;
    }

    public Analyzer getAnalyzer(String Language) throws FileNotFoundException, Throwable {
        Analyzer analyzer = null; //new SimpleAnalyzer();
        if (Language.equalsIgnoreCase("EN")) {
            analyzer = MyEnglishAnalizer();
        }

        if (analyzer == null) {
            Throwable ex = new Throwable("Language is not set correctly in the config file...");
            throw ex;
        }
        return analyzer;

    }
}

class RemoveNumberFilter extends FilteringTokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public RemoveNumberFilter(TokenStream in) {
        super(in);
    }
    @Override
    protected boolean accept() {
        String regex = "^[\\d,\\.]+$";//".*[^0-9].*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(termAtt.toString());
        return !matcher.matches();
    }
}
