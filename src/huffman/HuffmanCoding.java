package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Leo Lawrence
 * Compare with HuffmanProvided.java to see my code vs. the original file.
 */
public class HuffmanCoding {
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                System.exit(1);
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }
    
    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /**
     * Reads a given text file character by character, and returns an arraylist
     * of CharFreq objects with frequency > 0, sorted by frequency
     * 
     * @param filename The text file to read from
     * @return Arraylist of CharFreq objects, sorted by frequency
     */
    public static ArrayList<CharFreq> makeSortedList(String filename) {
        StdIn.setFile(filename);
        int[] charCount = new int[128];
        int size = 0;
        while (StdIn.hasNextChar()) {
            char temp = StdIn.readChar();
            charCount[temp]++;
        }
        for (int i = 0; i < charCount.length; i++) {
            if (charCount[i] != 0) {
                size = size+charCount[i];
            }
        }
        ArrayList<CharFreq> finalList = new ArrayList<CharFreq>();
        int ptr = 0;
        for (int i = 0; i < charCount.length; i++) {
            if (charCount[i] != 0) {
                double probability = charCount[i];
                CharFreq temp = new CharFreq((char)i, probability/size);
                finalList.add(temp);
                ptr = i;
            }
        }
        Collections.sort(finalList);
        if (finalList.size() == 1) {
            if (ptr == 127) {
                ptr = 0;
            }
            else {
                ptr++;
            }
            CharFreq edgeCase = new CharFreq((char) ptr, 0);
            finalList.add(edgeCase);
        }
        Collections.sort(finalList);
        return finalList;
    }

    /**
     * Uses a given sorted arraylist of CharFreq objects to build a huffman coding tree
     * 
     * @param sortedList The arraylist of CharFreq objects to build the tree from
     * @return A TreeNode representing the root of the huffman coding tree
     */
    public static TreeNode makeTree(ArrayList<CharFreq> sortedList) {  // has an null case somewhere...
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();
        CharFreq dataVals = new CharFreq();
        for (int i = 0; i < sortedList.size(); i++) {
            dataVals = sortedList.get(i);
            TreeNode newNode = new TreeNode(dataVals, null, null);
            source.enqueue(newNode);
        }

        while (!source.isEmpty() || target.size() > 1) {
            TreeNode left  = findMin(source, target);
            TreeNode right = findMin(source, target);
            double leftFreq = 0.0;
            double rightFreq = 0.0;
            if (left != null) {
                leftFreq = left.getData().getProbOccurrence();
            }
            if (right != null) {
                rightFreq = right.getData().getProbOccurrence();
            }
            CharFreq sumFreq = new CharFreq(null, (leftFreq + rightFreq));
            TreeNode parent = new TreeNode(sumFreq, left, right);
            target.enqueue(parent);
        }

        return findMin(source, target);
        
    }

    private static TreeNode findMin(Queue<TreeNode> q1, Queue<TreeNode> q2) {
        if (q1.isEmpty()) {
            if (q2.isEmpty()) {
                return null;
            }
            else {
                return q2.dequeue();
            }
        }
        else if (q2.isEmpty()) {
            if (q1.isEmpty()) {
                return null;
            }
            else {
                return q1.dequeue();
            }
        }
        else {
            if (q1.peek().getData().getProbOccurrence() <= q2.peek().getData().getProbOccurrence()) {
                return q1.dequeue();
            }
            else {
                return q2.dequeue();
            }
        }
    }
    /**
     * Uses a given huffman coding tree to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null
     * 
     * @param root The root of the given huffman coding tree
     * @return Array of strings containing only 1's and 0's representing character encodings
     */
    public static String[] makeEncodings(TreeNode root) { //doesnt work for input2
        String[] strArr = new String[128];
        TreeNode rootPtr = root;
        String bitStr = "";
        for (int i = 0; i < strArr.length; i++) {
            bitStr = "";
            findPath(strArr, bitStr, rootPtr);
            rootPtr = root;
        }
        return strArr;
    }

    private static void findPath(String[] strArr, String bitStr, TreeNode tn1) {
        String rtnVal = "";
        if (tn1 != null && !isLeaf(tn1)) {
            findPath(strArr, bitStr + '0', tn1.getLeft());
            findPath(strArr, bitStr + '1', tn1.getRight());
        }
        else if (tn1 != null && isLeaf(tn1)) {
            strArr[tn1.getData().getCharacter()] = bitStr;
        }
    }

    private static boolean isLeaf(TreeNode tn1) {
        if (tn1.getLeft() == null && tn1.getRight() == null) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Using a given string array of encodings, a given text file, and a file name to encode into,
     * this method makes use of the writeBitString method to write the final encoding of 1's and
     * 0's to the encoded file.
     * 
     * @param encodings The array containing binary string encodings for each ASCII character
     * @param textFile The text file which is to be encoded
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public static void encodeFromArray(String[] encodings, String textFile, String encodedFile) {
        StdIn.setFile(textFile);
        String encodedStr = "";
        while (StdIn.hasNextChar()) {
            char temp = StdIn.readChar();
            encodedStr += encodings[(int)temp];
        }
        writeBitString(encodedFile, encodedStr);
    }
    
    /**
     * Using a given encoded file name and a huffman coding tree, this method makes use of the 
     * readBitString method to convert the file into a bit string, then decodes the bit string
     * using the tree, and writes it to a file.
     * 
     * @param encodedFile The file which contains the encoded text we want to decode
     * @param root The root of your Huffman Coding tree
     * @param decodedFile The file which you want to decode into
     */
    public static void decode(String encodedFile, TreeNode root, String decodedFile) {
        String bitStr = readBitString(encodedFile);
        StdOut.setFile(decodedFile); 
        char[] ch = bitStr.toCharArray();
        TreeNode path = new TreeNode();
        path = root;
        int counter = 0;
        while (counter < ch.length) {
            if (isLeaf(path)) {
                StdOut.print(path.getData().getCharacter());
                path = root;
            }
            else if (ch[counter] == '1') {
                path = path.getRight();
                counter++;
            }
            else if (ch[counter] == '0') {
                path = path.getLeft();
                counter++;
            }
        }
        if (isLeaf(path)) {
            StdOut.print(path.getData().getCharacter());
            path = root;
        }
        
    }
}
