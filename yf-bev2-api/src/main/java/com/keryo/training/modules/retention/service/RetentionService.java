package com.keryo.training.modules.retention.service;

import java.util.Map;

/**
 * Retention archive operations. Archive is intentionally non-destructive.
 */
public interface RetentionService {

    Map<String, Long> preview();

    Map<String, Long> archive();
}

