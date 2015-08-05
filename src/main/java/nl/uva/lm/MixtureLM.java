/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.lm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 *
 * @author Mostafa Dehghani
 */
public final class MixtureLM extends LanguageModel {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MixtureLM.class.getName());

    public MixtureLM(HashSet<Entry<Double,LanguageModel>> lms) {
        this.generateMixturedLanguageModel(lms);
    }
    
    public void generateMixturedLanguageModel(HashSet<Entry<Double,LanguageModel>> lms) {
        HashSet<String> allTerms = new HashSet<>();
        for(Entry<Double,LanguageModel> e:lms){
            allTerms.addAll(e.getValue().getTerms());
        }
        for (String t : allTerms) {
            Double prob = 0D;
            for(Entry<Double,LanguageModel> e:lms){
                prob += e.getKey() * e.getValue().getProb(t);
            }
            this.setProb(t, prob);
        }
    }

}
