package ch.ethz.vppserver.ippclient;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class IppBuffer {

    protected static final short DEFAULT_BUFFER_SIZE = 8192;
    private final ByteBuffer ippBuf;

    public IppBuffer(short operationID) throws UnsupportedEncodingException {
        this(operationID, DEFAULT_BUFFER_SIZE);
    }

    public IppBuffer(short operationID, short bufferSize) throws UnsupportedEncodingException {
        ippBuf = ByteBuffer.allocateDirect(bufferSize);
        IppTag.putOperation(ippBuf, operationID);
    }

    public ByteBuffer getData() {
        IppTag.putEnd(ippBuf);
        ippBuf.flip();
        return ippBuf;
    }

    public void putUri(String name, String url) throws UnsupportedEncodingException {
        IppTag.putUri(ippBuf, name, url);
    }

    public void putInteger(String name, int value) throws UnsupportedEncodingException {
        IppTag.putInteger(ippBuf, name, value);
    }

    public void putNameWithoutLanguage(String name, String value) throws UnsupportedEncodingException {
        IppTag.putNameWithoutLanguage(ippBuf, name, value);
    }

    public void putTextWithoutLanguage(String name, String value) throws UnsupportedEncodingException {
        IppTag.putTextWithoutLanguage(ippBuf, name, value);
    }

    public void putKeyword(String name, String value) throws UnsupportedEncodingException {
        IppTag.putKeyword(ippBuf, name, value);
    }

    public void putBoolean(String name, boolean value) throws UnsupportedEncodingException {
        IppTag.putBoolean(ippBuf, name, value);
    }

    public void putMimeMediaType(String name, String value) throws UnsupportedEncodingException {
        IppTag.putMimeMediaType(ippBuf, name, value);
    }

    public void putNaturalLanguage(String name, String value) throws UnsupportedEncodingException {
        IppTag.putNaturalLanguage(ippBuf, name, value);
    }

    public void putResolution(String name, int value1, int value2, byte value3) throws UnsupportedEncodingException {
        IppTag.putResolution(ippBuf, name, value1, value2, value3);
    }

    public void putEnum(String name, int value) throws UnsupportedEncodingException {
        IppTag.putEnum(ippBuf, name, value);
    }

    public void putRangeOfInteger(String name, int low, int high) throws UnsupportedEncodingException {
        IppTag.putRangeOfInteger(ippBuf, name, low, high);
    }

    /**
     * TODO: not all possibilities implemented
     *
     * @param attributeBlocks
     * @throws UnsupportedEncodingException
     */
    public void putJobAttributes(String[] attributeBlocks) throws UnsupportedEncodingException {
        if (attributeBlocks == null)
            return;

        IppTag.putJobAttributesTag(ippBuf);

        for (String attributeBlock : attributeBlocks) {
            String[] attr = attributeBlock.split(":");
            if (attr.length != 3)
                return;

            String name = attr[0];
            String tagName = attr[1];
            String value = attr[2];

            switch (tagName) {
                case "boolean":
                    putBoolean(name, value.equals("true"));
                    break;
                case "integer":
                    putInteger(name, Integer.parseInt(value));
                    break;
                case "rangeOfInteger":
                    String[] range = value.split("-");
                    int low = Integer.parseInt(range[0]);
                    int high = Integer.parseInt(range[1]);
                    putRangeOfInteger(name, low, high);
                    break;
                case "setOfRangeOfInteger":
                    String[] ranges = value.split(",");
                    for (String r : ranges) {
                        r = r.trim();
                        String[] values = r.split("-");

                        int value1 = Integer.parseInt(values[0]);
                        int value2 = value1;
                        // two values provided?
                        if (values.length == 2) {
                            value2 = Integer.parseInt(values[1]);
                        }

                        // first attribute value needs name, additional values need to get the "null" name
                        putRangeOfInteger(name, value1, value2);
                        name = null;
                    }
                    break;
                case "keyword":
                    putKeyword(name, value);
                    break;
                case "name":
                    putNameWithoutLanguage(name, value);
                    break;
                case "enum":
                    putEnum(name, Integer.parseInt(value));
                    break;
                case "resolution":
                    String[] resolution = value.split(",");
                    int value1 = Integer.parseInt(resolution[0]);
                    int value2 = Integer.parseInt(resolution[1]);
                    byte value3 = Byte.parseByte(resolution[2]);
                    putResolution(name, value1, value2, value3);
                    break;
            }
        }
    }

}
