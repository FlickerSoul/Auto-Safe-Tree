import org.apache.log4j.Logger;

public class LogTest {
    private static final Logger logger = Logger.getLogger(LogTest.class);

    public static void main(String[] args){
        logger.info("nice job");
        logger.debug("display");
        logger.warn("something wrong");
        logger.error("broken");
    }
}
