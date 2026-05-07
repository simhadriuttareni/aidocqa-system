import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Document APIs
export const uploadDocument = async (file, type) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    
    const response = await api.post('/documents/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.data;
};

export const getAllDocuments = async () => {
    const response = await api.get('/documents');
    return response.data;
};

export const getDocument = async (id) => {
    const response = await api.get(`/documents/${id}`);
    return response.data;
};

// Chat APIs
export const askQuestion = async (question, documentId = null) => {
    const response = await api.post('/chat/ask', { question, documentId });
    return response.data;
};

export default api;