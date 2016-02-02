package nl.uva.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Mostafa Dehghani
 */
public class Config {

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Config.class.getName());
    public static Properties configFile = new Properties();

    static {
        try {
//            File cFile = new File("/Users/Mosi/GitHub/FB_sigir2016/Config.properties");
//            log.info("\n.....Confog file path: " + cFile.getAbsolutePath() + "......");
//            InputStream stream = new FileInputStream(cFile);
	      InputStream stream = Config.class.getResourceAsStream("Config.properties");
            configFile.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
