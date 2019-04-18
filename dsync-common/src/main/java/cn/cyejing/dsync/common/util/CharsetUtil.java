package cn.cyejing.dsync.common.util;

import io.netty.buffer.ByteBuf;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Born
 */
@Slf4j
public class CharsetUtil {
    public static final Charset UTF_8 = Charset.forName("UTF-8");


    public static String byteToString(ByteBuf byteBuf,int length) {
        StringBuilder builder = new StringBuilder();
        char c;
        for (int i = 0; i < length; i++) {
            if((c = byteBuf.readChar()) != 0){
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static String byteToString(byte[] src) {
        int i = 0;
        for (; i < src.length && src[i] != 0; i++) {
        }
        try {
            return new String(src, 0, i, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Character Convert Failed", e);
        }
        return null;
    }


    public static byte[] stringToByte(String src) {
        return src.getBytes(UTF_8);
    }

    public static byte[] stringToByte(String src, int length) {
        byte[] d = new byte[length];
        byte[] s = src.getBytes(UTF_8);
        System.arraycopy(s, 0, d, 0, s.length >= length ? length : s.length);
        return d;
    }

}
