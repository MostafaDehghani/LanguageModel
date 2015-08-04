/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.lm;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Mostafa Dehghani
 */
public class SmoothedLM extends LanguageModel {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SmoothedLM.class.getName());
    private LanguageModel backgroundLM;
    private LanguageModel documentLM;
    private Double lambda = 0.9D;

    public SmoothedLM(LanguageModel documentLM, LanguageModel backgroundLM, Double lambda) {
        this.backgroundLM = backgroundLM;
        this.documentLM = documentLM;
        this.lambda = lambda;
        this.generateSmoothedLanguageModel(this.lambda);
    }
    
    public SmoothedLM(LanguageModel documentLM, LanguageModel backgroundLM) {
        this.backgroundLM = backgroundLM;
        this.documentLM = documentLM;
        this.generateSmoothedLanguageModel(this.lambda);
    }

    public void generateSmoothedLanguageModel(Double lambda) {
        HashSet<String> terms = new HashSet<>();
        terms.addAll(this.documentLM.getTerms());
        terms.addAll(this.backgroundLM.getTerms());
        for (String s : terms) {
            Double documentProb = this.documentLM.getProb(s);
            Double backgoundProb = this.backgroundLM.getProb(s);
            if (backgoundProb == null) {
                backgoundProb = 0D;
            }
            if (documentProb == null) {
                documentProb = 0D;
            }
            Double newProb = (lambda * documentProb + ((1 - lambda) * backgoundProb));
            this.setProb(s, newProb);
        }
    }

}
