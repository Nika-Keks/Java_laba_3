import ru.spbstu.pipeline.BaseGrammar;

public class ReaderGrammar extends BaseGrammar{

    public ReaderGrammar(){
        super(new String[]{";", "bufSize"});
    }
}

enum RGIndexes{
    END_LINE,
    BUF_SIZE;
}