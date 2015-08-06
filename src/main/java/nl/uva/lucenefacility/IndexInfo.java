package nl.uva.lucenefacility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Mostafa Dehghani
 *
 * <code>IndexInfo</code> is a class which provide whit methods that extract
 * information about the terms and document from a lucene index Note: this class
 * is compatible with Lucene 4.9 index
 */
public class IndexInfo {

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndexInfo.class.getName());
    private IndexReader ireader = null;
    private String IndexPath = null;
    private Long numOfAllTerms = -1L;
    private Long numOfAllUniqTerms = -1L;
    private Double AvgDocLength = 0D;
    private TermStats[] topTermsDF = null;
    private TermStats[] topTermsTF = null;
    private List<String> fieldNames = null;
    private HashMap<String, Long> termCounts = null;
    private HashMap<String, Integer> indexId = null;

    /**
     * Constructor setting the path of index directory
     *
     * @param IndexPath path of the index directory
     */
    public IndexInfo(IndexReader ireader) {
        fieldNames = new ArrayList<String>();
        numOfAllTerms = -1L;
        AvgDocLength = -1D;
        this.ireader = ireader;
        Collections.sort(fieldNames);
    }

    /**
     *
     * <code>getTotalTF_OverAllFields</code> extracts term frequency of the
     * given Lucene term over all index fields. NOTE: the given term should be
     * analyzed (e.g. stemming) before.
     *
     * @param text
     * @return term frequency of given term over all index field
     */
    public Long getTotalTF_OverAllFields(String text) throws IOException {
        Long totalTF = 0L;
        try {
            Fields fields = MultiFields.getFields(this.ireader);
            Iterator<String> fieldIterator = fields.iterator();
            while (fieldIterator.hasNext()) {
                String fieldName = fieldIterator.next();
                //
                Term term = new Term(fieldName, text);
                totalTF += this.ireader.totalTermFreq(term);
                /*    
                 final Terms terms = MultiFields.getTerms(ireader,field);
                 if (terms != null) {
                 final TermsEnum termsEnum = terms.iterator(null);
                 if (termsEnum.seekExact(text)) {
                 return termsEnum.totalTermFreq();
                 }
                 }*/
            }
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        return totalTF;
    }

    /**
     *
     * <code>etTotalTF_PerField</code> extracts term frequency of the given
     * Lucene term for the given index field. NOTE: the given term should be
     * analyzed (e.g. stemming) before.
     *
     * @param field field's name
     * @param text
     * @return term frequency of given term over the given field
     */
    public Long getTotalTF_PerField(String field, String text) throws IOException {
        Long TF = 0L;
        Term term = new Term(field, text);
        try {
            TF = this.ireader.totalTermFreq(term);
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        return TF;
    }

    /**
     *
     * <code>getTF</code> extract the term frequency of the given term in the
     * given field of a specific document
     *
     * @param text
     * @param docId
     * @param field field's name
     * @return term frequency of the given term in the given field of a specific
     * document
     */
    public Integer getTF(String field, BytesRef text, Integer docId) throws IOException {
        Integer TF = 0;
        try {
            Terms terms = ireader.getTermVector(docId, field);
            if (terms != null && terms.size() > 0) {
                TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                termsEnum.seekExact(text);
                DocsEnum docsEnum = termsEnum.docs(null, null); // enumerate through documents, in this case only one
                int docIdEnum;
                while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                    TF = docsEnum.freq();
                }
            }

        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        return TF;
    }

    /**
     *
     * <code>getDF</code> extracts the document frequency of the given term in
     * the given field
     *
     * @param field field's name
     * @param text
     * @return document frequency of the given term in the given field
     */
    public Integer getDF(String field, BytesRef text) throws IOException {
        Integer DF = 0;
        Term term = new Term(field, text);
        try {
            DF = ireader.docFreq(term);
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        return DF;
    }

    /**
     *
     * <code>getDocumentLength</code> extracts the length of the given field of
     * the given document
     *
     * @param docId
     * @param field field's name
     * @return length of the given field the given document
     */
    public Long getDocumentLength(int docId, String field) throws IOException {
        Long dLenght = 0L;
        try {
            Terms terms = ireader.getTermVector(docId, field);
            if (terms != null && terms.size() > 0) {
                TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                BytesRef t = null;
                while ((t = termsEnum.next()) != null) {// explore the terms for this field
                    DocsEnum docsEnum = termsEnum.docs(null, null); // enumerate through documents, in this case only one
                    int docIdEnum;
                    while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        dLenght += docsEnum.freq();
                    }
                }
            }

        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        return dLenght;
    }

    /**
     *
     * <code>getNumberofUniqTermsInDocument</code> gets number of unique terms
     * in the given field of the given document
     *
     * @param docId
     * @param field field's name
     * @return
     */
    public Long getNumberofUniqTermsInDocument(int docId, String field) throws IOException {
        Long udl = 0L;
        try {
            Terms terms = ireader.getTermVector(docId, field);
            udl = terms.size();
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        return udl;
    }

    /**
     * <code>getNumOfAllTerms</code> gets number of all terms in the given field
     * of index (summation of all document's length)
     *
     * @param field field's name
     * @return number of all terms in the given field of index
     */
    public Long getNumOfAllTerms(String field) throws IOException {
        if (numOfAllTerms == -1) {
            try {
                numOfAllTerms = ireader.getSumTotalTermFreq(field);
            } catch (IOException ex) {
                log.error(ex);
                throw ex;
            }
        }
        return numOfAllTerms;
    }

    /**
     *
     * <code>getAvgDocLength</code> calculates average length of the given
     * fields over all documents
     *
     * @param field field's name
     * @return average length of the given fields over all documents
     */
    public Double getAvgDocLength(String field) throws IOException {
        if (AvgDocLength == -1) {
            Long sum = 0L;
            sum += this.getNumOfAllTerms(field);
            double avgDocLength = sum / (double) ireader.numDocs();
            AvgDocLength = avgDocLength;
        }
        return AvgDocLength;
    }

    /**
     * <code>getNumOfAllUniqueTerms_PerField</code> gets number of unique terms
     * in the given field of index
     *
     * @param field field's name
     * @return number of unique terms in the given field of index
     */
    public Long getNumOfAllUniqueTerms_PerField(String field) throws IOException {
        if (termCounts == null) {
            countTerms();
        }
        return termCounts.get(field);
    }

    /**
     * <code>getNumOfAllUniqueTerms_OverAllFields</code> gets number of unique
     * terms over all fields of index
     *
     * @return number of unique terms over all fields of index
     */
    public Long getNumOfAllUniqueTerms_OverAllFields() throws IOException {
        if (numOfAllUniqTerms == -1) {
            countTerms();
        }
        return numOfAllUniqTerms;
    }

    private void countTerms() throws IOException {
        try {
            termCounts = new HashMap<String, Long>();
            numOfAllUniqTerms = 0L;
            Fields fields = MultiFields.getFields(ireader);
            // if there are no postings, throw an exception
            if (fields == null) {
                log.warn("There are no postings in the index reader.");
            }
            Iterator<String> fe = fields.iterator();
            TermsEnum te = null;
            while (fe.hasNext()) {
                String fld = fe.next();
                Long termCount = 0L;
                Terms terms = fields.terms(fld);
                if (terms != null) { // count terms
                    te = terms.iterator();
                    while (te.next() != null) {
                        termCount++;
                        numOfAllUniqTerms++;
                    }
                }
                termCounts.put(fld, termCount);
            }
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
    }

    /**
     *
     * <code>getTopTerms_DF</code> extracts top k most frequent terms in the
     * given field of index in terms of Document Frequency
     *
     * @param field field's name
     * @param numTerms Threshold for number of terms in the output list
     * @return top k most frequent terms in the given field of index in terms of
     * Document Frequency
     */
    public ArrayList<String> getTopTerms_DF(String field, Integer numTerms) {
        ArrayList<String> top_DF = new ArrayList<>();
        if (topTermsDF == null) {
            HighFreqTerms HIT = new HighFreqTerms(ireader);
            topTermsDF = HIT.getHighDFTerms(numTerms, field);
        }
        for (TermStats ts : topTermsDF) {
            top_DF.add(ts.getTermText());
        }
        return top_DF;
    }

    /**
     *
     * <code>getTopTerms_DF</code> extracts top k most frequent terms in the
     * given field of index in terms of Term Frequency
     *
     * @param field field's name
     * @param numTerms Threshold for number of terms in the output list
     * @return top k most frequent terms in the given field of index in terms of
     * Term Frequency
     */
    public ArrayList<String> getTopTerms_TF(String field, Integer numTerms) {
        ArrayList<String> top_TF = new ArrayList<>();
        if (topTermsTF == null) {
            HighFreqTerms HIT = new HighFreqTerms(ireader);
            topTermsTF = HIT.getHighTFTerms(numTerms, field);
        }
        for (TermStats ts : topTermsTF) {
            top_TF.add(ts.getTermText());
        }
        return top_TF;
    }

    /**
     * <code>getAllTerms</code> extracts all terms in the given field.
     *
     * @param field field's name
     * @return all terms in the given field
     */
    public HashSet<String> getAllTerms(String field) throws IOException {
        HashSet<String> allTerms = new HashSet<>();
        try {
            TermsEnum te = null;
            Terms terms = MultiFields.getTerms(ireader, field);
            if (terms != null) {
                te = terms.iterator();
                BytesRef term;
                while ((term = te.next()) != null) {
                    BytesRef r = new BytesRef();
                    r = term.clone();
                    allTerms.add(r.utf8ToString());
                }
            }
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        return allTerms;
    }

    /**
     * <code>getIndexId</code> gets the integer lucene index id given the
     * external string id of the document
     *
     * @param docID
     * @return
     * @throws IOException
     */
    public Integer getIndexId(String docID) throws IOException {
        if (indexId == null) {
            this.indexId = new HashMap<>();
            for (int i = 0; i < this.ireader.numDocs(); i++) {
                try {
                    this.indexId.put(ireader.document(i).get("ID"), i);
                } catch (IOException ex) {
                    log.error(ex);
                    throw ex;
                }
            }

        }
        return this.indexId.get(docID);

//        TermsEnum te = MultiFields.getTerms(this.ireader, "ID").iterator(null);
//        BytesRef id = new BytesRef(docID);
//        te.seekExact(id);
//        DocsEnum docsEnum = te.docs(null, null);
//        int docIdEnum;
//        while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
//            return docIdEnum;
//        }
    }

    public HashMap<String, Double> getDocTermFreqVector(Integer docId, String field) throws IOException {
        HashMap<String, Double> TV = new HashMap<>();
        try {
            Terms vector = ireader.getTermVector(docId, field);
            if (vector != null && vector.size() > 0) {
                TermsEnum te = null;
                te = vector.iterator();
                BytesRef text = null;
                while ((text = te.next()) != null) {
                    String term = text.utf8ToString();
                    Long freq = te.totalTermFreq();
                    TV.put(term, freq.doubleValue());
                }
            }
//            Terms terms = this.ireader.getTermVector(docId,field); //get terms vectors for one document and one field
//            if (terms != null && terms.size() > 0) {
//                TermsEnum tEnum = terms.iterator(null); // access the terms for this field
//                BytesRef term = null;
//                while ((term = termsEnum.next()) != null) {// explore the terms for this field
//                    DocsEnum docsEnum = termsEnum.docs(null, null); // enumerate through documents, in this case only one
//                    int docIdEnum;
//                    while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
//                        TV.put(term.utf8ToString(), docsEnum.freq());
//                    }
//                }
//            }
        } catch (IOException ex) {
            log.error(docId);
            log.error(ex);
            throw ex;
        }
        return TV;
    }

    public HashSet<String> getDocAllTerm(Integer docId, String field) throws IOException {
        HashSet<String> T = new HashSet<>();
        try {
            Terms vector = ireader.getTermVector(docId, field);
            if (vector != null && vector.size() > 0) {
                TermsEnum te = null;
                te = vector.iterator();
                BytesRef text = null;
                while ((text = te.next()) != null) {
                    String term = text.utf8ToString();
                    T.add(term);
                }
            }
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        return T;
    }

//    public ArrayList<Integer> getHitDocs(Term term){
//        ArrayList<Integer> docs = new ArrayList<>();
//        this.ireader.
//        return docs;
//    }
}
