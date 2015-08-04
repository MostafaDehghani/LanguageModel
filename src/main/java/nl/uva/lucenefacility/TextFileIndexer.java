/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.lucenefacility;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static nl.uva.settings.Config.configFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 *
 * @author Mostafa Dehghani
 */
public class TextFileIndexer extends Indexer {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TextFileIndexer.class.getName());

    public TextFileIndexer() throws Exception, Throwable {
        super();
    }

    @Override
    protected void docIndexer() throws Exception {

        try {
            String CorpusPathString = configFile.getProperty("FILES_PATHS");
            File root = new File(CorpusPathString);
            List<File> files = (List<File>) FileUtils.listFiles(root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File file : files) {
                if(file.getName().equals(".DS_Store"))
                    continue;
                TextFile tFile = new TextFile(file, root);
                this.IndexDoc(tFile);
            }
        } catch (Exception ex) {
            log.error(ex);
            throw ex;
        }
    }

    @Override
    protected void analyzerMapInitializer(Map<String, Analyzer> analyzerMap) {
//        analyzerMap.put("ID", new KeywordAnalyzer());//StandardAnalyzer(Version.LUCENE_CURRENT));
        analyzerMap.put("ID", new MyAnalyzer(Boolean.FALSE).ArbitraryCharacterAsDelimiterAnalyzer('/'));
//        analyzerMap.put("PATH", new MyAnalyzer(Boolean.FALSE).ArbitraryCharacterAsDelimiterAnalyzer('/'));
    }

    @Override
    protected void IndexDoc(Object obj) throws Exception {
        TextFile tf = (TextFile) obj;
        Document doc = new Document();
//        Integer fileLength = tf.Content.split("\\s+").length;
//        if (fileLength <= minDocLength) //Filtering small documents
//        {
//            log.info("File " + tf.PathFromRoot + "is skeeped due to min length constraint: File Length=" + fileLength );
//            return;
//        }
        doc.add(new Field("ID", tf.PathFromRoot, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
//        doc.add(new Field("PATH", tf.PathFromRoot, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
        doc.add(new Field("TEXT", tf.Content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
        try {
            writer.addDocument(doc);
        } catch (IOException ex) {
            log.error(ex);
        }
        log.info("Document " + tf.PathFromRoot + " has been indexed successfully...");
}

    public static void main(String[] args) throws Exception, Throwable {
        TextFileIndexer di = new TextFileIndexer();
    }
}

class TextFile{

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TextFile.class.getName());
    public String FileName;
    public String PathFromRoot;
    public String Content;

    public TextFile(File file, File root) throws IOException {
        this.FileName = file.getName();
        this.PathFromRoot = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(root.getName()));
        this.Content = readFileAsString(file);
    }    
    public static String readFileAsString(String filePath) throws IOException {
        return readFileAsString(new File(filePath));
    }

    public static String readFileAsString(File file) throws IOException {
        BufferedInputStream f = null;
        String str = "";
        try {
            byte[] buffer = new byte[(int) file.length()];
            f = new BufferedInputStream(new FileInputStream(file));
            f.read(buffer);
            str = new String(buffer);
        } catch (FileNotFoundException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
        return str;
    }
}



