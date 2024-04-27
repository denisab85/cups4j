package org.cups4j.operations;

import lombok.SneakyThrows;
import org.cups4j.operations.ipp.IppSendDocumentOperation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

public class OperationsTest {

    @ParameterizedTest
    @MethodSource("generateClassSelection")
    @SneakyThrows
    public void testGetIppHeader(Class<? extends IppOperation> operationClass) {
        IppOperation operation = operationClass.newInstance();
        ByteBuffer header;
        if (operationClass.equals(IppSendDocumentOperation.class)) {
            Map<String, String> map = new HashMap<>();
            map.put("job-id", "5678");
            map.put("last-document", "true");
            map.put("job-attributes", "attr1:value1#attr2:value2#attr3:value3");
            header = operation.getIppHeader(new URL("http://test.url"), map);
        } else {
            header = operation.getIppHeader(new URL("http://test.url"));
        }
        System.out.print(header);
    }

    @ParameterizedTest
    @MethodSource("generateClassSelection")
    @SneakyThrows
    public void testGetIppHeaderWithAttributes(Class<? extends IppOperation> operationClass) {
        IppOperation operation = operationClass.newInstance();
        Map<String, String> map = new HashMap<>();
        map.put("requesting-user-name", "requesting-user-name");
        map.put("limit", "1234");
        map.put("requested-attributes", "attribute1 attribute2 attribute3");

        if (operationClass.equals(IppSendDocumentOperation.class)) {
            map.put("job-id", "5678");
            map.put("last-document", "true");
            map.put("job-attributes", "attr1:value1#attr2:value2#attr3:value3");
        }

        ByteBuffer header = operation.getIppHeader(new URL("http://test.url"), map);
        System.out.print(header);
    }

    public static List<Class<? extends IppOperation>> generateClassSelection() {
        List<Class<? extends IppOperation>> list = new ArrayList<>();
        Reflections reflections = new Reflections(IppOperation.class.getPackage().getName());
        Set<Class<? extends IppOperation>> classes = reflections.getSubTypesOf(IppOperation.class);
        for (Class<? extends IppOperation> clazz : classes) {
            if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                list.add(clazz);
            }
        }
        return list;
    }

}
