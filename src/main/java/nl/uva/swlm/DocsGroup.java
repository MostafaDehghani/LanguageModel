/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.swlm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import nl.uva.lm.CollectionSLM;
import nl.uva.lm.LanguageModel;
import nl.uva.lm.ParsimoniousLM;
import nl.uva.lm.StandardLM;
import nl.uva.lucenefacility.IndexInfo;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author Mostafa Dehghani
 */
public class DocsGroup {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DocsGroup.class.getName());
    public IndexReader iReader;
    public IndexInfo iInfo;
    public String field;
    public ArrayList<Integer> docs;
    public ArrayList<Double> docsPrior;
    private LanguageModel groupSWLM;
    private LanguageModel groupHSWLM;
    private LanguageModel groupParsimoniouseLM;
    private LanguageModel groupStandardLM;
    private LanguageModel GroupSpecificLM;
    private LanguageModel CollectionLM;
    private HashMap<Integer, HashMap<Integer, Double>> lambdas; //doc -> (mode -> prob)

    public DocsGroup(IndexReader iReader, String field, ArrayList<Integer> docs) {
        this.iReader = iReader;
        this.docs = docs;
        this.field = field;
        this.iInfo = new IndexInfo(this.iReader);
    }

    public LanguageModel getGroupStandardLM() throws IOException {
        if (this.groupStandardLM == null) {
            this.groupStandardLM = new StandardLM(this.iReader, this.docs, this.field);
        }
        return this.groupStandardLM;
    }

    public LanguageModel getGroupSWLM() throws IOException {
        if (this.groupSWLM == null) {
            GroupSWLM gGLM = new GroupSWLM(this);
            this.groupSWLM = new LanguageModel(gGLM.getModel());
        }
        return this.groupSWLM;
    }
    

    public LanguageModel getGroupHSWLM() throws IOException {
        if (this.groupHSWLM == null) {
            GroupHSWLM gHGLM = new GroupHSWLM(this);
            this.groupHSWLM = new LanguageModel(gHGLM.getModel());
        }
        return this.groupHSWLM;
    }
    
    

    public LanguageModel getGroupParsimoniouseLM() throws IOException {
        if (this.groupParsimoniouseLM == null) {
            this.groupParsimoniouseLM = new ParsimoniousLM(this.getGroupStandardLM(), this.getCollectionLM());
//            this.groupParsimoniouseLM = new ParsimoniousLM(this.getGroupStandardLM(), this.getGroupSpecificLM());
        }
        return this.groupParsimoniouseLM;
    }

    public LanguageModel getCollectionLM() throws IOException {
        if (this.CollectionLM == null) {
            this.CollectionLM = new CollectionSLM(this.iReader, this.field);
        }
        return this.CollectionLM;
    }

    
    public LanguageModel getGroupSpecificLM() throws IOException {
        
        if (this.GroupSpecificLM == null) {
            this.GroupSpecificLM = new LanguageModel();

            HashMap<Integer, LanguageModel> docsLMs = new HashMap<>();
            for (int id : this.docs) {
                StandardLM docSLM = new StandardLM(this.iReader, id, this.field);
                docsLMs.put(id, docSLM);
//                StandardLM docSLM = new StandardLM(this.iReader, id, this.field);
//                ParsimoniousLM docPLM = new ParsimoniousLM(docSLM,this.getCollectionLM());
//                docsLMs.put(id, docPLM);
            }
            
            this.GroupSpecificLM.setModel(this.getSpecificLM(this.getGroupStandardLM().getTerms(), docsLMs).getModel());
        }
        return this.GroupSpecificLM;

    }

    public LanguageModel getSpecificLM(Set<String> allTerms, HashMap<Integer, LanguageModel> docsLMs) throws IOException {
        LanguageModel specLM = new LanguageModel();
        for (String term : allTerms) {
            Integer docFreq = 0;
            Double probability = 0D;
            for (int i : docsLMs.keySet()) {
                Double joineProb = docsLMs.get(i).getProb(term);
                if (joineProb > 0) {
                    docFreq++;
                }
                for (int j : docsLMs.keySet()) {
                    if (i == j) {
                        continue;
                    }
                    joineProb = joineProb * (1 - docsLMs.get(j).getProb(term));
                }
                probability += joineProb;
            }
            specLM.setProb(term, probability / docFreq);
        }
        return specLM.getNormalizedLM();
    }
    
    public LanguageModel getGroupSpecificLM_idf() throws IOException {
        if (this.GroupSpecificLM == null) {
            this.GroupSpecificLM = new LanguageModel();
            LanguageModel specLM = new LanguageModel();
            HashMap<Integer, LanguageModel> docsLMs = new HashMap<>();
            for (int id : this.docs) {
                StandardLM docSLM = new StandardLM(this.iReader, id, this.field);
                docsLMs.put(id, docSLM);
            }
            for(String term: this.getGroupStandardLM().getTerms()){
                Double df=0D;
                for (int id : this.docs) {
                    if(docsLMs.get(id).getProb(term)>0)
                        df++;
                }
                specLM.setProb(term, df);
            }
            this.GroupSpecificLM.setModel(specLM.getNormalizedDestribution());
         }
          
        return this.GroupSpecificLM;
    }
    
        public LanguageModel getGroupSpecificLM_Entropy1() throws IOException {
        if (this.GroupSpecificLM == null) {
            this.GroupSpecificLM = new LanguageModel();
            LanguageModel specLM = new LanguageModel();
            HashMap<Integer, LanguageModel> docsLMs = new HashMap<>();
            for (int id : this.docs) {
                StandardLM docSLM = new StandardLM(this.iReader, id, this.field);
                docsLMs.put(id, docSLM);
            }
            for(String term: this.getGroupStandardLM().getTerms()){
                Double entopy=0D;
                for (int id : this.docs) {
                    if(docsLMs.get(id).getProb(term)>0){
                        Double tProb = docsLMs.get(id).getProb(term);
                        entopy += tProb * Math.log(tProb);
                    }
                }
                specLM.setProb(term, -1 * entopy);
            }
            
            this.GroupSpecificLM.setModel(new LanguageModel(specLM.getNormalizedEntrpy()).getNormalizedDestribution());
//            this.GroupSpecificLM.setModel(specLM.getNormalizedDestribution());
         }
        return this.GroupSpecificLM;
    }
    
    
    public LanguageModel getGroupSpecificLM_Entropy2() throws IOException {
        if (this.GroupSpecificLM == null) {
            this.GroupSpecificLM = new LanguageModel();
            
            LanguageModel specLM = new LanguageModel();
            HashMap<Integer, LanguageModel> docsLMs = new HashMap<>();
            HashMap<Integer, LanguageModel> docsLMs_tmp = new HashMap<>();
            for (int id : this.docs) {
                StandardLM docSLM = new StandardLM(this.iReader, id, this.field);
                docsLMs.put(id, docSLM);
            }
            LanguageModel tmpLM = new LanguageModel();
            for (int i : docsLMs.keySet()) {
                for (String term : docsLMs.get(i).getTerms()) {
                        Double joineProb = docsLMs.get(i).getProb(term);
                        for (int j : docsLMs.keySet()) {
                            if (i == j) {
                                continue;
                            }
                            joineProb = joineProb * (1 - docsLMs.get(j).getProb(term));
                        }
                        tmpLM.setProb(term, joineProb);
                    }
                docsLMs_tmp.put(i, tmpLM.getNormalizedLM());
            }
            for(String term: this.getGroupStandardLM().getTerms()){
                Double entopy=0D;
                for (int id : this.docs) {
                    if(docsLMs_tmp.get(id).getProb(term)>0){
                        Double tProb = docsLMs_tmp.get(id).getProb(term);
                        entopy += tProb * Math.log(tProb);
                    }
                }
                specLM.setProb(term, -1 * entopy);
            }
            
            this.GroupSpecificLM.setModel(new LanguageModel(specLM.getNormalizedEntrpy()).getNormalizedDestribution());
//            this.GroupSpecificLM.setModel(specLM.getNormalizedDestribution());
         }
         
        return this.GroupSpecificLM;
    }
    
    public HashMap<Integer, HashMap<Integer, Double>> getGroupLearnedLambdas() throws IOException {
        if (this.lambdas == null) {
            GroupSWLM gGLM = new GroupSWLM(this);
            this.lambdas = gGLM.getLambdas();
        }
        return this.lambdas;
    }
}