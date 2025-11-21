.PHONY: convert install-deps clean help venv

# Python virtual environment
VENV := .venv
PYTHON := $(VENV)/bin/python3
PIP := $(VENV)/bin/pip

# Default target
all: convert

# Create virtual environment
venv:
	@if [ ! -d "$(VENV)" ]; then \
		echo "Creating virtual environment..."; \
		python3 -m venv $(VENV); \
		echo "Virtual environment created!"; \
	fi

# Install required Python dependencies in venv
install-deps: venv
	@echo "Installing Python dependencies in virtual environment..."
	$(PIP) install ebooklib beautifulsoup4 lxml
	@echo "Dependencies installed!"

# Convert EPUB files to formatted text
convert: install-deps
	@echo "Converting EPUB files to formatted text..."
	$(PYTHON) convert_epubs.py

# Clean generated files (use with caution!)
clean:
	@echo "Cleaning generated files..."
	@rm -f ../app/src/main/res/raw/capital_de.txt
	@rm -f ../app/src/main/res/raw/capital_en.txt
	@rm -f ../app/src/main/res/raw/capital_es.txt
	@echo "Done!"

# Clean everything including venv
clean-all: clean
	@echo "Removing virtual environment..."
	@rm -rf $(VENV)
	@echo "Done!"

# Show help
help:
	@echo "Available targets:"
	@echo "  make convert       - Convert EPUB files to formatted text (default)"
	@echo "  make install-deps  - Install required Python dependencies in venv"
	@echo "  make venv         - Create virtual environment"
	@echo "  make clean        - Remove generated text files"
	@echo "  make clean-all    - Remove generated files and virtual environment"
	@echo "  make help         - Show this help message"
