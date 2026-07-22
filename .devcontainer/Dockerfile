FROM mcr.microsoft.com/devcontainers/java:25@sha256:5549bc9f2176f4ce864e5774c2dedd400085a45bd145d51bf322806008243481

# Install Node.js LTS
RUN curl -fsSL https://deb.nodesource.com/setup_24.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*
RUN npm install -g ro-crate-html-js

# Verify installations
RUN node --version && npm --version
