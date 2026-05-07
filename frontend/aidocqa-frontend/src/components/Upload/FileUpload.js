import React, { useState } from 'react';
import './FileUpload.css';

const FileUpload = ({ onUploadSuccess }) => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [fileType, setFileType] = useState('PDF');
    const [uploading, setUploading] = useState(false);
    const [message, setMessage] = useState('');

    const handleFileChange = (e) => {
        setSelectedFile(e.target.files[0]);
        setMessage('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedFile) {
            setMessage('Please select a file');
            return;
        }

        setUploading(true);
        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('type', fileType);

        try {
            const response = await fetch('http://localhost:8080/api/documents/upload', {
                method: 'POST',
                body: formData,
            });
            const data = await response.json();
            
            if (response.ok) {
                setMessage(`✅ Success! Document "${selectedFile.name}" uploaded.`);
                setSelectedFile(null);
                if (onUploadSuccess) onUploadSuccess();
                // Reset file input
                e.target.reset();
            } else {
                setMessage(`❌ Error: ${data.message || 'Upload failed'}`);
            }
        } catch (error) {
            setMessage(`❌ Error: ${error.message}`);
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="file-upload">
            <h3>📄 Upload Document</h3>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>File Type:</label>
                    <select value={fileType} onChange={(e) => setFileType(e.target.value)}>
                        <option value="PDF">PDF Document</option>
                        <option value="TEXT">Text File</option>
                        <option value="AUDIO">Audio File</option>
                        <option value="VIDEO">Video File</option>
                    </select>
                </div>
                <div className="form-group">
                    <label>Select File:</label>
                    <input 
                        type="file" 
                        onChange={handleFileChange}
                        accept={
                            fileType === 'PDF' ? '.pdf' :
                            fileType === 'TEXT' ? '.txt' :
                            fileType === 'AUDIO' ? 'audio/*' :
                            'video/*'
                        }
                    />
                </div>
                <button type="submit" disabled={uploading}>
                    {uploading ? '⏳ Uploading...' : '🚀 Upload'}
                </button>
            </form>
            {message && <div className="message">{message}</div>}
        </div>
    );
};

export default FileUpload;