import React, { useState, useEffect } from 'react';
import './DocumentList.css';

const DocumentList = ({ onSelectDocument, selectedDocumentId }) => {
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchDocuments = async () => {
        setLoading(true);
        try {
            const response = await fetch('http://localhost:8080/api/documents');
            const data = await response.json();
            setDocuments(data);
        } catch (error) {
            console.error('Error fetching documents:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDocuments();
    }, []);

    const getFileIcon = (fileType) => {
        switch (fileType?.toUpperCase()) {
            case 'PDF': return '📄';
            case 'TEXT': return '📝';
            case 'AUDIO': return '🎵';
            case 'VIDEO': return '🎬';
            default: return '📁';
        }
    };

    return (
        <div className="document-list">
            <div className="document-header">
                <h3>📚 Your Documents</h3>
                <button onClick={fetchDocuments} className="refresh-btn">🔄 Refresh</button>
            </div>
            {loading && <div className="loading">Loading...</div>}
            <div className="document-items">
                {documents.length === 0 ? (
                    <div className="no-documents">
                        <p>No documents uploaded yet.</p>
                        <p>Upload a PDF or text file to get started!</p>
                    </div>
                ) : (
                    documents.map((doc) => (
                        <div
                            key={doc.id}
                            className={`document-item ${selectedDocumentId === doc.id ? 'selected' : ''}`}
                            onClick={() => onSelectDocument(doc.id)}
                        >
                            <div className="doc-icon">{getFileIcon(doc.fileType)}</div>
                            <div className="doc-info">
                                <div className="doc-name" title={doc.filename}>
                                    {doc.filename}
                                </div>
                                <div className="doc-meta">
                                    <span className="doc-type">{doc.fileType}</span>
                                    <span className="doc-date">
                                        {new Date(doc.createdAt).toLocaleDateString()}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default DocumentList;