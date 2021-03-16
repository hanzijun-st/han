package com.qianlima.offline.service.han;

import org.springframework.http.ResponseEntity;

public interface TestDownService {
    ResponseEntity<byte[]> downFile();
}