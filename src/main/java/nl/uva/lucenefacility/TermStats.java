package nl.uva.lucenefacility;

import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Mostafa Dehghani
 */
public final class TermStats {

    public BytesRef termtext;
    public String field;
    public int docFreq;
    public long totalTermFreq;

    TermStats(String field, BytesRef termtext, int df) {
        this.termtext = (BytesRef) termtext.clone();
        this.field = field;
        this.docFreq = df;
    }

    TermStats(String field, BytesRef termtext, int df, long tf) {
        this.termtext = (BytesRef) termtext.clone();
        this.field = field;
        this.docFreq = df;
        this.totalTermFreq = tf;
    }

    String getTermText() {
        return termtext.utf8ToString();
    }

    public String toString() {
        return field + ":" + termtext.utf8ToString() + ":" + docFreq + ":" + totalTermFreq;
    }
}
