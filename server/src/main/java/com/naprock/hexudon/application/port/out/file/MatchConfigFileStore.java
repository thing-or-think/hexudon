package com.naprock.hexudon.application.port.out.file;

import com.naprock.hexudon.adapter.out.persistence.file.dto.MatchConfigDocument;

import java.util.Map;

public interface MatchConfigFileStore {

    Map<String, MatchConfigDocument> load();

    void save(Map<String, MatchConfigDocument> documents);

}