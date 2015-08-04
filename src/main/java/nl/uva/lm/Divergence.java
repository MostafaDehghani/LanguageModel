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
public class Divergence {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Divergence.class.getName());
    private LanguageModel LM1;
    private LanguageModel LM2;

    public Divergence(LanguageModel LM1, LanguageModel LM2) {
        this.LM1 = LM1;
        this.LM2 = LM2;
    }

    public Divergence(){}
    
    
    public Double JsdScore(LanguageModel lm1, LanguageModel lm2){
        return this.JsdScore(lm1.getModel(), lm1.getModel());
    }
    
    private Double JsdScore(HashMap<String, Double> d1, HashMap<String, Double> d2) {
        Double score = 0D;
        HashMap<String, Double> avg = new HashMap<>();
        HashSet<String> allTerms = new HashSet<>();
        allTerms.addAll(d1.keySet());
        allTerms.addAll(d2.keySet());
        for (String t : allTerms) {
            Double p = (d1.get(t) == null) ? 0 : d1.get(t);
            Double q = (d2.get(t) == null) ? 0 : d2.get(t);
            avg.put(t, ((p + q) / 2));
        }
        score = (this.KldScore(d1, avg) + this.KldScore(d2, avg)) / 2;
        return score;
    }
    
    public Double KldScore(LanguageModel lm1, LanguageModel lm2){
        return this.KldScore(lm1.getModel(), lm1.getModel());
    }

    private Double KldScore(HashMap<String, Double> d1, HashMap<String, Double> d2) {
        Double score = 0D;
        for (String item : d1.keySet()) {
            if (d2.containsKey(item)) {
                Double p = d1.get(item);
                Double q = d2.get(item);
                score += this.singleItemKLD(p, q);
            }
        }
        return score;
    }

    private Double singleItemKLD(Double p, Double q) {
        Double res = p * this.log(p/q, 2D);
        return res;
    }

    public Double getKldSimScore() {
        return -1 * this.KldScore(this.LM1, this.LM2);
    }

    public Double getJsdSimScore() {
        return (1 - this.JsdScore(this.LM1, this.LM2));
    }
    
    private Double log(Double x, Double base)
    {
        return  (Math.log(x) / Math.log(base));
    }

}
