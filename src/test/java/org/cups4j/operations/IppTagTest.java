package org.cups4j.operations;

import ch.ethz.vppserver.ippclient.IppTag;
import cups4j.TestUtil;
import lombok.SneakyThrows;
import org.cups4j.operations.ipp.IppSendDocumentOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class IppTagTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private MockedStatic<IppTag> ippTagMocked;

    @BeforeEach
    public void setUp() {
        // Don't increment request-id to allow comparison
        ippTagMocked.when(IppTag::getNextRequestID).thenReturn(1);
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
        Path expectedFilePath = Paths.get("src/test/resources/operations/header/with-attributes", operationClass.getSimpleName());
        ByteBuffer expected = TestUtil.readByteBuffer(expectedFilePath);
        Assertions.assertEquals(expected, header);
    }

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
        Path expectedFilePath = Paths.get("src/test/resources/operations/header/default", operationClass.getSimpleName());
        ByteBuffer expected = TestUtil.readByteBuffer(expectedFilePath);
        Assertions.assertEquals(expected, header);
    }

}
