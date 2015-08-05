package nl.uva.lm;

import java.util.HashMap;
import java.util.Map.Entry;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Mostafa Dehghani
 */
public final class ParsimoniousLM extends LanguageModel {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ParsimoniousLM.class.getName());
    private LanguageModel backgroundLM;
    private LanguageModel documentLM;
    private HashMap<String, Double> documentTV;
    private LanguageModel tmpLM;
    private Double alpha = 0.05D;
    private Double probThreshold = 1e-5; //0.00001D;
    private Integer numberOfIttereation = 100;

    public ParsimoniousLM(LanguageModel documentLM, HashMap<String, Double> documentTV, LanguageModel backgroundLM,
            Double alpha, Double probThreshold, Integer numberOfIttereation) {
        this.backgroundLM = backgroundLM;
        this.documentLM = documentLM;
        this.documentTV = documentTV;
        this.tmpLM = documentLM;
        this.alpha = alpha;
        this.probThreshold = probThreshold;
        this.numberOfIttereation = numberOfIttereation;
        this.generateParsimoniousLanguageModel();
    }

     public ParsimoniousLM(LanguageModel documentLM, HashMap<String, Double> documentTV, LanguageModel backgroundLM) {
        this.backgroundLM = backgroundLM;
        this.documentLM = documentLM;
        this.documentTV = documentTV;
        this.tmpLM = documentLM;
        this.generateParsimoniousLanguageModel();
    }
     
    public ParsimoniousLM(LanguageModel documentLM, LanguageModel backgroundLM,
        Double alpha, Double probThreshold, Integer numberOfIttereation) {
        this.backgroundLM = backgroundLM;
        this.documentLM = documentLM;
        this.documentTV = documentLM.getModel();
        this.tmpLM = documentLM;
        this.alpha = alpha;
        this.probThreshold = probThreshold;
        this.numberOfIttereation = numberOfIttereation;
        this.generateParsimoniousLanguageModel();
    }

    public ParsimoniousLM(LanguageModel documentLM, LanguageModel backgroundLM) {
        this.backgroundLM = backgroundLM;
        this.documentLM = documentLM;
        this.documentTV = documentLM.getModel();
        this.tmpLM = documentLM;
        this.generateParsimoniousLanguageModel();
    }

    private void E_step(Double alpha) {
        for (Entry<String, Double> e : this.tmpLM.getEntrySet()) {
            Double backgoundProb = null;
                backgoundProb = this.backgroundLM.getProb(e.getKey());
            if (backgoundProb == null) {
                backgoundProb = 0D;
            }
            Double tf = documentTV.get(e.getKey());
                Double newProb = tf * ((alpha * e.getValue()) / ((alpha * e.getValue()) + ((1 - alpha) * backgoundProb)));
                this.setProb(e.getKey(), newProb);
        }
        this.tmpLM = new LanguageModel(this.getModel());
    }

    private void M_step(Double probThreshold) {
        Double summation = 0D;
        for (Entry<String, Double> e : this.tmpLM.getEntrySet()) {
            summation += e.getValue();
        }
        for (Entry<String, Double> e : this.tmpLM.getEntrySet()) {
            Double newProb = e.getValue() / summation;
            if (newProb <= probThreshold) {
                this.removeTerm(e.getKey());
            } else {
                this.setProb(e.getKey(), newProb);
            }
        }
        this.setModel(this.getNormalizedDestribution());
        this.tmpLM = new LanguageModel(this.getModel());
    }

    public void generateParsimoniousLanguageModel() {
        
        for (int i = 0; i < this.numberOfIttereation; i++) {
            this.E_step(this.alpha);
            this.M_step(this.probThreshold);
        }
    }
    

}