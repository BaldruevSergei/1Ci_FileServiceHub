let replaceActive = false;
let originalText = {};

function replaceA() {
    const elements = document.querySelectorAll('*:not(script):not(style)');
    elements.forEach(el => {
        el.childNodes.forEach(node => {
            if (node.nodeType === Node.TEXT_NODE) {
                if (!originalText[node]) {
                    originalText[node] = node.textContent;
                }
                node.textContent = node.textContent.replace(/а/g, () => Math.floor(Math.random() * 10));
            }
        });
    });
}

function restoreA() {
    const elements = document.querySelectorAll('*:not(script):not(style)');
    elements.forEach(el => {
        el.childNodes.forEach(node => {
            if (node.nodeType === Node.TEXT_NODE && originalText[node]) {
                node.textContent = originalText[node];
            }
        });
    });
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action === 'start') {
        replaceActive = true;
        replaceA();
    } else if (message.action === 'stop') {
        replaceActive = false;
        restoreA();
    } else if (message.action === 'resume') {
        replaceActive = true;
        replaceA();
    }
    sendResponse({ status: replaceActive ? "started" : "stopped" });
});
