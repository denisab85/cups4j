package ch.ethz.vppserver.ippclient;

import lombok.NonNull;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Copyright (C) 2008 ITS of ETH Zurich, Switzerland, Sarah Windler Burri
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * <p>
 * See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * program; if not, see <http://www.gnu.org/licenses/>.
 */
public class IppTag {

    private final static byte MAJOR_VERSION = 0x01;
    private final static byte MINOR_VERSION = 0x01;

    private final static String ATTRIBUTES_CHARSET = "attributes-charset";
    private final static String ATTRIBUTES_NATURAL_LANGUAGE = "attributes-natural-language";

    private final static String ATTRIBUTES_CHARSET_VALUE = "utf-8";
    private final static String ATTRIBUTES_NATURAL_LANGUAGE_VALUE = "en-us";
    private final static short ATTRIBUTES_INTEGER_VALUE_LENGTH = 0x0004;
    private final static short ATTRIBUTES_RANGE_OF_INT_VALUE_LENGTH = 0x0008;
    private final static short ATTRIBUTES_BOOLEAN_VALUE_LENGTH = 0x0001;
    private final static short ATTRIBUTES_RESOLUTION_VALUE_LENGTH = 0x0009;
    private final static byte ATTRIBUTES_BOOLEAN_FALSE_VALUE = 0x00;
    private final static byte ATTRIBUTES_BOOLEAN_TRUE_VALUE = 0x01;

    private final static byte OPERATION_ATTRIBUTES_TAG = 0x01;
    private final static byte JOB_ATTRIBUTES_TAG = 0x02;
    private final static byte END_OF_ATTRIBUTES_TAG = 0x03;
    private final static byte PRINTER_ATTRIBUTES_TAG = 0x04;
    private final static byte UNSUPPORTED_ATTRIBUTES_TAG = 0x05;
    private final static byte SUBSCRIPTION_ATTRIBUTES_TAG = 0x06;
    private final static byte EVENT_NOTIFICATION_ATTRIBUTES_TAG = 0x07;
    private final static byte INTEGER_TAG = 0x21;
    private final static byte BOOLEAN_TAG = 0x22;
    private final static byte ENUM_TAG = 0x23;
    private final static byte RESOLUTION_TAG = 0x32;
    private final static byte RANGE_OF_INTEGER_TAG = 0x33;
    private final static byte TEXT_WITHOUT_LANGUAGE_TAG = 0x41;
    private final static byte NAME_WITHOUT_LANGUAGE_TAG = 0x42;
    private final static byte KEYWORD_TAG = 0x44;
    private final static byte URI_TAG = 0x45;
    private final static byte URI_SCHEME_TAG = 0x46;
    private final static byte CHARSET_TAG = 0x47;
    private final static byte NATURAL_LANGUAGE_TAG = 0x48;
    private final static byte MIME_MEDIA_TYPE_TAG = 0x49;

    private final static short NULL_LENGTH = 0;

    private static int requestID = 0; // required attribute within operations (will increase with every request)

    public static void putOperation(@NonNull ByteBuffer ippBuf, short operation) throws UnsupportedEncodingException {
        putOperation(ippBuf, operation, null, null);
    }

    public static void putOperation(@NonNull ByteBuffer ippBuf, short operation, String charset, String naturalLanguage)
            throws UnsupportedEncodingException {
        if (charset == null) {
            charset = ATTRIBUTES_CHARSET_VALUE;
        }
        if (naturalLanguage == null) {
            naturalLanguage = ATTRIBUTES_NATURAL_LANGUAGE_VALUE;
        }
        ippBuf.put(MAJOR_VERSION);
        ippBuf.put(MINOR_VERSION);
        ippBuf.putShort(operation);
        ippBuf.putInt(getNextRequestID());
        ippBuf.put(OPERATION_ATTRIBUTES_TAG);

        putCharset(ippBuf, ATTRIBUTES_CHARSET, charset);
        putNaturalLanguage(ippBuf, ATTRIBUTES_NATURAL_LANGUAGE, naturalLanguage);
    }

    public static int getNextRequestID() {
        return ++requestID;
    }

    public static void putOperationAttributesTag(@NonNull ByteBuffer ippBuf) {
        ippBuf.put(OPERATION_ATTRIBUTES_TAG);
    }

    public static void putJobAttributesTag(@NonNull ByteBuffer ippBuf) {
        ippBuf.put(JOB_ATTRIBUTES_TAG);
    }

    public static void putSubscriptionAttributesTag(@NonNull ByteBuffer ippBuf) {
        ippBuf.put(SUBSCRIPTION_ATTRIBUTES_TAG);
    }

    public static void putEventNotificationAttributesTag(@NonNull ByteBuffer ippBuf) {
        ippBuf.put(EVENT_NOTIFICATION_ATTRIBUTES_TAG);
    }

    public static void putUnsupportedAttributesTag(@NonNull ByteBuffer ippBuf) {
        ippBuf.put(UNSUPPORTED_ATTRIBUTES_TAG);
    }

    public static void putPrinterAttributesTag(@NonNull ByteBuffer ippBuf) {
        ippBuf.put(PRINTER_ATTRIBUTES_TAG);
    }

    public static void putCharset(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putCharset(ippBuf, null, null);
    }

    public static void putCharset(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        putCharset(ippBuf, attributeName, null);
    }

    public static void putCharset(@NonNull ByteBuffer ippBuf, String attributeName, String value)
            throws UnsupportedEncodingException {
        putUsAscii(ippBuf, CHARSET_TAG, attributeName, value);
    }

    public static void putNaturalLanguage(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putNaturalLanguage(ippBuf, null, null);
    }

    public static void putNaturalLanguage(@NonNull ByteBuffer ippBuf, String attributeName)
            throws UnsupportedEncodingException {
        putNaturalLanguage(ippBuf, attributeName, null);
    }

    public static void putNaturalLanguage(@NonNull ByteBuffer ippBuf, String attributeName, String value)
            throws UnsupportedEncodingException {
        putUsAscii(ippBuf, NATURAL_LANGUAGE_TAG, attributeName, value);
    }

    public static void putUri(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putUri(ippBuf, null, null);
    }

    public static void putUri(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        putUri(ippBuf, attributeName, null);
    }

    public static void putUri(@NonNull ByteBuffer ippBuf, String attributeName, String value)
            throws UnsupportedEncodingException {
        putUsAscii(ippBuf, URI_TAG, attributeName, value);
    }

    public static void putUriScheme(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putUriScheme(ippBuf, null, null);
    }

    public static void putUriScheme(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        putUriScheme(ippBuf, attributeName, null);
    }

    public static void putUriScheme(@NonNull ByteBuffer ippBuf, String attributeName, String value)
            throws UnsupportedEncodingException {
        putUsAscii(ippBuf, URI_SCHEME_TAG, attributeName, value);
    }

    public static void putNameWithoutLanguage(@NonNull ByteBuffer ippBuf, String attributeName, String value)
            throws UnsupportedEncodingException {
        ippBuf.put(NAME_WITHOUT_LANGUAGE_TAG);

        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }

        if (value != null) {
            putAttName(ippBuf, value);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
    }

    public static void putTextWithoutLanguage(@NonNull ByteBuffer ippBuf, String attributeName, String value)
            throws UnsupportedEncodingException {
        ippBuf.put(TEXT_WITHOUT_LANGUAGE_TAG);

        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }

        if (value != null) {
            putAttName(ippBuf, value);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
    }

    public static void putInteger(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putInteger(ippBuf, null);
    }

    public static void putInteger(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        ippBuf.put(INTEGER_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }

        ippBuf.putShort(NULL_LENGTH);
    }

    public static void putInteger(@NonNull ByteBuffer ippBuf, String attributeName, int value)
            throws UnsupportedEncodingException {
        ippBuf.put(INTEGER_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }

        ippBuf.putShort(ATTRIBUTES_INTEGER_VALUE_LENGTH);
        ippBuf.putInt(value);
    }

    public static void putBoolean(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putBoolean(ippBuf, null);
    }

    public static void putBoolean(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        ippBuf.put(BOOLEAN_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }

        ippBuf.putShort(NULL_LENGTH);
    }

    public static void putBoolean(@NonNull ByteBuffer ippBuf, String attributeName, boolean value)
            throws UnsupportedEncodingException {
        ippBuf.put(BOOLEAN_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }

        ippBuf.putShort(ATTRIBUTES_BOOLEAN_VALUE_LENGTH);
        if (value) {
            ippBuf.put(ATTRIBUTES_BOOLEAN_TRUE_VALUE);
        } else {
            ippBuf.put(ATTRIBUTES_BOOLEAN_FALSE_VALUE);
        }
    }

    public static void putEnum(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putEnum(ippBuf, null);
    }

    public static void putEnum(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        ippBuf.put(ENUM_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
        ippBuf.putShort(NULL_LENGTH);
    }

    public static void putEnum(@NonNull ByteBuffer ippBuf, String attributeName, int value)
            throws UnsupportedEncodingException {
        ippBuf.put(ENUM_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
        ippBuf.putShort(ATTRIBUTES_INTEGER_VALUE_LENGTH);
        ippBuf.putInt(value);
    }

    public static void putResolution(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putResolution(ippBuf, null);
    }

    public static void putResolution(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        ippBuf.put(RESOLUTION_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
        ippBuf.putShort(NULL_LENGTH);
    }

    public static void putResolution(@NonNull ByteBuffer ippBuf, String attributeName, int value1, int value2, byte value3)
            throws UnsupportedEncodingException {
        ippBuf.put(RESOLUTION_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
        ippBuf.putShort(ATTRIBUTES_RESOLUTION_VALUE_LENGTH);
        ippBuf.putInt(value1);
        ippBuf.putInt(value2);
        ippBuf.put(value3);
    }

    public static void putRangeOfInteger(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putRangeOfInteger(ippBuf, null);
    }

    public static void putRangeOfInteger(@NonNull ByteBuffer ippBuf, String attributeName)
            throws UnsupportedEncodingException {
        ippBuf.put(RANGE_OF_INTEGER_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
        ippBuf.putShort(NULL_LENGTH);
    }

    public static void putRangeOfInteger(@NonNull ByteBuffer ippBuf, String attributeName, int value1, int value2)
            throws UnsupportedEncodingException {
        ippBuf.put(RANGE_OF_INTEGER_TAG);
        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
        ippBuf.putShort(ATTRIBUTES_RANGE_OF_INT_VALUE_LENGTH);
        ippBuf.putInt(value1);
        ippBuf.putInt(value2);
    }

    public static void putMimeMediaType(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putMimeMediaType(ippBuf, null, null);
    }

    public static void putMimeMediaType(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        putMimeMediaType(ippBuf, attributeName, null);
    }

    public static void putMimeMediaType(@NonNull ByteBuffer ippBuf, String attributeName, String value)
            throws UnsupportedEncodingException {
        putUsAscii(ippBuf, MIME_MEDIA_TYPE_TAG, attributeName, value);
    }

    public static void putKeyword(@NonNull ByteBuffer ippBuf) throws UnsupportedEncodingException {
        putKeyword(ippBuf, null, null);
    }

    public static void putKeyword(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        putKeyword(ippBuf, attributeName, null);
    }

    public static void putKeyword(@NonNull ByteBuffer ippBuf, String attributeName, String value)
            throws UnsupportedEncodingException {
        putUsAscii(ippBuf, KEYWORD_TAG, attributeName, value);
    }

    public static void putEnd(@NonNull ByteBuffer ippBuf) {
        ippBuf.put(END_OF_ATTRIBUTES_TAG);
    }

    private static void putAttName(@NonNull ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
        byte[] bytes = IppUtil.toBytes(attributeName);
        ippBuf.putShort((short) bytes.length);
        ippBuf.put(bytes);
    }

    private static void putUsAscii(@NonNull ByteBuffer ippBuf, byte tag, String attributeName, String value)
            throws UnsupportedEncodingException {
        ippBuf.put(tag);

        if (attributeName != null) {
            putAttName(ippBuf, attributeName);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }

        if (value != null) {
            putAttName(ippBuf, value);
        } else {
            ippBuf.putShort(NULL_LENGTH);
        }
    }
}