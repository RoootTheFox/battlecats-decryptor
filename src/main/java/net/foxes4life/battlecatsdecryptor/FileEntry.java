package net.foxes4life.battlecatsdecryptor;

public class FileEntry {
    public String name;
    public int start;
    public int size;

    public FileEntry(String name, int start, int size) {
        this.name = name;
        this.start = start;
        this.size = size;
    }
}
