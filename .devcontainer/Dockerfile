FROM mcr.microsoft.com/devcontainers/java:25@sha256:748d72150760bf624e2f5c0805442965ffaa69b7cc80e61b63e98ba4ebe7d20b

# Install Node.js LTS
RUN curl -fsSL https://deb.nodesource.com/setup_24.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*
RUN npm install -g ro-crate-html-js

# Verify installations
RUN node --version && npm --version
