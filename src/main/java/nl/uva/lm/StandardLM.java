/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.lm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.html.parser.Entity;
import nl.uva.lucenefacility.IndexInfo;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author Mostafa Dehghani
 */
public class StandardLM extends LanguageModel {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StandardLM.class.getName());
    private IndexReader ireader;
    private String field;
    private IndexInfo iInfo;

    public StandardLM(IndexReader ireader, Integer dId, String field) throws IOException {
        this.ireader = ireader;
        this.field = field;
        this.iInfo = new IndexInfo(this.ireader);
        try {
            generateStandardLanguageModel(dId);
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
    }

    public StandardLM(IndexReader ireader, ArrayList<Integer> docIds, String field) throws IOException {
        this.ireader = ireader;
        ArrayList<Integer> dIds = docIds;
        this.field = field;
        this.iInfo = new IndexInfo(this.ireader);
        try {
            generateStandardLanguageModel(dIds);
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
    }

    private void generateStandardLanguageModel(Integer dId) throws IOException {
        HashMap<String, Double> tv;
        try {
            tv = this.iInfo.getDocTermFreqVector(dId, this.field);
            Long dLength = this.iInfo.getDocumentLength(dId, this.field);
            for (Map.Entry<String, Double> e : tv.entrySet()) {
                Double prob = e.getValue() / dLength;
                tv.put(e.getKey(), prob);
//                tv.put(e.getKey(), e.getValue());
            }
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        this.setModel(tv);
    }

    private void generateStandardLanguageModel(ArrayList<Integer> dIds) throws IOException {
        HashMap<String, Double> tv = new HashMap<>();
        Double Length = 0D;
        try {
            for (int dId : dIds) {
                for (Map.Entry<String, Double> e : this.iInfo.getDocTermFreqVector(dId, this.field).entrySet()) {
                    Double Freq = tv.get(e.getKey());
                    if (Freq == null) {
                        Freq = 0D;
                    }
                    Freq += e.getValue();
                    tv.put(e.getKey(), Freq);
                    Length += e.getValue();
                }
            }
            
            for (Map.Entry<String, Double> e : tv.entrySet()) {
                Double prob = e.getValue() / Length;
                tv.put(e.getKey(), prob);
            }
            
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        this.setModel(tv);
    }
}
