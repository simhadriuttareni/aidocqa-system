// repository/DocumentRepository.java
package com.simhadri.aidocqa.repository;

import com.simhadri.aidocqa.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
}