import java.util.logging.Logger;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.spbstu.pipeline.IConsumer;
import ru.spbstu.pipeline.IProducer;
import ru.spbstu.pipeline.IWriter;
import ru.spbstu.pipeline.RC;
import ru.spbstu.pipeline.TYPE;
import ru.spbstu.pipeline.IMediator;

public class Writer implements IWriter{
    WriterGrammar grammar = new WriterGrammar();
    FileOutputStream bFile;
    final Logger logger;
    IConsumer consumer;
    TYPE[] inputTypes = new TYPE[]{TYPE.BYTE, TYPE.SHORT, TYPE.CHAR};
    TYPE type;
    IMediator mediator;

    public Writer(Logger logger){
        this.logger = logger;
    }

    @Override
    public RC setOutputStream(FileOutputStream file){
        if (file == null)
            return RC.CODE_INVALID_OUTPUT_STREAM;
        bFile = file;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String cfgPath){
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

    @Override
    public RC setProducer(IProducer producer){
        if (producer == null)
            return RC.CODE_INVALID_ARGUMENT;
        TYPE[] types = producer.getOutputTypes();
        TYPE type = null;
        for (TYPE type1 : inputTypes){
            for (TYPE type2 : types){
                if (type1.equals(type2)){
                    type = type1;
                    break;
                }
                if (type != null)
                    break;
            }
        }
        if (type == null){
            logger.severe(LogMsg.INCOMPATIBLE_INPUT_AND_OUTPUT_TYPES.msg);
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        this.type = type;
        mediator = producer.getMediator(type);
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(){
        Object data = mediator.getData();
        if (data == null)
            return RC.CODE_SUCCESS;
        byte[] inputData;
        if (type.equals(TYPE.CHAR))
            inputData = Cast.charToByte((char[])data);
        else if(type.equals(TYPE.SHORT))
            inputData = Cast.shortToByte((short[])data);
        else if (type.equals(TYPE.BYTE))
            inputData = (byte[])data;
        else
            return RC.CODE_FAILED_TO_WRITE;

        try{
            bFile.write(inputData);
            return RC.CODE_SUCCESS;
        } catch (IOException ex) {
            logger.severe(LogMsg.FAILED_TO_WRITE.msg);
        }
        return RC.CODE_FAILED_TO_WRITE;
    }
}
