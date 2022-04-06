package net.foxes4life.battlecatsdecryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {
    public static final File OUTPUT_DIR = new File("output");

    public static void main(String[] args) {
        File input = new File("input/");String[] files = input.list();

        assert files != null;

        HashMap<File, File> packs = new HashMap<>();
        for (String file : files) {
            if(file.endsWith(".list")) {
                file = new File("input", file).toString();
                String pack = file.substring(0, file.length()-5) + ".pack";
                File packFile = new File(pack);
                if(packFile.exists()) {
                    packs.put(new File(file), packFile);
                }
            }
        }

        Iterator<Map.Entry<File, File>> it = packs.entrySet().iterator();

        while(it.hasNext()) {
            Map.Entry<File, File> e = it.next();
            System.out.println(e.getKey().toString());
            System.out.println(e.getValue().toString());

            File list = e.getKey();
            File pack = e.getValue();

            String packName = list.getName().substring(0, list.getName().length()-5);

            ArrayList<FileEntry> entries = new ArrayList<>();
            try {
                String key = Objects.requireNonNull(Crypto.md5("pack")).substring(0, 16);
                String packListData = new String(Crypto.decrypt(key, Files.readAllBytes(list.toPath())));
                String[] packFiles = packListData.split("\n");
                packFiles = Arrays.copyOfRange(packFiles, 1, packFiles.length);
                for (String packFile : packFiles) {
                    String[] packFileEntry = packFile.split(",");
                    System.out.println(packFile);
                    entries.add(new FileEntry(packFileEntry[0], Integer.parseInt(packFileEntry[1]), Integer.parseInt(packFileEntry[2])));
                }
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException ex) {
                ex.printStackTrace();
            }

            String dataKey = Objects.requireNonNull(Crypto.md5("battlecats")).substring(0, 16);

            try {
                byte[] encryptedPackData = Files.readAllBytes(pack.toPath());
                for (FileEntry entry : entries) {
                    byte[] entryDataEncrypted = Arrays.copyOfRange(encryptedPackData, entry.start, entry.start+entry.size);
                    byte[] entryData = Crypto.decrypt(dataKey, entryDataEncrypted);

                    File output = new File(OUTPUT_DIR, packName);

                    if(!output.exists()) output.mkdirs();
                    if(!output.isDirectory()) {
                        System.out.println("AAAAAAAAA - not a directory");
                        return;
                    }

                    File outputFile = new File(output, entry.name);
                    try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                        outputStream.write(entryData);
                    }
                }
            } catch (IOException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException ex) {
                ex.printStackTrace();
            }
        }
    }
}
