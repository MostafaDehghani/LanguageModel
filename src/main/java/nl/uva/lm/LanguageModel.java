/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.lm;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Mostafa Dehghani
 */
public class LanguageModel {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LanguageModel.class.getName());
    private HashMap<String, Double> LanguageModel;

    public LanguageModel() {
        LanguageModel = new HashMap<>();
    }

    public LanguageModel(HashMap<String, Double> LM) {
        this.LanguageModel = LM;
    }
    
    public String getProb(String term, Integer round){
       Double prob = LanguageModel.get(term);
       if(prob == null)
           prob = 0D;
       if(round<0)
           return prob.toString();
       return String.format("%."+ round + "f", prob);
    }
    
    public Double getProb(String term){
       Double prob = LanguageModel.get(term);
       if(prob == null)
           prob = 0D;
       return  prob;
    }
    
    public void setProb(String term, Double prob){
       this.LanguageModel.put(term, prob);
    }
    
    public void removeTerm(String term){
        this.LanguageModel.remove(term);
    }
    
    public void setModel(HashMap<String, Double> lm){
        this.LanguageModel = lm;
    }
    
    public HashMap<String, Double> getModel(){
        return new HashMap<String, Double>(this.LanguageModel);
    }
    
   public LanguageModel cloneModel(){
        return new LanguageModel(this.LanguageModel);
    }

    public Integer getSize(){
        return this.LanguageModel.size();
    }
    
    public Set<String> getTerms(){
        return this.LanguageModel.keySet();
    }
    
    public Set<Entry<String,Double>> getEntrySet(){
        return this.LanguageModel.entrySet();
    } 
    
   

    public static List<Map.Entry<String, Double>> sortByValues(Map<String, Double> unsortMap, final boolean order) {
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                    Map.Entry<String, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

//        // Maintaining insertion order with the help of LinkedList
//        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
//        for (Entry<String, Double> entry : list)
//        {
//            sortedMap.put(entry.getKey(), entry.getValue());
//        }
//        return sortedMap;
        return list;
    }

    public List<Map.Entry<String, Double>> getTopK(Integer k) {
        List<Map.Entry<String, Double>> sorted = sortByValues(LanguageModel, false);
        k = k < sorted.size() ? k : sorted.size();
        return sorted.subList(0, k);
    }

    public List<Map.Entry<String, Double>> getNormalizedTopK(Integer k) {
        List<Map.Entry<String, Double>> sorted = sortByValues(LanguageModel, false);
        List<Map.Entry<String, Double>> newList = new ArrayList<>();
        k = k < sorted.size() ? k : sorted.size();
        Double summation = 0D;
        for (Map.Entry<String, Double> e : sorted.subList(0, k)) {
            summation += e.getValue();
        }
        for (Map.Entry<String, Double> e : sorted.subList(0, k)) {
            Double newProb = e.getValue() / summation;
            newList.add(new AbstractMap.SimpleEntry<>(e.getKey(), newProb));
        }
        return newList;
    }
    
    public HashMap<String, Double> getNormalizedDestribution() {
        Double summation = 0D;
        HashMap<String, Double> newLM = new HashMap<>();
        for (Map.Entry<String, Double> e : this.LanguageModel.entrySet()) {
            summation += e.getValue();
        }
        for (Map.Entry<String, Double> e :this.LanguageModel.entrySet() ) {
            Double newProb = e.getValue() / summation;
            newLM.put(e.getKey(), newProb);
        }
        return newLM;
    }
    

    public HashMap<String, Double> getNormalizedEntrpy() {
        Double summation = 0D;
        HashMap<String, Double> newLM = new HashMap<>();
        for (Map.Entry<String, Double> e : this.LanguageModel.entrySet()) {
            summation += e.getValue();
        }
        for (Map.Entry<String, Double> e :this.LanguageModel.entrySet() ) {
            Double newProb = e.getValue() / summation;
            newLM.put(e.getKey(), (1-newProb));
        }
        return newLM;
    }
    
    public LanguageModel getNormalizedLM(){
        Double summation = 0D;
        LanguageModel newLM = new LanguageModel();
        for (Map.Entry<String, Double> e : this.LanguageModel.entrySet()) {
            summation += e.getValue();
        }
        for (Map.Entry<String, Double> e :this.LanguageModel.entrySet() ) {
            Double newProb = e.getValue() / summation;
            newLM.setProb(e.getKey(), newProb);
        }
        return newLM;
    }

    public List<Map.Entry<String, Double>> getSorted() {
        List<Map.Entry<String, Double>> sorted = sortByValues(LanguageModel, false);
        return sorted;
    }


    public void erase() {
        this.LanguageModel = new HashMap<>();
    }

}
