package nl.uva.lucenefacility;

import java.io.IOException;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.PriorityQueue;

/**
 *
 * @author Mostafa Dehghani
 *
 * <code>HighFreqTerms</code> class extracts the top n most frequent terms (by
 * document frequency or by total term frequency) from an existing Lucene.
 */
public class HighFreqTerms {

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HighFreqTerms.class.getName());
    private final TermStats[] EMPTY_STATS = new TermStats[0];
    private IndexReader ireader = null;

    /**
     * Constructor
     *
     * @param ireader IndexReader instance that determines the target index
     */
    public HighFreqTerms(IndexReader ireader) {
        this.ireader = ireader;
    }

    /**
     *
     * @param numTerms a threshold for determining size of output list
     * @param fieldName name of the index field which is desired
     * @return TermStats[] ordered by terms with highest docFreq first.
     */
    public TermStats[] getHighDFTerms(int numTerms, String fieldName) {
        TermStatsDFQueue tiq = null;
        TermsEnum te = null;
        try {
            tiq = new TermStatsDFQueue(numTerms);
            Terms terms = MultiFields.getTerms(ireader, fieldName);
            if (terms != null) {
                te = terms.iterator(); //.iterator(te);
                this.fillQueue(te, tiq, fieldName);
            }
        } catch (IOException ex) {
            log.error(ex);
        } catch (Exception ex) {
            log.error(ex);
        }
        TermStats[] result = new TermStats[tiq.size()];
        // we want highest first so we read the queue and populate the array
        // starting at the end and work backwards
        int count = tiq.size() - 1;
        while (tiq.size() != 0) {
            result[count] = tiq.pop();
            count--;
        }
        return result;
    }

    /**
     *
     * @param numTerms a threshold for determining size of output list
     * @param fieldName name of the index field which is desired
     * @return TermStats[] ordered by terms with highest docFreq first.
     */
    public TermStats[] getHighTFTerms(int numTerms, String fieldName) {
        TermStatsTFQueue tiq = null;
        TermsEnum te = null;
        try {
//            Fields fields = MultiFields.getFields(this.ireader);
            tiq = new TermStatsTFQueue(numTerms);
//            Iterator<String> fieldIterator = fields.iterator();
//            while (fieldIterator.hasNext()) {
//                String fieldName = fieldIterator.next();
//            Terms terms = fields.terms(fieldName);
            Terms terms = MultiFields.getTerms(ireader, fieldName);
            if (terms != null) {
                te = terms.iterator();//iterator(te);
                this.fillQueue(te, tiq, fieldName);
            }
//            } 
        } catch (IOException ex) {
            log.error(ex);
        } catch (Exception ex) {
            log.error(ex);
        }
        TermStats[] result = new TermStats[tiq.size()];
        int count = tiq.size() - 1;
        while (tiq.size() != 0) {
            result[count] = tiq.pop();
            count--;
        }
        return result;
    }

    /**
     * <code>getTotalTF_PerField</code> calculate total term frequency for the
     * given term in the given field of index
     *
     * @param field name of the index field which is desired
     * @param term
     * @return
     */
    private Long getTotalTF_PerField(String field, BytesRef text) {
        Long TF = 0L;
        Term term = new Term(field, text);
        try {
            TF = this.ireader.totalTermFreq(term);
        } catch (IOException ex) {
            log.error(ex);
        }
        return TF;
    }

    /**
     *
     * <code>fillQueue</code> is a function that fill given priority queue with
     * given object
     *
     * @param termsEnum term enumerator that contains the terms those should be
     * pushed to the given queue
     * @param tiq the priority queue
     * @param field name of the index field which terms belong to
     * @throws Exception
     */
    public void fillQueue(TermsEnum termsEnum, PriorityQueue tiq, String field) throws Exception {
        BytesRef term;
        while ((term = termsEnum.next()) != null) {
            BytesRef r = new BytesRef();
            r = term.clone(); //.copyBytes(term);
            tiq.insertWithOverflow(new TermStats(field, r, termsEnum.docFreq(), this.getTotalTF_PerField(field, term)));

        }
    }
}

/**
 * Priority queue for TermStats objects ordered by docFreq
 *
 */
final class TermStatsDFQueue extends PriorityQueue<TermStats> {

    TermStatsDFQueue(int size) {
        super(size);
    }

    @Override
    protected boolean lessThan(TermStats termInfoA, TermStats termInfoB) {
        return termInfoA.docFreq < termInfoB.docFreq;
    }
}

/**
 * Priority queue for TermStats objects ordered by termFreq
 *
 */
final class TermStatsTFQueue extends PriorityQueue<TermStats> {

    TermStatsTFQueue(int size) {
        super(size);
    }

    @Override
    protected boolean lessThan(TermStats termInfoA, TermStats termInfoB) {
        return termInfoA.totalTermFreq < termInfoB.totalTermFreq;
    }
}
