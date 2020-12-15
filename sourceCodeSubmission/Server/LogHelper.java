package com.coen445.FinalProject;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class LogHelper{
    private FileHandler handler;
    private Logger logger;
    private final boolean append = true;


    public LogHelper(String logName) throws IOException {
        handler = new FileHandler(logName, append);
        logger = Logger.getLogger("com.coen445.FinalProject");
        logger.addHandler(handler);
    }

    public void writeInfo(Object message){
        logger.info(message.toString());
    }

    public void writeWarning(String message){
        logger.warning(message);
    }
}
