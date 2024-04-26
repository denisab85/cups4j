package ch.ethz.vppserver.ippclient;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.cups4j.ipp.attributes.Enum;
import org.cups4j.ipp.attributes.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
@Slf4j
public class IppResponse {
    private final static String CRLF = "\r\n";
    private static final int BYTEBUFFER_CAPACITY = 8192;
    // Saved list of elements of 'TAG_LIST_FILENAME' and 'ATTRIBUTE_LIST_FILENAME'
    private final List<Tag> _tagList;
    private final List<AttributeGroup> _attributeGroupList;
    private final List<AttributeGroup> _result;
    // read IPP response in global buffer
    ByteBuffer _buf;
    // Saved response of printer
    private AttributeGroup _attributeGroupResult = null;
    private Attribute _attributeResult = null;

    public IppResponse() {
        IIppAttributeProvider ippAttributeProvider = IppAttributeProviderFactory.createIppAttributeProvider();

        _tagList = ippAttributeProvider.getTagList();
        _attributeGroupList = ippAttributeProvider.getAttributeGroupList();

        _result = new ArrayList<>();
        _buf = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
    }

    /**
     * @param channel
     * @return
     * @throws IOException
     */
    public IppResult getResponse(SocketChannel channel) throws IOException {
        if (channel == null) {
            log.error("IppResponse.getResponse(): no channel given");
            return null;
        }

        _buf.clear();

        _attributeGroupResult = null;
        _attributeResult = null;
        _result.clear();

        IppResult result = new IppResult();
        boolean httpResponse = false;
        boolean ippHeaderResponse = false;

        // be careful: HTTP and IPP could be transmitted in different set of buffers.
        // see RFC2910, http://www.ietf.org/rfc/rfc2910, page 19

        ByteBuffer tmpBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
        ArrayList<ByteBuffer> bufferList = new ArrayList<>();

        while (channel.read(tmpBuffer) != -1) {
            tmpBuffer.flip();
            // read HTTP header
            if ((!httpResponse) && (tmpBuffer.hasRemaining())) {
                _buf = tmpBuffer;
                result.setHttpStatusResponse(getHTTPHeader());
                httpResponse = true;
            }

            // read IPP header
            if ((!ippHeaderResponse) && (tmpBuffer.hasRemaining())) {
                _buf = tmpBuffer;
                result.setIppStatusResponse(getIPPHeader());
                ippHeaderResponse = true;
            }

            // read the IPP-answer - this can be large, so take to read all information
            if (tmpBuffer.hasRemaining()) {
                bufferList.add(tmpBuffer);
            }
            tmpBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
        }

        _buf = concatenateBytebuffers(bufferList);
        // read attribute group list with attributes
        getAttributeGroupList();

        closeAttributeGroup();
        result.setAttributeGroupList(_result);
        return result;
    }

    /**
     * @param channel
     * @return
     * @throws IOException
     */
    public IppResult getResponse(ByteBuffer buffer) throws IOException {

        _buf.clear();

        _attributeGroupResult = null;
        _attributeResult = null;
        _result.clear();

        IppResult result = new IppResult();

        // be careful: HTTP and IPP could be transmitted in different set of buffers.
        // see RFC2910, http://www.ietf.org/rfc/rfc2910, page 19
        // read IPP header
        if (buffer.hasRemaining()) {
            _buf = buffer;
            if (buffer.get(0) > 0x20) {
                return parseErrorText();
            } else {
                result.setIppStatusResponse(getIPPHeader());
            }
        }

        _buf = buffer;
        // read attribute group list with attributes
        getAttributeGroupList();

        closeAttributeGroup();
        result.setAttributeGroupList(_result);
        return result;
    }

    /**
     * concatenate nio-ByteBuffers
     *
     * @param buffers ArrayList<ByteBuffer>
     * @return ByteBuffer
     */
    private ByteBuffer concatenateBytebuffers(ArrayList<ByteBuffer> buffers) {
        int n = 0;
        for (ByteBuffer b : buffers)
            n += b.remaining();

        ByteBuffer buf = (n > 0 && buffers.get(0).isDirect()) ? ByteBuffer.allocateDirect(n) : ByteBuffer.allocate(n);
        if (n > 0)
            buf.order(buffers.get(0).order());

        for (ByteBuffer b : buffers)
            buf.put(b.duplicate());

        buf.flip();
        return buf;
    }

    /**
     * @return
     */
    private String getHTTPHeader() {
        String endOf = CRLF + CRLF;
        StringBuilder sb = new StringBuilder();
        while (sb.indexOf(endOf) == -1) {
            int b = _buf.get();
            int ival = b & 0xff;
            char c = (char) ival;
            sb.append(c);
        }
        if (sb.length() != 0) {
            return sb.toString();
        }
        return null;
    }

    /**
     * @return
     */
    private String getIPPHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Major Version:").append(IppUtil.toHexWithMarker(_buf.get()));
        sb.append(" Minor Version:").append(IppUtil.toHexWithMarker(_buf.get()));

        String statusCode = IppUtil.toHexWithMarker(_buf.get()) + IppUtil.toHex(_buf.get());
        String statusMessage = getEnumName(statusCode, "status-code");

        sb.append(" Request Id:").append(_buf.getInt()).append("\n");
        sb.append("Status Code:").append(statusCode).append("(").append(statusMessage).append(")");

        if (sb.length() != 0) {
            return sb.toString();
        }
        return null;
    }

    private IppResult parseErrorText() {
        IppResult result = new IppResult();
        byte[] buffer = new byte[_buf.capacity() - _buf.position()];
        _buf.get(buffer);
        String errorText = new String(buffer);
        if (errorText.contains("Unauthorized")) {
            result.setIppStatusResponse("client-error-not-authorized (0x403)");
        } else {
            result.setIppStatusResponse("unknown");
        }
        log.warn(errorText);
        return result;
    }

    /**
     * <p>
     * <strong>Note:</strong> Global variables <code>_attributeGroupResult</code>,
     * <code>_attributeResult</code>, <code>_result</code> are filled by local
     * 'tag' methods.<br />
     * Decision for this programming solution is based on the structure of IPP tag
     * sequences to clarify the attribute structure with its values.
     * </p>
     *
     * @return list of attributes group
     */
    private List<AttributeGroup> getAttributeGroupList() {
        while (_buf.hasRemaining()) {

            byte tag = _buf.get();
            switch (tag) {
                case 0x00:
                    setAttributeGroup(tag); // reserved
                    continue;
                case 0x01:
                    setAttributeGroup(tag); // operation-attributes
                    continue;
                case 0x02:
                    setAttributeGroup(tag); // job-attributes
                    continue;
                case 0x03:
                    return _result; // end-attributes
                case 0x04:
                    setAttributeGroup(tag); // printer-attributes
                    continue;
                case 0x05:
                    setAttributeGroup(tag); // unsupported-attributes
                    continue;
                case 0x06:
                    setAttributeGroup(tag); // subscription-attributes
                    continue;
                case 0x07:
                    setAttributeGroup(tag); // event-notification-attributes
                    continue;
                case 0x13:
                    setNoValueAttribute(); // no-value
                    continue;
                case 0x21:
                    setIntegerAttribute(tag); // integer
                    continue;
                case 0x22:
                    setBooleanAttribute(tag); // boolean
                    continue;
                case 0x23:
                    setEnumAttribute(tag); // enumeration
                    continue;
                case 0x30:
                    setTextAttribute(tag); // octetString;
                    continue;
                case 0x31:
                    setDateTimeAttribute(tag);// datetime
                    continue;
                case 0x32:
                    setResolutionAttribute(tag);// resolution
                    continue;
                case 0x33:
                    setRangeOfIntegerAttribute(tag);// rangeOfInteger
                    continue;
                case 0x35:
                    setTextWithLanguageAttribute(tag); // textWithLanguage
                    continue;
                case 0x36:
                    setNameWithLanguageAttribute(tag); // nameWithLanguage
                    continue;
                case 0x41:
                    setTextAttribute(tag); // textWithoutLanguage
                    continue;
                case 0x42:
                    setTextAttribute(tag); // nameWithoutLanguage
                    continue;
                case 0x44:
                    setTextAttribute(tag); // keyword
                    continue;
                case 0x45:
                    setTextAttribute(tag); // uri
                    continue;
                case 0x46:
                    setTextAttribute(tag); // uriScheme
                    continue;
                case 0x47:
                    setTextAttribute(tag); // charset
                    continue;
                case 0x48:
                    setTextAttribute(tag); // naturalLanguage
                    continue;
                case 0x49:
                    setTextAttribute(tag); // mimeMediaType
                    continue;
                default:
                    return _result; // not defined
            }
        }
        return null;
    }

    /**
     * @param tag
     */
    private void setAttributeGroup(byte tag) {
        if (_attributeGroupResult != null) {
            if (_attributeResult != null) {
                _attributeGroupResult.getAttributes().add(_attributeResult);
            }
            _result.add(_attributeGroupResult);
        }
        _attributeResult = null;

        _attributeGroupResult = new AttributeGroup();
        _attributeGroupResult.setTagName(getTagName(IppUtil.toHexWithMarker(tag)));
    }

    /**
     *
     */
    private void closeAttributeGroup() {
        if (_attributeGroupResult != null) {
            if (_attributeResult != null) {
                _attributeGroupResult.getAttributes().add(_attributeResult);
            }
            _result.add(_attributeGroupResult);
        }
        _attributeResult = null;
        _attributeGroupResult = null;
    }

    /**
     * @param tag
     */
    private void setTextAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }

        // set attribute value
        if (!_buf.hasRemaining()) {
            return;
        }
        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            byte[] dst = new byte[length];
            _buf.get(dst);
            String value = IppUtil.toString(dst);
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);
            attrValue.setValue(value);
            _attributeResult.getAttributeValues().add(attrValue);
        }

    }

    /**
     * TODO: natural-language not considered in reporting
     *
     * @param tag
     */
    private void setTextWithLanguageAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }

        // set natural-language and attribute value
        if (!_buf.hasRemaining()) {
            return;
        }

        // set tag, tag name, natural-language
        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            byte[] dst = new byte[length];
            _buf.get(dst);
            String value = IppUtil.toString(dst);
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);
            attrValue.setValue(value);
            _attributeResult.getAttributeValues().add(attrValue);

            // set value
            length = _buf.getShort();
            if ((length != 0) && (_buf.remaining() >= length)) {
                dst = new byte[length];
                _buf.get(dst);
                value = IppUtil.toString(dst);
                attrValue = new AttributeValue();
                attrValue.setValue(value);
                _attributeResult.getAttributeValues().add(attrValue);
            }
        }
    }

    /**
     * TODO: natural-language not considered in reporting
     *
     * @param tag
     */
    private void setNameWithLanguageAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }

        // set natural-language and attribute value
        if (!_buf.hasRemaining()) {
            return;
        }

        // set tag, tag name, natural-language
        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            byte[] dst = new byte[length];
            _buf.get(dst);
            String value = IppUtil.toString(dst);
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);
            attrValue.setValue(value);
            _attributeResult.getAttributeValues().add(attrValue);

            // set value
            length = _buf.getShort();
            if ((length != 0) && (_buf.remaining() >= length)) {
                dst = new byte[length];
                _buf.get(dst);
                value = IppUtil.toString(dst);
                attrValue = new AttributeValue();
                attrValue.setValue(value);
                _attributeResult.getAttributeValues().add(attrValue);
            }
        }
    }

    /**
     * @param tag
     */
    private void setBooleanAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }

        // set attribute value
        if (!_buf.hasRemaining()) {
            return;
        }
        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            byte value = _buf.get();
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);
            attrValue.setValue(IppUtil.toBoolean(value));
            _attributeResult.getAttributeValues().add(attrValue);
        }
    }

    /**
     * @param tag
     */
    private void setDateTimeAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }

        // set attribute value
        if (!_buf.hasRemaining()) {
            return;
        }
        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            byte[] dst = new byte[length];
            _buf.get(dst, 0, length);
            String value = IppUtil.toDateTime(dst);
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);
            attrValue.setValue(value);
            _attributeResult.getAttributeValues().add(attrValue);
        }
    }

    /**
     * @param tag
     */
    private void setIntegerAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }
        // set attribute value
        if (!_buf.hasRemaining()) {
            return;
        }
        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            int value = _buf.getInt();
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);
            attrValue.setValue(Integer.toString(value));
            _attributeResult.getAttributeValues().add(attrValue);
        }
    }

    /**
     *
     */
    private void setNoValueAttribute() {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }
    }

    /**
     * @param tag
     */
    private void setRangeOfIntegerAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }
        // set attribute value
        if (!_buf.hasRemaining()) {
            return;
        }
        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            int value1 = _buf.getInt();
            int value2 = _buf.getInt();
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);
            attrValue.setValue(value1 + "," + value2);
            _attributeResult.getAttributeValues().add(attrValue);
        }
    }

    /**
     * @param tag
     */
    private void setResolutionAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }

        // set attribute value
        if (!_buf.hasRemaining()) {
            return;
        }
        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            int value1 = _buf.getInt();
            int value2 = _buf.getInt();
            byte value3 = _buf.get();
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);
            attrValue.setValue(value1 + "," + value2 + "," + Integer.toString(value3));
            _attributeResult.getAttributeValues().add(attrValue);
        }
    }

    /**
     * @param tag
     */
    private void setEnumAttribute(byte tag) {
        short length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            setAttributeName(length);
        }

        // set attribute value
        if (!_buf.hasRemaining()) {
            return;
        }

        length = _buf.getShort();
        if ((length != 0) && (_buf.remaining() >= length)) {
            String hex = IppUtil.toHexWithMarker(tag);
            AttributeValue attrValue = new AttributeValue();
            attrValue.setTag(hex);
            String tagName = getTagName(hex);
            attrValue.setTagName(tagName);

            int value = _buf.getInt();
            if (_attributeResult != null) {
                String enumName = getEnumName(value, _attributeResult.getName());
                attrValue.setValue(enumName);
            } else {
                _attributeResult = new Attribute();
                _attributeResult.setName("no attribute name given:");
                attrValue.setValue(Integer.toString(value));
            }

            _attributeResult.getAttributeValues().add(attrValue);
        }
    }

    /**
     * @param length
     */
    private void setAttributeName(short length) {
        if ((length == 0) || (_buf.remaining() < length)) {
            return;
        }
        byte[] dst = new byte[length];
        _buf.get(dst);
        String name = IppUtil.toString(dst);
        if (_attributeResult != null) {
            _attributeGroupResult.getAttributes().add(_attributeResult);
        }
        _attributeResult = new Attribute();
        _attributeResult.setName(name);
    }

    /**
     * @param tag
     * @return
     */
    private String getTagName(String tag) {
        if (tag == null) {
            log.error("IppResponse.getTagName(): no tag given");
            return null;
        }
        for (Tag value : _tagList) {
            if (tag.equals(value.getValue())) {
                return value.getName();
            }
        }
        return "no name found for tag:" + tag;
    }

    /**
     * @param value
     * @param nameOfAttribute
     * @return
     */
    private String getEnumName(@NonNull String value, @NonNull String nameOfAttribute) {
        int enumValue;
        if (value.contains("0x")) {
            value = value.replace("0x", "");
            enumValue = Integer.parseInt(value, 16);
        } else {
            enumValue = Integer.parseInt(value, 10);
        }
        return getEnumName(enumValue, nameOfAttribute);
    }

    /**
     * @param value
     * @return
     * @nameOfAttribute
     */
    private String getEnumName(int value, @NonNull String nameOfAttribute) {
        for (AttributeGroup attributeGroup : _attributeGroupList) {
            List<Attribute> attributeList = attributeGroup.getAttributes();
            for (Attribute attribute : attributeList) {
                String attributeName = attribute.getName();
                if ((attributeName != null) && (attributeName.equals(nameOfAttribute))) {
                    List<AttributeValue> attributeValueList = attribute.getAttributeValues();
                    for (AttributeValue attributeValue : attributeValueList) {
                        if (attributeValue.getSetOfEnum() != null) {
                            SetOfEnum setOfEnum = attributeValue.getSetOfEnum();
                            Set<Enum> enumList = setOfEnum.getEnums();
                            for (org.cups4j.ipp.attributes.Enum enumEntry : enumList) {
                                String enumValueString = enumEntry.getValue();
                                int enumValue;
                                // some IPP enumerations are in hex, other decimal
                                // see http://www.iana.org/assignments/ipp-registrations for
                                // reference
                                if (enumValueString.contains("0x")) {
                                    enumValueString = enumValueString.replace("0x", "");
                                    enumValue = Integer.parseInt(enumValueString, 16);
                                } else {
                                    enumValue = Integer.parseInt(enumValueString, 10);
                                }
                                if (value == enumValue) {
                                    return enumEntry.getName();
                                }
                            }
                        } else {
                            log.error("IPPResponse.getEnumName(): " + "set-of-enum is null for attribute " + attributeName
                                    + ". Please control " + "the enumeration list in the XML file");
                            return null;
                        }
                    }
                }
            }
        }
        return "enum name not found in IANA list: " + value;
    }
}
