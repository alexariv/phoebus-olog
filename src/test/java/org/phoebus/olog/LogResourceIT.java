package org.phoebus.olog;

import junitx.framework.FileAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.phoebus.olog.entity.Attachment;
import org.phoebus.olog.entity.Log;
import org.phoebus.olog.entity.Logbook;
import org.phoebus.olog.entity.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Principal;
import java.time.Instant;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AuthenticationManager.class)
@ContextConfiguration(classes = {LogResource.class, LogRepository.class, ElasticConfig.class, GridFsOperations.class})
@TestPropertySource(locations = "classpath:test_application.properties")
@SuppressWarnings("unused")
class LogResourceIT extends ResourcesTestBase {

    @Autowired
    LogResource logResource;

    @Autowired
    private LogbookRepository logbookRepository;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private GridFsOperations gridOperation;


    private static final String testOwner = "log-resource-test";

    // Read the elatic index and type from the application.properties
    @Value("${elasticsearch.logbook.index:olog_logbooks}")
    private String ES_LOGBOOK_INDEX;
    @Value("${elasticsearch.logbook.type:olog_logbook}")
    private String ES_LOGBOOK_TYPE;
    @Value("${elasticsearch.log.index:olog_logs}")
    private String ES_LOG_INDEX;
    @Value("${elasticsearch.log.type:olog_log}")
    private String ES_LOG_TYPE;

    private static Logbook logbook1;
    private static Logbook logbook2;

    private static MultipartFile multipartFile1;
    private static MultipartFile multipartFile2;

    @BeforeAll
    public static void init() {
        logbook1 = new Logbook("name1", "user");
        logbook2 = new Logbook("name2", "user");

        multipartFile1 = new MultipartFile() {
            @Override
            public String getName() {
                return "Tulips.jpg";
            }

            @Override
            public String getOriginalFilename() {
                return "Tulips.jpg";
            }

            @Override
            public String getContentType() {
                return "image/jpg";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public byte[] getBytes() {
                return new byte[0];
            }

            @Override
            public InputStream getInputStream() {
                return getClass().getResourceAsStream("/Tulips.jpg");
            }

            @Override
            public void transferTo(File dest) throws IllegalStateException {

            }
        };

        multipartFile2 = new MultipartFile() {
            @Override
            public String getName() {
                return "Magnolia.jpg";
            }

            @Override
            public String getOriginalFilename() {
                return "Magnolia.jpg";
            }

            @Override
            public String getContentType() {
                return "image/jpg";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public byte[] getBytes() {
                return new byte[0];
            }

            @Override
            public InputStream getInputStream() {
                return getClass().getResourceAsStream("/Magnolia.jpg");
            }

            @Override
            public void transferTo(File dest) throws IllegalStateException {

            }
        };
    }

    @Test
    void createLogEntryAndRetrieveAttachment() {
        File testFile = new File("src/test/resources/SampleTextFile_100kb.txt");

        try {
            MockMultipartFile mock = new MockMultipartFile(testFile.getName(), new FileInputStream(testFile));
            Attachment testAttachment = new Attachment(mock, "SampleTextFile_100kb.txt", "");

            Logbook testLogbook = new Logbook("test-logbook-1", testOwner, State.Active);
            logbookRepository.save(testLogbook);

            Log log = Log.LogBuilder.createLog("This is a test entry")
                    .owner(testOwner)
                    .withLogbook(testLogbook)
                    .withAttachment(testAttachment)
                    .build();

            Log createdLog = logRepository.save(log);

            String attachmentId = createdLog.getAttachments().iterator().next().getId();
            Resource a = logResource.getAttachment(createdLog.getId().toString(), testFile.getName()).getBody();

            File foundTestFile = new File("LogResourceIT_attachment_" + testAttachment.getId() + "_" + testAttachment.getFilename());
            Files.copy(a.getInputStream(), foundTestFile.toPath());
            FileAssert.assertBinaryEquals("failed to create log entry with attachment", testFile, foundTestFile);
            Files.delete(foundTestFile.toPath());
            gridOperation.delete(new Query(Criteria.where("_id").is(attachmentId)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateLogEntryWithAttachmentAndUpdateWithNewAttachment() throws Exception{

        Instant now = Instant.now();
        String uuid = UUID.randomUUID().toString();
        Attachment attachment = new Attachment();
        attachment.setId(uuid);
        attachment.setFilename(uuid + "_tulips.jpg");
        Log log = Log.LogBuilder.createLog()
                .id(1L)
                .owner("user")
                .title("title")
                .withLogbooks(Set.of(logbook1, logbook2))
                .source("description1")
                .description("description1")
                .createDate(now)
                .modifyDate(now)
                .level("Urgent")
                .build();
        SortedSet<Attachment> attachments = new TreeSet<>();
        attachments.add(attachment);
        log.setAttachments(attachments);

        MockMultipartFile file1 =
                new MockMultipartFile("files", uuid + "_tulips.jpg", "image/jpeg",
                        getClass().getResourceAsStream("/Tulips.jpg").readAllBytes());

        MockMultipartFile log1 = new MockMultipartFile("logEntry", "", "application/json", objectMapper.writeValueAsString(log).getBytes());


        logResource.createLog("n/a", "markdown", null, log, new MultipartFile[]{log1, file1}, new Principal() {
            @Override
            public String getName() {
                return "user";
            }
        });

        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.multipart(HttpMethod.PUT,
                                "/" + OlogResourceDescriptors.LOG_RESOURCE_URI + "/multipart")
                        .file(file1)
                        .file(log1)
                        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION)
                        .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data")
                        .contentType(JSON);
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();


    }
}
