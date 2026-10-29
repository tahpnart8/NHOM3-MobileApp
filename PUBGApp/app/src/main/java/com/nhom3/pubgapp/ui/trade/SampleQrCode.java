package com.nhom3.pubgapp.ui.trade;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Random;

// Draws a QR-like picture for the simulated bank transfer. It is decoration only and cannot be scanned.
public final class SampleQrCode {

    public static final int MODULE_COUNT = 25;
    private static final int FINDER_SIZE = 7;
    private static final int QUIET_ZONE = 2;
    private static final String TRANSFER_PREFIX = "PUBG";
    private static final int SHORT_CODE_LENGTH = 8;

    private SampleQrCode() {
    }

    /** The transfer content shown under the QR: "PUBG" and the first characters of the deal id. */
    @NonNull
    public static String buildTransferContent(@Nullable String dealId) {
        if (dealId == null || dealId.trim().isEmpty()) {
            return TRANSFER_PREFIX;
        }
        String trimmed = dealId.trim();
        int length = Math.min(SHORT_CODE_LENGTH, trimmed.length());
        return TRANSFER_PREFIX + trimmed.substring(0, length).toUpperCase(Locale.ROOT);
    }

    /** True means a dark square. The same content always gives the same picture. */
    @NonNull
    public static boolean[][] buildModules(@NonNull String content) {
        boolean[][] modules = new boolean[MODULE_COUNT][MODULE_COUNT];
        Random random = new Random(content.hashCode());
        for (int row = 0; row < MODULE_COUNT; row++) {
            for (int column = 0; column < MODULE_COUNT; column++) {
                modules[row][column] = random.nextBoolean();
            }
        }
        int farEdge = MODULE_COUNT - FINDER_SIZE;
        drawFinder(modules, 0, 0);
        drawFinder(modules, 0, farEdge);
        drawFinder(modules, farEdge, 0);
        return modules;
    }

    @NonNull
    public static Bitmap createBitmap(@NonNull String content, int sizePx, int darkColor, int lightColor) {
        boolean[][] modules = buildModules(content);
        int cellsPerSide = MODULE_COUNT + QUIET_ZONE * 2;
        int cellSize = Math.max(1, sizePx / cellsPerSide);
        int bitmapSize = cellSize * cellsPerSide;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(lightColor);
        Paint paint = new Paint();
        paint.setColor(darkColor);
        for (int row = 0; row < MODULE_COUNT; row++) {
            for (int column = 0; column < MODULE_COUNT; column++) {
                if (modules[row][column]) {
                    int left = (column + QUIET_ZONE) * cellSize;
                    int top = (row + QUIET_ZONE) * cellSize;
                    canvas.drawRect(left, top, left + cellSize, top + cellSize, paint);
                }
            }
        }
        return bitmap;
    }

    // The three corner squares are what make the picture read as a QR code.
    private static void drawFinder(boolean[][] modules, int top, int left) {
        for (int row = -1; row <= FINDER_SIZE; row++) {
            for (int column = -1; column <= FINDER_SIZE; column++) {
                setModule(modules, top + row, left + column, isFinderDark(row, column));
            }
        }
    }

    private static boolean isFinderDark(int row, int column) {
        boolean insideSquare = row >= 0 && row < FINDER_SIZE && column >= 0 && column < FINDER_SIZE;
        if (!insideSquare) {
            return false;
        }
        boolean onBorder = row == 0 || row == FINDER_SIZE - 1 || column == 0 || column == FINDER_SIZE - 1;
        boolean inCentre = row >= 2 && row <= 4 && column >= 2 && column <= 4;
        return onBorder || inCentre;
    }

    private static void setModule(boolean[][] modules, int row, int column, boolean dark) {
        if (row < 0 || row >= MODULE_COUNT || column < 0 || column >= MODULE_COUNT) {
            return;
        }
        modules[row][column] = dark;
    }
}
