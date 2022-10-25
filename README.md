# HuffmanCoding
Implementation of the Huffman Algorithm to compress files.

**Test the program by:**
- Download and unzip the folder (or git clone)
- Navigate to the folder in the terminal
- Compile with javac -d bin src/huffman/*.java
- Run with java -cp bin huffman.Driver

**Each method has a different purpose:**

**makeSortedList** sorts the characters from least to greatest according to how frequently they appear in the file.

**makeTree** takes the probability of each character & each group of characters and prints the output as a tree.

**makeEncodings** encodes the data based on character frequency (lower characters have a shorter encoding).

**encodeFromArray** compresses the file using the Huffman algorithm and previous methods.

**decode** decodes the file that was previously encoded from the encodeFromArray.

These five methods were made entirely by myself. All other code was provided from class.
