/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.lm;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import nl.uva.lucenefacility.IndexInfo;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author Mostafa Dehghani
 */
public class CollectionSLM extends LanguageModel {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CollectionSLM.class.getName());
    private IndexReader ireader;
    private String field;
    private IndexInfo iInfo;

    public CollectionSLM(IndexReader ireader, String field) throws IOException {
        this.ireader = ireader;
        this.field = field;
        this.iInfo = new IndexInfo(this.ireader);
        try {
            generateCollectionLanguageModel();
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
    }

    public void generateCollectionLanguageModel() throws IOException {
        HashMap<String, Double> tv = new HashMap<>();
        try {
            HashSet<String> allTerms = this.iInfo.getAllTerms(this.field);
            Long cLength = this.iInfo.getNumOfAllTerms(this.field);
            for (String term : allTerms) {
                Double prob = this.iInfo.getTotalTF_PerField(this.field, term).doubleValue() / cLength;
                tv.put(term, prob);
            }
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        this.setModel(tv);
    }

}
