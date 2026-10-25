package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.nhom3.pubgapp.model.enums.ImagePurpose;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MediaRepositoryTest {

    // The images rule in firebase/firestore.rules allows exactly these keys on create.
    @Test
    public void buildImageDocument_usesOnlyKeysTheRuleAllows() {
        Map<String, Object> image = MediaRepository.buildImageDocument(
                "uid1", ImagePurpose.VERIFICATION, "QUJD", 600, 380, 1515);

        Set<String> expected = new HashSet<String>(Arrays.asList(
                "ownerId", "purpose", "data", "width", "height", "sizeBytes", "createdAt"));
        assertEquals(expected, image.keySet());
    }

    @Test
    public void buildImageDocument_neverCarriesUpdatedAt() {
        Map<String, Object> image = MediaRepository.buildImageDocument(
                "uid1", ImagePurpose.LISTING, "QUJD", 10, 10, 3);

        assertFalse(image.containsKey("updatedAt"));
        assertFalse(image.containsKey("id"));
    }

    @Test
    public void buildImageDocument_writesPurposeAsEnumName() {
        Map<String, Object> image = MediaRepository.buildImageDocument(
                "uid1", ImagePurpose.WAYBILL, "QUJD", 10, 10, 3);

        assertEquals("WAYBILL", image.get("purpose"));
        assertEquals("uid1", image.get("ownerId"));
    }
}
