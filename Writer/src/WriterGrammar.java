import ru.spbstu.pipeline.BaseGrammar;

public class WriterGrammar extends BaseGrammar {
    public WriterGrammar(){
        super(new String[] {";"});
    }
}

enum WGIndexes{
    END_LINE;
}
