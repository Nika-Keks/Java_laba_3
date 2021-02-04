import ru.spbstu.pipeline.BaseGrammar;

public class ManagerGrammar extends BaseGrammar {
    public ManagerGrammar(){
        super(new String[] {";", "Reader", "Executor", "Writer", "cfg", "input", "output"});
    }
}

enum MGIndexes{
    END_LINE,
    READER,
    EXECUTOR,
    WRITER,
    CFG_PREFIX,
    INPUT_PATH,
    OUTPUT_PATH;
}