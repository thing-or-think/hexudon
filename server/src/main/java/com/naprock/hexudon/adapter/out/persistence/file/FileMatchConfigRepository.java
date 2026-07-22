package com.naprock.hexudon.adapter.out.persistence.file;

import com.naprock.hexudon.adapter.out.persistence.file.dto.MatchConfigDocument;
import com.naprock.hexudon.adapter.out.persistence.file.mapper.MatchConfigMapper;
import com.naprock.hexudon.application.port.out.file.MatchConfigFileStore;
import com.naprock.hexudon.application.port.out.file.MatchConfigRepository;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.exception.repository.ResourceAlreadyExistsException;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.naprock.hexudon.adapter.out.persistence.file.mapper.MatchConfigMapper.toMatchConfig;
import static com.naprock.hexudon.adapter.out.persistence.file.mapper.MatchConfigMapper.toMatchConfigDocument;

/**
 * File-based implementation of {@link MatchConfigRepository}.
 *
 * <p>Stores match configurations in a JSON file.</p>
 */
@Repository
public class FileMatchConfigRepository implements MatchConfigRepository {

    private final MatchConfigFileStore fileStore;

    public FileMatchConfigRepository(MatchConfigFileStore fileStore) {
        this.fileStore = fileStore;
    }

    @Override
    public void save(MatchConfig config) {
        Map<String, MatchConfigDocument> documents = fileStore.load();

        if (documents.containsKey(config.gameId())) {
            throw new ResourceAlreadyExistsException(
                    "MatchConfig",
                    config.gameId()
            );
        }

        documents.put(
                config.gameId(),
                toMatchConfigDocument(config)
        );

        fileStore.save(documents);
    }

    @Override
    public MatchConfig findByGameId(String gameId) {
        MatchConfigDocument document = fileStore.load().get(gameId);

        if (document == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "Match configuration not found for gameId: " + gameId
            );
        }

        return toMatchConfig(document);
    }

    @Override
    public boolean existsByGameId(String gameId) {
        return fileStore.load().containsKey(gameId);
    }

    @Override
    public void deleteByGameId(String gameId) {
        Map<String, MatchConfigDocument> documents = fileStore.load();

        if (documents.remove(gameId) == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "MatchConfig not found: " + gameId
            );
        }

        fileStore.save(documents);
    }

    @Override
    public List<MatchConfig> findAll() {
        return fileStore.load()
                .values()
                .stream()
                .map(MatchConfigMapper::toMatchConfig)
                .toList();
    }
}