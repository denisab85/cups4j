package cups4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtil {

    public static ByteBuffer readByteBuffer(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            channel.read(byteBuffer);
            byteBuffer.flip();
            return byteBuffer;
        }
    }

    public static void writeByteBuffer(ByteBuffer byteBuffer, Path path) throws IOException {
        try (FileChannel fc = new FileOutputStream(path.toFile()).getChannel()) {
            fc.write(byteBuffer);
        }
    }

    public static String toHexString(ByteBuffer byteBuffer) {
        StringBuilder sb = new StringBuilder();
        boolean lastWasAscii = false;

        while (byteBuffer.hasRemaining()) {
            byte currentByte = byteBuffer.get();

            if (currentByte >= 32 && currentByte <= 126) { // Printable ASCII
                if (!lastWasAscii && sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append((char) currentByte);
                lastWasAscii = true;
            } else {
                if (lastWasAscii || (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ')) {
                    sb.append(' ');
                }
                sb.append(String.format("%02x", currentByte));
                lastWasAscii = false;
            }
        }
        return sb.toString();
    }

}
