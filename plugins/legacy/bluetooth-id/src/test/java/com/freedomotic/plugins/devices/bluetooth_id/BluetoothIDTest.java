package com.freedomotic.plugins.devices.bluetooth_id;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BluetoothIDTest {

    @Test
    public void saveImageCopiesUrlContent() throws Exception {
        Path source = Files.createTempFile("bluetooth-id-source", ".jpg");
        Path destination = Files.createTempFile("bluetooth-id-destination", ".jpg");
        byte[] content = "image content".getBytes(StandardCharsets.UTF_8);
        Files.write(source, content);

        try {
            BluetoothID.saveImage(source.toUri().toURL().toString(), destination.toString());

            assertArrayEquals(content, Files.readAllBytes(destination));
        } finally {
            Files.deleteIfExists(source);
            Files.deleteIfExists(destination);
        }
    }
}
