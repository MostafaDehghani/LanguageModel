/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.lmoperations;

import java.util.HashSet;
import java.util.Set;
import nl.uva.lm.LanguageModel;

/**
 *
 * @author Mostafa Dehghani
 */
public class ProjectionLM extends LanguageModel {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjectionLM.class.getName());

    private LanguageModel LM1;
    private LanguageModel LM2;
    //projects lm1 on lm2 (lm1>lm2)
    public ProjectionLM(LanguageModel LM1, LanguageModel LM2) {
        this.LM1 = LM1;
        this.LM2 = LM2;
        this.ProjectLm1onLm2();
    }
    
    public void ProjectLm1onLm2() {
        Set<String> terms = LM2.getTerms();
        for (String t : LM1.getTerms()) {
            if(terms.contains(t)){
                this.setProb(t, LM1.getProb(t));
            }
        }
        this.Normalized();
    }
}
