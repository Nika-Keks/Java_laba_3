import java.nio.ByteBuffer;

public class Cast {
    private Cast(){}

    public static byte[] shortToByte(short[] shorts){
        ByteBuffer buffer = ByteBuffer.allocate(shorts.length * 2);
        buffer.asShortBuffer().put(shorts);
        byte[] bytes = new byte[shorts.length * 2];
        for (int i = 0; i < shorts.length; i++){
            bytes[2 * i] = buffer.get(2 * i);
            bytes[2 * i + 1] = buffer.get(2 * i + 1);
        }
        return bytes;
    }

    public static byte[] charToByte(char[] chars){
        ByteBuffer buffer = ByteBuffer.allocate(chars.length * 2);
        buffer.asCharBuffer().put(chars);
        byte[] bytes = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++){
            bytes[2 * i] = buffer.get(2 * i);
            bytes[2 * i + 1] = buffer.get(2 * i + 1);
        }
        return bytes;
    }

    public static char[] byteToChar(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        char[] chars = new char[bytes.length / 2];
        for (int i = 0; i < bytes.length / 2; i++){
            chars[i] = buffer.asCharBuffer().get(i);
        }
        return chars;
    }

    public static short[] byteToShort(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < bytes.length / 2; i++){
            shorts[i] = buffer.asShortBuffer().get(i);
        }
        return shorts;
    }
}
