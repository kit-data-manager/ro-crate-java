FROM mcr.microsoft.com/devcontainers/java:25@sha256:816affc3f88ddcf9700e6b23ab171e94b1f84d0046a3a5973449fd12c365c5c0

# Install Node.js LTS
RUN curl -fsSL https://deb.nodesource.com/setup_24.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*
RUN npm install -g ro-crate-html-js

# Verify installations
RUN node --version && npm --version
