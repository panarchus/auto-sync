package realm.every.io.autosync;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChannelIOHelper {
    public static byte[] readFromSocket(SocketChannel channel) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(256);

        int bytes = 0;
        int totalBytes = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((bytes = channel.read(bb)) != -1) {
            totalBytes += bytes;
            if (totalBytes == 256 || !bb.hasRemaining()) {
                baos.write(bb.array(), 0, 256);
                bb.clear();
                totalBytes = 0;
            }
        }
        if (totalBytes > 0) {
            baos.write(bb.array(), 0, totalBytes);
        }

        return baos.toByteArray();
    }
}
