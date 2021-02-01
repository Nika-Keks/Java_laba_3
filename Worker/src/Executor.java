import ru.spbstu.pipeline.*;

import java.io.BufferedWriter;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

public class Executor implements IExecutor{

    final Encoder encoder;
    final Decoder decoder;
    IConsumer consumer;
    IProducer producer;
    ExecutorGrammar grammar = new ExecutorGrammar();
    String exeMode;
    Logger logger;

    IMediator mediator;
    TYPE type;
    final TYPE[] outputTypes = new TYPE[]{TYPE.BYTE};
    final TYPE[] inputTypes = new TYPE[]{TYPE.SHORT, TYPE.CHAR, TYPE.BYTE};
    byte[] nextDataPiece;

    class ByteMediator implements IMediator {
        @Override
        public Object getData(){
            if (nextDataPiece == null)
                return null;
            return Arrays.copyOf(nextDataPiece, nextDataPiece.length);
        }
    }

    @Override
    public IMediator getMediator(TYPE type){
        if (type.equals(TYPE.BYTE))
            return new ByteMediator();
        else
            return null;
    }

    @Override
    public TYPE[] getOutputTypes() {
        return outputTypes;
    }

    public Executor(Logger logger){
        this.logger = logger;
        encoder = new Encoder(logger);
        decoder = new Decoder(logger);
    }


    @Override
    public RC setConfig(String cfgPath){
        HashMap<String,String> cfgParam =
                SyntacticalAnalyser.getValidExpr(cfgPath,
                        grammar.delimiter(),
                        grammar.token(0));

        if (cfgParam.get(grammar.token(1)) == null)
            return RC.CODE_CONFIG_SEMANTIC_ERROR;

        exeMode = cfgParam.get(grammar.token(1));
        if (exeMode.equals(grammar.token(2)))
            return encoder.setConfig(cfgParam);
        if (exeMode.equals(grammar.token(3)))
            return RC.CODE_SUCCESS;
        return RC.CODE_CONFIG_GRAMMAR_ERROR;
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
        this.producer = producer;
        TYPE type = null;

        for (TYPE type1 : inputTypes) {
            for (TYPE type2 : producer.getOutputTypes()) {
                if (type1.equals(type2)) {
                    type = type1;
                    break;
                }
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
        if (data == null) {
            nextDataPiece = null;
            return consumer.execute();
        }
        byte[] inputData =  null;
        if (type.equals(TYPE.SHORT))
            inputData = Cast.shortToByte((short[])data);
        else if(type.equals(TYPE.CHAR))
            inputData = Cast.charToByte((char[])data);
        else if(type.equals(TYPE.BYTE))
            inputData = (byte[])data;

        if (exeMode.equals(grammar.token(2)))
            nextDataPiece = encoder.encode(inputData);
        else if (exeMode.equals(grammar.token(3)))
            nextDataPiece = decoder.decode(inputData);

        return consumer.execute();
    }
}


