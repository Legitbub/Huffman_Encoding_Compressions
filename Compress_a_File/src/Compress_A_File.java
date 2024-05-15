/**
 * Justin Dam, Ethan Ho, Aakarsh Pasi, and Terry Zou
 * Programming Project Part 1
 * A program that compresses a source file into binary using the
 * Huffman coding method then uses BitOutPutStream to output the encoded
 * contents into a target file
 */

import java.io.*;

public class Compress_A_File {

    public static void main(String[] args) throws IOException {

        //Ensures user knows what to input
        if(args.length != 2) {
            System.out.println("Usage: java Compress_A_File filename");
            System.exit(1);
        }

        String filename = args[0];
        File file = new File(filename);
        if(!file.exists()) {
            System.out.println("The file " + filename + " does not exist.");
            System.exit(1);
        }

        //Input: grabs the text from the file
        DataInputStream input = new DataInputStream(new BufferedInputStream(
                new FileInputStream(file)));
        int size = input.available();
        byte[] b = new byte[size];
        input.read(b);
        input.close();
        String text = new String(b);

        //Huffman Process
        int[] frequency = getCharacterFrequency(text); //array of word occurrences
        HuffmanTree tree = getHuffmanTree(frequency); //Creates the tree
        String[] codes = getCode(tree.root);         //Gets the codes
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < text.length(); i++) {
            result.append(codes[text.charAt(i)]);
        }

        //Output: grabs from the file and outputs the binary contents
        BitOutputStream BinaryOutput = new BitOutputStream(new File(args[1]));
        BinaryOutput.getStream().writeObject(codes);
        BinaryOutput.writeBit(result.toString());
        BinaryOutput.close();
    }


    public static String[] getCode(HuffmanTree.Node root) {
        if (root == null) {
            return null;
        }
        // Justin: Create an array to store occurrences of each possible ASCII
        // character encoding
        String[] codes = new String[256];
        assignCode(root, codes);
        return codes;
    }

    // Justin: Go through every node in a path and give it the corresponding
    //"1" or "0" for the code relative to the root
    private static void assignCode(HuffmanTree.Node node, String[] codes) {
        if (node.leftChild != null) {
            if (node.code == null) {
                node.leftChild.code = "0";
                assignCode(node.leftChild, codes);
                node.rightChild.code = "1";
                assignCode(node.rightChild, codes);
            } else {
                node.leftChild.code = node.code + "0";
                assignCode(node.leftChild, codes);
                node.rightChild.code = node.code + "1";
                assignCode(node.rightChild, codes);
            }
        } else {
            codes[(int) node.character] = node.code;
        }
    }

    // Justin: Create a Huffman encoding tree using a heap structure; the
    // heap combines trees containing each character until one ordered
    // Huffman tree is returned
    public static HuffmanTree getHuffmanTree(int[] frequencies) {

        // Heap ensures that added trees remain ordered by their relative
        // character weights
        Heap<HuffmanTree> hTree = new Heap<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                hTree.add(new HuffmanTree(frequencies[i], (char) i));
            }
        }

        while (hTree.getSize() > 1) {
            HuffmanTree subtree1 = hTree.remove();
            HuffmanTree subtree2 = hTree.remove();
            hTree.add(new HuffmanTree(subtree1, subtree2));
        }
        return hTree.remove();
    }

    // Justin: Return an array that holds frequencies of each character in
    // the given file
    public static int[] getCharacterFrequency(String sourceText) {
        int[] frequencies = new int[256];
        for (int i = 0; i < sourceText.length(); i++) {
            frequencies[(char) sourceText.charAt(i)]++;
        }
        return frequencies;
    }

    // Justin: This class represents the tree that stores the characters and
    // their associated codes based on frequency
    public static class HuffmanTree implements Comparable<HuffmanTree> {
        Node root;

        public HuffmanTree(int weight, char character) {
            root = new Node(weight, character);
        }

        // Construct a tree from 2 other trees
        public HuffmanTree(HuffmanTree tree1, HuffmanTree tree2) {
            root = new Node();
            root.leftChild = tree1.root;
            root.rightChild = tree2.root;
            root.weight = tree1.root.weight + tree2.root.weight;
        }

        // Compare 2 trees based on the weight of their roots
        @Override
        public int compareTo (HuffmanTree second) {
            if (root.weight < second.root.weight) {
                return 1;
            } else if (root.weight > second.root.weight) {
                return -1;
            } else {
                return 0;
            }
        }

        // A tree is composed of nodes
        public class Node {
            char character;
            String code;
            int weight;
            Node leftChild;
            Node rightChild;

            public Node() {

            }

            public Node(int weight, char character) {
                this.weight = weight;
                this.character = character;
            }
        }
    }
}

// Heap class used in creating a Huffman Tree
class Heap<E extends Comparable<E>> {
    private java.util.ArrayList<E> list = new java.util.ArrayList<>();

    public Heap() {

    }

    public Heap(E[] objects) {
        for (int i = 0; i < objects.length; i++) {
            add(objects[i]);
        }
    }

    /** Add a new object into the heap */
    public void add(E newObject) {
        list.add(newObject); // Append to the heap
        int currentIndex = list.size() - 1;

        while (currentIndex > 0) {
            int parentIndex = (currentIndex - 1) / 2;
            // Swap if the current object is greater than its parent
            if (list.get(currentIndex).compareTo(list.get(parentIndex)) > 0) {
                E temp = list.get(currentIndex);
                list.set(currentIndex, list.get(parentIndex));
                list.set(parentIndex, temp);
            } else {
                break;
            }

            currentIndex = parentIndex;
        }
    }

    /** Remove the root from the heap */
    public E remove() {
        if (list.size() == 0) {
            return null;
        }
        E removedObject = list.get(0);
        list.set(0, list.get(list.size() - 1));
        list.remove(list.size() - 1);

        int currentIndex = 0;
        while (currentIndex < list.size()) {
            int leftChildIndex = 2 * currentIndex + 1;
            int rightChildIndex = 2 * currentIndex + 2;

            // Find the maximum between two children
            if (leftChildIndex >= list.size()) {
                break; // The tree is a heap
            }
            int maxIndex = leftChildIndex;
            if (rightChildIndex < list.size()) {
                if (list.get(maxIndex).compareTo(list.get(rightChildIndex)) < 0) {
                    maxIndex = rightChildIndex;
                }
            }

            // Swap if the current node is less than the maximum
            if (list.get(currentIndex).compareTo(list.get(maxIndex)) < 0) {
                E temp = list.get(maxIndex);
                list.set(maxIndex, list.get(currentIndex));
                list.set(currentIndex, temp);
                currentIndex = maxIndex;
            } else {
                break; // The tree is a heap
            }
        }
        return removedObject;
    }

    /** Get the number of nodes in the tree */
    public int getSize() {
        return list.size();
    }
}

class BitOutputStream {
    private ObjectOutputStream output;
    private byte currentByte;
    private int bitsDone;

    public BitOutputStream(File file) throws IOException {
        output = new ObjectOutputStream(new FileOutputStream(file));
        bitsDone = 0;
        currentByte = 0;

    }

    public void writeBit(String bitString) throws IOException {
        for(int i = 0; i < bitString.length(); i++) {
            writeBit(bitString.charAt(i));
        }
    }

    public void writeBit(char bit) throws IOException {
        currentByte = (byte)(currentByte << 1);
        currentByte += (bit == '0' ? 0 : 1);
        if(++bitsDone >= 8) {
            output.write(currentByte);
            currentByte = 0;
            bitsDone = 0;
        }
    }

    public void close() throws IOException {
        while (bitsDone != 0) {
            writeBit('0');
        }
        output.close();
    }

    public ObjectOutputStream getStream() {
        return output;
    }
}