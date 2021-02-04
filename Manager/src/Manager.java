import ru.spbstu.pipeline.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.logging.Logger;

public class Manager {
    IReader pipelineStart;
    static final ManagerGrammar grammar = new ManagerGrammar();
    static final Logger logger = Logger.getLogger(Manager.class.getName());
    FileInputStream inputStream;
    FileOutputStream outputStream;


    public Manager(String cfgPath) {
        HashMap<String, String> variableMap = SyntacticalAnalyser.getValidExpr(cfgPath,
                grammar.delimiter(),
                grammar.token(0));
        RC rc = createConveyor(variableMap);
        if (rc != RC.CODE_SUCCESS) {
            pipelineStart = null;
            logger.severe(LogMsg.FAILED_PIPELINE_CONSTRUCTION.msg);
        }
        else
            logger.info(LogMsg.SUCCESS.msg);
    }

    private RC createConveyor(HashMap<String, String> variableMap){

        ArrayDeque<String> workersNames = new ArrayDeque<>();
        ArrayDeque<String> workersCfg = new ArrayDeque<>();
        RC rc = fillPLSData(variableMap, workersNames, workersCfg);
        if (rc != RC.CODE_SUCCESS) {
            logger.severe(rc.name());
            return rc;
        }
        String inputPath = variableMap.get(grammar.token(MGIndexes.INPUT_PATH.ordinal()));
        String outputPath = variableMap.get(grammar.token(MGIndexes.OUTPUT_PATH.ordinal()));

        try {
            IReader reader = createWorker(workersNames.pollFirst(), workersCfg.pollFirst());
            reader.setProducer(null);
            rc = setInputStream(reader, inputPath);
            if (rc != RC.CODE_SUCCESS)
                return rc;

            pipelineStart = reader;
            IExecutor prevWorker = null;
            IExecutor nextWorker = null;
            while (workersCfg.size() > 1 && workersNames.size() > 1) {
                nextWorker = createWorker(workersNames.pollFirst(), workersCfg.pollFirst());
                if (prevWorker == null) {
                    nextWorker.setProducer(reader);
                    reader.setConsumer(nextWorker);
                }else {
                    nextWorker.setProducer(prevWorker);
                    prevWorker.setConsumer(nextWorker);
                }
                prevWorker = nextWorker;
            }
            IWriter writer = createWorker(workersNames.pollFirst(), workersCfg.pollFirst());
            rc = setOutputStream(writer, outputPath);
            if (rc != RC.CODE_SUCCESS)
                return rc;

            writer.setProducer(prevWorker);
            prevWorker.setConsumer(writer);

        } catch(IllegalAccessException |
                ClassNotFoundException |
                InstantiationException |
                NullPointerException |
                NoSuchMethodException |
                InvocationTargetException e) {
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }
        return RC.CODE_SUCCESS;
    }

    // method fillPLSData fills Dequeues with worker's names ard configs from manager config
    private RC fillPLSData(HashMap<String, String> variableMap, ArrayDeque<String> workersNames, ArrayDeque<String> workersCfg){

        String readerName = grammar.token(MGIndexes.READER.ordinal());
        String readerCfg = grammar.token(MGIndexes.CFG_PREFIX.ordinal()) + grammar.token(MGIndexes.READER.ordinal());
        RC rc = varInDeque(variableMap, workersNames, readerName);
        if (rc != RC.CODE_SUCCESS)
            return rc;
        rc = varInDeque(variableMap, workersCfg, readerCfg);
        if (rc != RC.CODE_SUCCESS)
            return rc;

        int i = 1;
        while(true){
            String nextWorkerName = grammar.token(MGIndexes.EXECUTOR.ordinal()) + Integer.toString(i);
            String nextWorkerCfg = grammar.token(MGIndexes.CFG_PREFIX.ordinal()) + grammar.token(MGIndexes.EXECUTOR.ordinal()) + Integer.toString(i);
            if (varInDeque(variableMap, workersNames, nextWorkerName) != RC.CODE_SUCCESS ||
                varInDeque(variableMap, workersCfg, nextWorkerCfg) != RC.CODE_SUCCESS)
                break;
            i++;
        }

        String writerName = grammar.token(MGIndexes.WRITER.ordinal());
        String writerCfg = grammar.token(MGIndexes.CFG_PREFIX.ordinal()) + grammar.token(MGIndexes.WRITER.ordinal());
        rc = varInDeque(variableMap, workersNames, writerName);
        if (rc != RC.CODE_SUCCESS)
            return rc;
        rc = varInDeque(variableMap, workersCfg, writerCfg);
        if (rc != RC.CODE_SUCCESS)
            return rc;
        return RC.CODE_SUCCESS;
    }

    private RC varInDeque(HashMap<String, String> variableMap, ArrayDeque<String> workersNames, String token){
        String nextWorker = variableMap.get(token);
        if (nextWorker == null)
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        workersNames.add(nextWorker);
        return RC.CODE_SUCCESS;
    }

    private RC setInputStream(IReader reader, String inputPath){

        try{
            inputStream = new FileInputStream(inputPath);
            System.out.println();
            return reader.setInputStream(inputStream);
        }
        catch (FileNotFoundException e) {
            inputStream = null;
            logger.severe(LogMsg.INVALID_INPUT_STREAM.msg);
            return RC.CODE_INVALID_INPUT_STREAM;
        }
    }

    private RC setOutputStream(IWriter writer, String outputPath){

        try{
            outputStream = new FileOutputStream(outputPath);
            return writer.setOutputStream(outputStream);
        } catch (FileNotFoundException ex) {
            outputStream = null;
            logger.severe(LogMsg.INVALID_OUTPUT_STREAM.msg);
            return RC.CODE_SUCCESS;
        }
    }

    // this method is called only in createConveyor and only in try-catch block
    private <T extends IConfigurable> T createWorker(String workerName, String workerCfg)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        logger.info(LogMsg.TRY_CREATE_WORKER.msg + ": " + workerName);
        Class<?> workerClass = Class.forName(workerName);
        T worker = (T)workerClass.getConstructor(Logger.class).newInstance(logger);
        RC rc = worker.setConfig(workerCfg);
        if (rc != RC.CODE_SUCCESS)
            return null;

        logger.info(LogMsg.SUCCESS.msg);

        return worker;
    }

    public RC execute(){
        if (pipelineStart == null) {
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        RC rc = pipelineStart.execute();
        logger.info(rc.toString());
        pipelineStart = null;
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.severe(LogMsg.INVALID_INPUT_STREAM.msg);
            rc = RC.CODE_INVALID_INPUT_STREAM;
        }
        try{
            outputStream.close();
        } catch (IOException e) {
            logger.severe(LogMsg.INVALID_OUTPUT_STREAM.msg);
            rc = RC.CODE_INVALID_OUTPUT_STREAM;
        }
        return rc;
    }
}
