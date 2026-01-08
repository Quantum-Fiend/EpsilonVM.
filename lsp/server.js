const {
    createConnection,
    TextDocuments,
    ProposedFeatures,
    DidChangeConfigurationNotification,
    TextDocumentSyncKind,
    CompletionItem,
    CompletionItemKind
} = require('vscode-languageserver/node');

const { TextDocument } = require('vscode-languageserver-textdocument');

// Create a connection for the server
const connection = createConnection(ProposedFeatures.all);

// Create a simple text document manager
const documents = new TextDocuments(TextDocument);

connection.onInitialize((params) => {
    return {
        capabilities: {
            textDocumentSync: TextDocumentSyncKind.Incremental,
            // Tell the client that this server supports code completion.
            completionProvider: {
                resolveProvider: true
            }
        }
    };
});

connection.onInitialized(() => {
    // Register for config changes if needed
});

// The content of a text document has changed.
documents.onDidChangeContent(change => {
    validateTextDocument(change.document);
});

const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');

async function validateTextDocument(textDocument) {
    const text = textDocument.getText();
    const diagnostics = [];

    // Temporary file for validation
    const tempFile = path.join(__dirname, `diag_${Date.now()}.epsilon`);
    fs.writeFileSync(tempFile, text);

    // Call Java Frontend in --check mode
    const cp = path.join(__dirname, '../frontend/bin');
    const cmd = `java -cp "${cp}" com.epsilon.frontend.Main --check "${tempFile}"`;

    exec(cmd, (error, stdout, stderr) => {
        if (stderr) {
            // Parse errors in format [LINE:COL] MESSAGE
            const lines = stderr.split('\n');
            for (const line of lines) {
                const match = line.match(/^\[(\d+):(\d+)\] (.*)/);
                if (match) {
                    const lineNum = parseInt(match[1]) - 1;
                    const colNum = parseInt(match[2]) - 1;
                    const message = match[3];

                    diagnostics.push({
                        severity: 1, // Error
                        range: {
                            start: { line: lineNum, character: colNum },
                            end: { line: lineNum, character: colNum + 10 } // approximation
                        },
                        message: message,
                        source: 'epsilon-frontend'
                    });
                }
            }
        }

        connection.sendDiagnostics({ uri: textDocument.uri, diagnostics });
        if (fs.existsSync(tempFile)) fs.unlinkSync(tempFile);
    });
}

// Completion
connection.onCompletion((_textDocumentPosition) => {
    // return suggested keywords
    return [
        { label: 'fn', kind: CompletionItemKind.Keyword, data: 1 },
        { label: 'var', kind: CompletionItemKind.Keyword, data: 2 },
        { label: 'if', kind: CompletionItemKind.Keyword, data: 3 },
        { label: 'else', kind: CompletionItemKind.Keyword, data: 4 },
        { label: 'while', kind: CompletionItemKind.Keyword, data: 5 },
        { label: 'return', kind: CompletionItemKind.Keyword, data: 6 },
        { label: 'print', kind: CompletionItemKind.Keyword, data: 7 }
    ];
});

connection.onCompletionResolve((item) => {
    if (item.data === 1) {
        item.detail = 'Function definition';
        item.documentation = 'fn name(args) { ... }';
    }
    return item;
});

// Make the text document manager listen on the connection
documents.listen(connection);

// Listen on the connection
connection.listen();
