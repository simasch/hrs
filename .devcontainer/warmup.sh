#!/bin/bash
set -e
# Pre-fetch Maven dependencies and the Vaadin frontend
./mvnw -q -B dependency:go-offline
# Pre-install the AIUP agent plugins (wired up in devcontainer.json via chat.pluginLocations)
git clone --depth 1 https://github.com/ai-unified-process/marketplace.git "$HOME/.aiup/marketplace"
# Browser for the Playwright MCP of the aiup-vaadin-jooq plugin
npx -y playwright install --with-deps chromium
