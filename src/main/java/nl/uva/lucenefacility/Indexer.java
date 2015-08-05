/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.lucenefacility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static nl.uva.settings.Config.configFile;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 *
 * @author Mostafa Dehghani
 */
public abstract class Indexer {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Indexer.class.getName());
    protected IndexWriter writer;
    private final Boolean stemming = configFile.getProperty("IF_STEMMING").equals("1"); 
    private final Boolean stopWordsRemoving = configFile.getProperty("IF_STOPWORDS_REMOVING").equals("1");
    private final Boolean commonWordsRemoving = configFile.getProperty("IF_COMMONWORDS_REMOVING").equals("1");
    private final Integer commonWordNum = Integer.parseInt(configFile.getProperty("COMMON_WORDS_NUM"));
    protected final Integer minDocLength = Integer.parseInt(configFile.getProperty("MIN_DOC_LENGTH"));
    private Map<String, Analyzer> analyzerMap = new HashMap<>();
    protected Integer docCount = 0;
    
    public Indexer() throws Exception, Throwable {
        try {
            this.analyzerMapInitializer(this.analyzerMap);
            this.Indexer();
        } catch (Exception ex) {
            log.error(ex);
            throw ex;
        }
    }

    public void Indexer() throws Exception, Throwable {
        try {
            log.info("----------------------- INDEXING--------------------------");

            String indexPathString = configFile.getProperty("INDEX_PATH");
            Path ipath = FileSystems.getDefault().getPath(indexPathString);
            this.IndexesCleaner(indexPathString);

            if (!stopWordsRemoving || commonWordsRemoving) {
                //With Stopwords (or/ In order to make list of common words)

                MyAnalyzer myAnalyzer_noStoplist = new MyAnalyzer(stemming);
                Analyzer analyzer = myAnalyzer_noStoplist.getAnalyzer(configFile.getProperty("CORPUS_LANGUAGE"));
                PerFieldAnalyzerWrapper prfWrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerMap);
                IndexWriterConfig irc = new IndexWriterConfig(prfWrapper);
                this.writer = new IndexWriter(new SimpleFSDirectory(ipath), irc);
                this.docIndexer();
                this.writer.commit();
                this.writer.close();
                analyzer.close();
                prfWrapper.close();
                log.info("-------------------------------------------------");
                log.info("Index without common words removing is created successfully...");
                log.info("-------------------------------------------------");
                if (commonWordsRemoving) {
                    String tmpIndexPath = configFile.getProperty("INDEX_PATH") + "/tmp";
                    Path tmpipath = FileSystems.getDefault().getPath(tmpIndexPath);
                    FileUtils.forceMkdir(new File(tmpIndexPath));
                    IndexReader ireader = DirectoryReader.open(FSDirectory.open(ipath));
                    IndexInfo iInfo = new IndexInfo(ireader);
                    ArrayList<String> commonWs = iInfo.getTopTerms_TF("TEXT", this.commonWordNum);
                    MyAnalyzer myAnalyzer_Stoplist = new MyAnalyzer(stemming, commonWs);
                    Analyzer analyzer_2 = myAnalyzer_Stoplist.getAnalyzer(configFile.getProperty("CORPUS_LANGUAGE"));
                    PerFieldAnalyzerWrapper prfWrapper_2 = new PerFieldAnalyzerWrapper(analyzer_2, analyzerMap);
                    IndexWriterConfig irc_2 = new IndexWriterConfig(prfWrapper_2);
                    this.writer = new IndexWriter(new SimpleFSDirectory(tmpipath), irc_2);
                    this.docIndexer();
                    this.writer.commit();
                    this.writer.close();
                    analyzer_2.close();
                    prfWrapper_2.close();
                    FileUtils.deleteDirectory(new File(indexPathString));
                    File index = new File(tmpIndexPath);
                    File newIndex = new File(indexPathString);
                    index.renameTo(newIndex);
                    log.info("-------------------------------------------------");
                    log.info("Index with common words removing is created successfully...");
                    log.info("-------------------------------------------------");
                }
            } else if (stopWordsRemoving) {
                MyAnalyzer myAnalyzer_Stoplist = new MyAnalyzer(stemming, this.LoadStopwords());
                Analyzer analyzer = myAnalyzer_Stoplist.getAnalyzer(configFile.getProperty("CORPUS_LANGUAGE"));
                PerFieldAnalyzerWrapper prfWrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerMap);
                IndexWriterConfig irc = new IndexWriterConfig(prfWrapper);
                this.writer = new IndexWriter(new SimpleFSDirectory(ipath), irc);
                this.docIndexer();
                this.writer.commit();
                this.writer.close();
                analyzer.close();
                prfWrapper.close();
                log.info("-------------------------------------------------");
                log.info("Index without common words removing is created successfully...");
                log.info("-------------------------------------------------");
            }

        } catch (Exception ex) {
            log.error(ex);
            throw ex;
        }
    }

    protected abstract void docIndexer() throws Exception;

    protected abstract void IndexDoc(Object obj) throws Exception;

    protected abstract void analyzerMapInitializer(Map<String, Analyzer> analyzerMap);

    private void IndexesCleaner(String path) {
        try {
            File Index = new File(path);
            if (Index.exists()) {
                FileUtils.deleteDirectory(Index);
                log.info("Deletting the existing index directory on: " + path);
            }
            FileUtils.forceMkdir(new File(path));
            log.info("Making Index directory on: " + path);
            log.info("\n\n -----------------------CLeaning is Finished--------------------------\n");
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    private ArrayList<String> LoadStopwords() throws FileNotFoundException, IOException {
        ArrayList<String> stoplist = new ArrayList<>();
        File stopfile = new File(configFile.getProperty("STOPWORDS_PATH"));
        try (BufferedReader br = new BufferedReader(new FileReader(stopfile))) {
            for (String line; (line = br.readLine()) != null;) {
                stoplist.add(line);
            }
        }
        log.info("Stopwords file is loaded....");
        return stoplist;
    }
}
