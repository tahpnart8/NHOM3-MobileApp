package com.nhom3.pubgapp.ui.common;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nhom3.pubgapp.databinding.ViewAvatarBinding;

import java.util.Locale;

// Fills a view_avatar include. Every screen that shows a user's avatar calls this, not its own decode.
// The initial is always set; the photo is drawn on top and only covers it once it has decoded.
public final class AvatarBinder {

    // No placeholder and no fallback: a transparent photo view lets the initial underneath show through.
    private static final int NO_DRAWABLE = 0;

    private AvatarBinder() {
    }

    /** The initial of displayName, then the photo from avatarBase64 when there is one. */
    public static void bind(@NonNull ViewAvatarBinding avatar, @Nullable String displayName,
                            @Nullable String avatarBase64) {
        avatar.textInitial.setText(initialOf(displayName));
        avatar.textInitial.setVisibility(View.VISIBLE);
        boolean hasPhoto = avatarBase64 != null && !avatarBase64.trim().isEmpty();
        // ImageLoader tags the view with the string it shows; the same photo again would only blink.
        if (hasPhoto && avatarBase64.equals(avatar.imageAvatar.getTag())) {
            avatar.imageAvatar.setVisibility(View.VISIBLE);
            return;
        }
        // Called even without a photo: it clears the tag, so a late decode for a recycled row is dropped.
        ImageLoader.load(hasPhoto ? avatarBase64 : null, avatar.imageAvatar, NO_DRAWABLE, NO_DRAWABLE);
        avatar.imageAvatar.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);
    }

    /** First letter of the trimmed name in upper case, or an empty string when there is no name. */
    static String initialOf(@Nullable String displayName) {
        if (displayName == null) {
            return "";
        }
        String trimmed = displayName.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        int firstLength = Character.charCount(trimmed.codePointAt(0));
        return trimmed.substring(0, firstLength).toUpperCase(Locale.ROOT);
    }
}
