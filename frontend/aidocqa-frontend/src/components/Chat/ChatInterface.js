import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import './ChatInterface.css';

const ChatInterface = ({ selectedDocumentId }) => {
    const [question, setQuestion] = useState('');
    const [answer, setAnswer] = useState('');
    const [loading, setLoading] = useState(false);
    const [history, setHistory] = useState([]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!question.trim()) return;

        setLoading(true);
        const userQuestion = question;
        setQuestion('');

        try {
            const response = await fetch('http://localhost:8080/api/chat/ask', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    question: userQuestion,
                    documentId: selectedDocumentId,
                }),
            });
            const data = await response.json();

            const newEntry = {
                question: userQuestion,
                answer: data.answer || 'No answer received',
                timestamp: new Date().toLocaleTimeString(),
            };
            setHistory([newEntry, ...history]);
            setAnswer(data.answer);
        } catch (error) {
            const errorEntry = {
                question: userQuestion,
                answer: `Error: ${error.message}`,
                timestamp: new Date().toLocaleTimeString(),
                isError: true,
            };
            setHistory([errorEntry, ...history]);
            setAnswer('Error occurred. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="chat-interface">
            <div className="chat-header">
                <h3>💬 AI Chat Assistant</h3>
                {selectedDocumentId && <div className="selected-doc">📄 Document selected</div>}
            </div>
            
            <div className="chat-history">
                {history.length === 0 ? (
                    <div className="welcome-message">
                        <p>Ask me questions about your uploaded documents!</p>
                        <p>Example: "What is this document about?" or "Summarize the content"</p>
                    </div>
                ) : (
                    history.map((item, idx) => (
                        <div key={idx} className={`chat-message ${item.isError ? 'error' : ''}`}>
                            <div className="question">
                                <strong>You:</strong> {item.question}
                            </div>
                            <div className="answer">
                                <strong>AI:</strong>
                                <ReactMarkdown>{item.answer}</ReactMarkdown>
                            </div>
                            <div className="timestamp">{item.timestamp}</div>
                        </div>
                    ))
                )}
                {loading && (
                    <div className="loading">
                        <span>🤔 Thinking...</span>
                    </div>
                )}
            </div>

            <form onSubmit={handleSubmit} className="chat-input-form">
                <input
                    type="text"
                    value={question}
                    onChange={(e) => setQuestion(e.target.value)}
                    placeholder="Ask a question about your document..."
                    disabled={loading}
                />
                <button type="submit" disabled={loading}>
                    {loading ? 'Sending...' : 'Send'}
                </button>
            </form>
        </div>
    );
};

export default ChatInterface;