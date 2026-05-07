import React, { useState } from 'react';
import FileUpload from './components/Upload/FileUpload';
import ChatInterface from './components/Chat/ChatInterface';
import DocumentList from './components/Documents/DocumentList';
import './App.css';

function App() {
    const [selectedDocumentId, setSelectedDocumentId] = useState(null);

    return (
        <div className="app">
            <header className="app-header">
                <h1>🤖 AI Document Q&A System</h1>
                <p>Upload PDFs, Text, Audio, or Video - Ask Questions - Get AI Answers</p>
            </header>

            <div className="app-container">
                <div className="sidebar">
                    <FileUpload onUploadSuccess={() => {
                        // Refresh document list logic will be added
                        window.location.reload();
                    }} />
                    <DocumentList 
                        onSelectDocument={setSelectedDocumentId}
                        selectedDocumentId={selectedDocumentId}
                    />
                </div>

                <div className="main-content">
                    <ChatInterface selectedDocumentId={selectedDocumentId} />
                </div>
            </div>
        </div>
    );
}

export default App;