/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.generalinzedlm;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import nl.uva.lm.LanguageModel;
import nl.uva.lm.ParsimoniousLM;
import nl.uva.lm.StandardLM;

/**
 *
 * @author Mostafa Dehghani
 */
public class GroupHGLM extends LanguageModel { //p(theta_r|t)

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupHGLM.class.getName());


    private HashMap<Integer, LanguageModel> docsSLM;
    private HashMap<Integer, LanguageModel> docsHPLM;
    private LanguageModel groupHPLM;
    private LanguageModel groupHGLM;
    private DocsGroup group;

    public GroupHGLM(DocsGroup group) throws IOException {
        this.group = group;

        
        this.docsSLM = new HashMap<>();
        for (int id : this.group.docs) {
            StandardLM slm = new StandardLM(this.group.iReader, id, this.group.field);
            this.docsSLM.put(id, slm);
        }
        
        this.docsHPLM = new HashMap<>();
        for (int id : this.group.docs) {
            ParsimoniousLM  hplm = new ParsimoniousLM(this.docsSLM.get(id),this.group.getCollectionLM());
            hplm = new ParsimoniousLM(hplm,this.group.getGroupStandardLM());
            this.docsHPLM.put(id, hplm);
        }
        
        this.groupHPLM = new ParsimoniousLM(this.group.getGroupStandardLM(),this.group.getCollectionLM());
//        for (int id : this.group.docs) {
//            this.HGLM  = new ParsimoniousLM(this.HGLM,this.docsHPLM.get(id));
//        }
        Set<String> allDocTerms = new HashSet<String>();
        for(LanguageModel dlm: this.docsHPLM.values())
            allDocTerms.addAll(dlm.getTerms());
        this.groupHGLM  = new ParsimoniousLM(this.groupHPLM,this.group.getSpecificLM(allDocTerms,docsHPLM));
        
        this.setModel(this.groupHGLM.getModel());
    }
}
