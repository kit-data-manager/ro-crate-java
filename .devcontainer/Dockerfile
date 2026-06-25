FROM mcr.microsoft.com/devcontainers/java:25

# Install Node.js LTS
RUN curl -fsSL https://deb.nodesource.com/setup_24.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*
RUN npm install -g ro-crate-html-js

# Verify installations
RUN node --version && npm --version
