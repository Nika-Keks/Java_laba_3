import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;
import java.io.*;
import java.util.HashMap;

import ru.spbstu.pipeline.IConsumer;
import ru.spbstu.pipeline.IProducer;
import ru.spbstu.pipeline.IReader;
import ru.spbstu.pipeline.RC;
import ru.spbstu.pipeline.TYPE;
import ru.spbstu.pipeline.IMediator;

public class Reader implements IReader{
    private Integer nProcBytes;
    int fileSize;
    int nReadData;
    FileInputStream bFile;
    ReaderGrammar grammar = new ReaderGrammar();
    IConsumer consumer;
    IProducer producer;
    final TYPE[] outputTypes = new TYPE[]{TYPE.BYTE, TYPE.CHAR};
    final Logger logger;
    byte[] nextDataPiece;

    class ByteMediator implements IMediator{
        @Override
        public Object getData(){
            if (nextDataPiece == null)
                return null;
            return Arrays.copyOf(nextDataPiece, nextDataPiece.length);
        }
    }

    class CharMediator implements IMediator{
        @Override
        public Object getData() {
            return Cast.byteToChar(nextDataPiece);
        }
    }

    class ShortMediator implements IMediator{
        @Override
        public Object getData() {
            return Cast.byteToShort(nextDataPiece);
        }
    }

    public Reader(Logger logger){
        this.logger = logger;
    }


    private byte[] readFile(){
        if (bFile == null)
            return  null;

        try{
            byte[] data = null;
            if (nProcBytes < fileSize - nReadData){
                data = new byte[nProcBytes];
                bFile.read(data);
                nReadData += nProcBytes;
            }
            else if (fileSize - nReadData > 0){
                data = new byte[fileSize - nReadData];
                nReadData = fileSize;
                bFile.read(data);
            }
            return data;
        } catch (IOException ex) {
            logger.severe(LogMsg.FAILED_TO_READ.msg);
        }
        return null;
    }

    @Override
    public RC setInputStream(FileInputStream file){
        if (file == null)
            return RC.CODE_INVALID_INPUT_STREAM;
        bFile = file;
        try {
            fileSize = (int)file.getChannel().size();
        } catch (IOException e) {
            logger.severe(LogMsg.INVALID_INPUT_STREAM.msg);
            return RC.CODE_INVALID_INPUT_STREAM;
        }
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String cfgPath){
        HashMap<String,String> cfgParam =
                SyntacticalAnalyser.getValidExpr(cfgPath,
                        grammar.delimiter(),
                        grammar.token(RGIndexes.END_LINE.ordinal()));

        nProcBytes = SemanticAnalyser.getInteger(cfgParam, grammar.token(RGIndexes.BUF_SIZE.ordinal()));
        if (nProcBytes == null){
            logger.severe(LogMsg.INVALID_CONFIG_DATA.msg);
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }
        logger.info(LogMsg.SUCCESS.msg);
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer consumer){
        if (consumer == null)
            return RC.CODE_INVALID_ARGUMENT;
        this.consumer = consumer;
        return RC.CODE_SUCCESS;
    }


    public RC setProducer(IProducer var1){
        producer = null;
        return RC.CODE_SUCCESS;
    }

    @Override
    public TYPE[] getOutputTypes() {
        return outputTypes;
    }

    @Override
    public IMediator getMediator(TYPE type){
        if (type.equals(TYPE.BYTE))
            return new ByteMediator();
        else if (type.equals(TYPE.CHAR))
            return new CharMediator();
        else if (type.equals(TYPE.SHORT))
            return new ShortMediator();
        else
            return null;
    }

    @Override
    public RC execute(){
        byte[] data;
        RC rc;
        do{
            nextDataPiece = readFile();
            rc = consumer.execute();
            if (rc != RC.CODE_SUCCESS)
                break;
        }while (nextDataPiece != null);

        return rc;
    }
}
