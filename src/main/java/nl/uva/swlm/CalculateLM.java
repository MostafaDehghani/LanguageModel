/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.swlm;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import static nl.uva.settings.Config.configFile;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Mostafa Dehghani
 */
public class CalculateLM {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CalculateLM.class.getName());
    
    public static void main(String[] args) throws IOException {
        ArrayList<Integer> docsList = new ArrayList<>();
//        for(int i=10061;i<11061;i++)
//        for(int i=10061;i<10172;i++)
        for(int i=0;i<2;i++)
            docsList.add(i);
        String indexPathString = configFile.getProperty("INDEX_PATH");
        Path ipath = FileSystems.getDefault().getPath(indexPathString);
        IndexReader ireader = DirectoryReader.open(FSDirectory.open(ipath));
        
        DocsGroup dGroup = new DocsGroup(ireader, "TEXT", docsList);
        System.out.println("CLM" + dGroup.getGeneralLM().getTopK(20));
//        System.out.println("CLM:" + dGroup.getGeneralLM().getSize());
        System.out.println("STLM" + dGroup.getGroupStandardLM().getTopK(20));
//        System.out.println("STLM:" + dGroup.getGroupStandardLM().getSize());
        System.out.println("SPLM" + dGroup.getGroupSpecificLM().getTopK(20));
//        System.out.println("SPLM:" + dGroup.getGroupSpecificLM().getSize());
        System.out.println("PLM" + dGroup.getGroupParsimoniouseLM().getTopK(20));
//        System.out.println("PLM:" + dGroup.getGroupParsimoniouseLM().getSize());
        System.out.println("SWLM" + dGroup.getGroupSWLM().getTopK(20));   
//        System.out.println("GLM:" + dGroup.getGroupSWLM().getSize());   
        System.out.println("HSWLM" + dGroup.getGroupHSWLM().getTopK(20));   
//        System.out.println("HGLM:" + dGroup.getGroupHSWLM().getSize());   
    }

}
