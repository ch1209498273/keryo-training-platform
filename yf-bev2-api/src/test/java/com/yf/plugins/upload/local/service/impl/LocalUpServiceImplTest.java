package com.yf.plugins.upload.local.service.impl;

import com.yf.plugins.upload.local.config.LocalConfig;
import com.yf.system.modules.plugin.service.PluginDataService;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class LocalUpServiceImplTest {

    @Test
    public void getRealPath_resolvesWithinConfiguredRoot() {
        LocalConfig config = new LocalConfig();
        config.setLocalDir("C:\\keryo-upload");
        LocalUpServiceImpl service = new LocalUpServiceImpl(mock(PluginDataService.class));

        String actual = service.getRealPath(config, "/upload/file/2026/08/training.pdf");

        Path expected = Paths.get("C:\\keryo-upload", "2026", "08", "training.pdf")
                .toAbsolutePath().normalize();
        assertEquals(expected.toString(), actual);
    }

    @Test
    public void getRealPath_rejectsTraversal() {
        LocalConfig config = new LocalConfig();
        config.setLocalDir("C:\\keryo-upload");
        LocalUpServiceImpl service = new LocalUpServiceImpl(mock(PluginDataService.class));

        assertNull(service.getRealPath(config, "/upload/file/../../secret.txt"));
        assertNull(service.getRealPath(config, "/upload/file/%2e%2e/%2e%2e/secret.txt"));
    }
}

