/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.swlm;

import java.io.IOException;
import java.util.HashMap;
import nl.uva.lm.LanguageModel;

/**
 *
 * @author Mostafa Dehghani
 */
public class GroupSWLM_FixedLambdas extends LanguageModel { //p(theta_r|t)

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupSWLM_FixedLambdas.class.getName());

    private final Integer relavanceModelIndex = 0;

    private final HashMap<Integer, LanguageModel> docsTermVectors;

    //For each Model, for each document, for each term
    private HashMap<Integer, HashMap<Integer, LanguageModel>> modelSelectionProb; // p(X_{d,t} = x )| x \in {s,g,r}
    //Note: Language Model here does not mean a probabilistic Language Model, it is only uses as the data structure

    //For each model, for each document
    private HashMap<Integer, HashMap<Integer, Double>> lambda_X_d; // Lambda_x = p(\theta_x|d) --> Coefficient of each model in each document

    //For each model, for each term
    private HashMap<Integer, LanguageModel> models;

    private final DocsGroup group;
    private final Integer numberOfItereation = 100;
//
//    public GroupSWLM_FixedLambdas(DocsGroup group) throws IOException {
//        this(group, 0.0002D, 0.4999D, 0.4999D);
//    }

public GroupSWLM_FixedLambdas(DocsGroup group, Double lambda_r, Double lambda_g, Double lambda_s) throws IOException {
        this.group = group;

        //Fetcing termVectors from index
        this.docsTermVectors = new HashMap<>();
        for (int id : this.group.docs) {
            LanguageModel tv = new LanguageModel(group.iInfo.getDocTermFreqVector(id, this.group.field));
            this.docsTermVectors.put(id, tv);
        }

        //Initializing Models:
        this.models = new HashMap<>();
        //Relavance Model
        this.models.put(this.relavanceModelIndex,this.group.getGroupStandardLM().cloneModel());
        //General Model
        this.models.put(1,this.group.getGeneralLM());
        //Specific Model
        this.models.put(2,this.group.getGroupSpecificLM());

        //Initializing  lambda_X_d
        lambda_X_d = new HashMap<>();
        for (Integer m : models.keySet()) {
            HashMap<Integer, Double> docsHM = new HashMap<>();
            for (int id : group.docs) {
                if(m==0) //Relavance Model
                    docsHM.put(id,lambda_r );
                if(m==1)  //General Model
                    docsHM.put(id, lambda_g);
                if(m==2) //Specific Model
                    docsHM.put(id, lambda_s);
            }
            lambda_X_d.put(m, docsHM);
        }
//        log.info("Lambdas' value are initialized....");

        //Caculate GLM
        this.CalculateGLM();
    }


    private void E_step() {
        this.modelSelectionProb = new HashMap<>();
        for (Integer m : models.keySet()) {
            HashMap<Integer, LanguageModel> docsHM = new HashMap<>();
            for (Integer id : this.group.docs) {
                LanguageModel lm = new LanguageModel();
                Double lambda = this.lambda_X_d.get(m).get(id);
                if(lambda == null)
                       System.out.println("nulllll");
                for (String term : this.docsTermVectors.get(id).getTerms()) {
                    Double prob = this.models.get(m).getProb(term);
                        Double selectionProb = prob * lambda / this.Get_E_step_denominator(id, term);
                        lm.setProb(term, selectionProb);
                }
                docsHM.put(id, lm);
            }
            modelSelectionProb.put(m, docsHM);
        }
    }

    private Double Get_E_step_denominator(Integer did, String term) {
        Double denominator = 0D;
        for (Integer m : models.keySet()) {
            Double prob = this.models.get(m).getProb(term);
            Double lambda = this.lambda_X_d.get(m).get(did);
            denominator += prob * lambda;
        }
        return denominator;
    }

    private void M_step() {

//        this.lambda_X_d = new HashMap<>(); //Clear Matrix
        Integer modelToBeUpdate = this.relavanceModelIndex;
        this.models.get(modelToBeUpdate).erase();
        Double denominator_relModel = null;
        for (Integer m : models.keySet()) {
            if (m.equals(modelToBeUpdate)) {
                denominator_relModel = this.Get_M_step_denominator_relModel(modelToBeUpdate);
            }
            HashMap<Integer, LanguageModel> docsHM = this.modelSelectionProb.get(m);
            for (Integer id : docsHM.keySet()) {
                for (String term : docsHM.get(id).getTerms()) {
                    // Updating relevance Model
                    if (m.equals(modelToBeUpdate)) {
                        Double newProb = this.Get_M_step_numerator_relModel(m, term) / denominator_relModel;
                        if(newProb > 0)
                            this.models.get(m).setProb(term, newProb);
                    }
                }
            }
        }
    }

    private Double Get_M_step_numerator_relModel(Integer m, String term) {
        Double numerator = 0D;
        HashMap<Integer, LanguageModel> docsHM = this.modelSelectionProb.get(m);
        for (Integer id : docsHM.keySet()) {
            Double tf = this.docsTermVectors.get(id).getProb(term);
            numerator += tf * docsHM.get(id).getProb(term);
        }
        return numerator;
    }


    private Double Get_M_step_denominator_relModel(Integer m) {
        Double denominator = 0D;
        HashMap<Integer, LanguageModel> docsHM = this.modelSelectionProb.get(m);
        for (Integer id : docsHM.keySet()) {
            for (String term : docsHM.get(id).getTerms()) {
                Double tf = this.docsTermVectors.get(id).getProb(term);
                denominator += tf * docsHM.get(id).getProb(term);
            }
        }
        return denominator;
    }


    public void CalculateGLM() {
        for (int i = 0; i < this.numberOfItereation; i++) {
//                System.out.println("iteration num:" + i);
//                System.out.println(this.models.get(this.relavanceModelIndex).getTopK(20));
                this.E_step();
                this.M_step();
            }
        this.setModel(this.models.get(this.relavanceModelIndex).getModel());
    }
    
    public HashMap<Integer, HashMap<Integer, Double>> getLambdas() {
        //Change axises of lambda matrix 
        HashMap<Integer, HashMap<Integer, Double>> lambdas = new HashMap<>();
        for (Integer m : models.keySet()) {
            for (int dId : lambda_X_d.get(m).keySet()) {
                HashMap<Integer, Double> ls = lambdas.get(dId);
                if(ls==null){
                    ls = new HashMap<>();
                }
                ls.put(m,lambda_X_d.get(m).get(dId));
                lambdas.put(dId, ls);
            }
        }
        return lambdas;
    }
}
