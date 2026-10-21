package com.keryo.training.common.security;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.Assert.fail;

public class FileSecurityValidatorTest {

    @Test
    public void pdfRequiresPdfSignature() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "course.pdf", "application/pdf", "not-a-pdf".getBytes());
        assertRejected(file);
    }

    @Test
    public void mp4UsesFtypSignatureInsteadOfGenericRiffCheck() {
        byte[] mp4 = new byte[]{0, 0, 0, 24, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};
        FileSecurityValidator.validate(new MockMultipartFile(
                "file", "course.mp4", "video/mp4", mp4));
    }

    @Test
    public void executableRenamedAsPdfIsRejected() {
        assertRejected(new MockMultipartFile(
                "file", "payload.pdf", "application/pdf", new byte[]{'M', 'Z', 0, 0}));
    }

    private void assertRejected(MockMultipartFile file) {
        try {
            FileSecurityValidator.validate(file);
            fail("伪造文件必须被拒绝");
        } catch (SecurityException expected) {
            // expected
        }
    }
}

