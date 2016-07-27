/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.lucenefacility;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import nl.uva.settings.Config;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author Mostafa Dehghani
 */
public class LuceneUtil {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LuceneUtil.class.getName());
    
    public static List<String> tokenizeString(Analyzer analyzer, String string) {
      List<String> result = new ArrayList<String>();
      try {
        TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
        stream.reset();
        while (stream.incrementToken()) {
          result.add(stream.getAttribute(CharTermAttribute.class).toString());
        }
      } catch (IOException e) {
            throw new RuntimeException(e);
      }
      return result;
    }
    
    public static List<String> tokenizeString(String string, Boolean Stemming) throws Throwable {
        MyAnalyzer myAnalyzer = new  MyAnalyzer(Stemming);
        return  tokenizeString(myAnalyzer.getAnalyzer(Config.configFile.getProperty("CORPUS_LANGUAGE")), string);
    }
}
