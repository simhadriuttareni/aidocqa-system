package com.simhadri.aidocqa.repository;

import com.simhadri.aidocqa.model.Chunk;
import com.simhadri.aidocqa.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, String> {
    List<Chunk> findByDocument(Document document);

    void deleteByDocument(Document document);
}