import ru.spbstu.pipeline.BaseGrammar;

public class ExecutorGrammar extends BaseGrammar{
    public ExecutorGrammar(){
        super(new String[] {";", "exeMode", "-e", "-d", "rightBorder", "leftBorder", "eps"});
    }

}

enum EGIndexes{
    END_LINE,
    EXE_MODE,
    ENCODE,
    DECODE,
    R_BORDER,
    L_BORDER,
    EPS;
}