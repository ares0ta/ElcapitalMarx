#!/usr/bin/env python3
"""
Convert EPUB files from Capital by Karl Marx to plain text files
for the Android app.

Extracts plain text content from EPUB files.
"""

import os
import sys
import re
from pathlib import Path

try:
    import ebooklib
    from ebooklib import epub
    from bs4 import BeautifulSoup
except ImportError:
    print("Error: Required libraries not found.")
    print("Please install them with:")
    print("  pip install ebooklib beautifulsoup4 lxml")
    sys.exit(1)


def extract_text_from_epub(epub_path):
    """Extract text content from an EPUB file."""
    book = epub.read_epub(epub_path)
    
    chapters = []
    for item in book.get_items():
        if item.get_type() == ebooklib.ITEM_DOCUMENT:
            soup = BeautifulSoup(item.get_content(), 'html.parser')
            text = soup.get_text()
            
            # Clean up the text
            text = re.sub(r'\n\s*\n\s*\n+', '\n\n', text)  # Remove excessive newlines
            text = text.strip()
            
            if text:
                chapters.append(text)
    
    return '\n\n'.join(chapters)


def format_text(text):
    """
    Simple text formatting - just clean text.
    """
    return text


def process_epub_files(raw_dir, output_dir):
    """Process all EPUB files and generate formatted output."""
    raw_path = Path(raw_dir)
    output_path = Path(output_dir)
    
    # Ensure output directory exists
    output_path.mkdir(parents=True, exist_ok=True)
    
    # Language mappings
    languages = {
        'german': ('raw.epub', 'capital_de.txt'),
        'english': (['raw1.epub', 'raw2.epub', 'raw3.epub'], 'capital_en.txt'),
        'spanish': ('raw.epub', 'capital_es.txt'),
    }
    
    for lang, (epub_files, output_file) in languages.items():
        print(f"\nProcessing {lang}...")
        
        # Handle single or multiple EPUB files
        if isinstance(epub_files, str):
            epub_files = [epub_files]
        
        all_text = []
        for epub_file in epub_files:
            epub_path = raw_path / lang / epub_file
            
            if not epub_path.exists():
                print(f"  Warning: {epub_path} not found, skipping...")
                continue
            
            print(f"  Extracting text from {epub_file}...")
            text = extract_text_from_epub(str(epub_path))
            all_text.append(text)
        
        if not all_text:
            print(f"  Error: No EPUB files found for {lang}")
            continue
        
        # Combine all volumes
        combined_text = '\n\n'.join(all_text)
        
        # Format text
        print(f"  Formatting text...")
        formatted_text = format_text(combined_text)
        
        # Write output
        output_file_path = output_path / output_file
        print(f"  Writing to {output_file_path}...")
        with open(output_file_path, 'w', encoding='utf-8') as f:
            f.write(formatted_text)
        
        print(f"  ✓ {lang} completed!")


def main():
    """Main entry point."""
    # Determine paths relative to script location
    script_dir = Path(__file__).parent
    raw_dir = script_dir / 'raw'
    output_dir = script_dir.parent / 'app' / 'src' / 'main' / 'res' / 'raw'
    
    print(f"Script directory: {script_dir}")
    print(f"Input directory: {raw_dir}")
    print(f"Output directory: {output_dir}")
    
    if not raw_dir.exists():
        print(f"\nError: Input directory {raw_dir} not found!")
        sys.exit(1)
    
    process_epub_files(raw_dir, output_dir)
    
    print("\n" + "="*60)
    print("Conversion complete!")
    print("="*60)


if __name__ == '__main__':
    main()

