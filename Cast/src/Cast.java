import java.nio.ByteBuffer;

public class Cast {
    private Cast(){}

    public static byte[] shortToByte(short[] shorts){
        ByteBuffer buffer = ByteBuffer.allocate(shorts.length * 2);
        buffer.asShortBuffer().put(shorts);
        return buffer.array();
    }

    public static byte[] charToByte(char[] chars){
        ByteBuffer buffer = ByteBuffer.allocate(chars.length * 2);
        buffer.asCharBuffer().put(chars);
        return buffer.array();
    }

    public static char[] byteToChar(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        char[] charArr = new char[bytes.length / 2];
        for (int i = 0; i < bytes.length / 2; i++){
            charArr[i] = buffer.asCharBuffer().get();
        }
        return charArr;
    }

    public static short[] byteToShort(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        short[] shortArr = new short[bytes.length / 2];
        for (int i = 0; i < bytes.length / 2; i++){
            shortArr[i] = buffer.asShortBuffer().get();
        }
        return shortArr;
    }
}
