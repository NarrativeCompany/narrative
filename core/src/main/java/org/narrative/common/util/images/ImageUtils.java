package org.narrative.common.util.images;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.network.core.statistics.StatisticManager;
import org.im4java.core.ConvertCmd;
import org.im4java.core.ETOperation;
import org.im4java.core.ExiftoolCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.Pipe;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

public class ImageUtils {
    private static final NarrativeLogger logger = new NarrativeLogger(ImageUtils.class);

    private static List<String> runIdentifyOperation(IMOperation operation) {
        return runIdentifyOperation(operation, null);
    }

    private static List<String> runIdentifyOperation(IMOperation operation, InputStream input) {
        try {
            IdentifyCmd identifyCommand = new IdentifyCmd();

            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            if (input != null) {
                identifyCommand.setInputProvider(new Pipe(input, null));
            }
            identifyCommand.setOutputConsumer(output);
            identifyCommand.run(operation);

            return output.getOutput();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed issuing identify operation: " + operation, e);
            }
        }
        return null;
    }

    private static ObjectPair<Boolean, Throwable> runConvertOperation(IMOperation operation, File in, File out) {
        Throwable t;
        try {
            ConvertCmd convertCommand = new ConvertCmd();

            convertCommand.run(operation);

            return new ObjectPair<Boolean, Throwable>(true, null);

        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed converting image: " + in.getAbsolutePath() + " with operation: " + operation, e);
            }
            t = e;
        }

        //Need to do this as very small images will cause ImageMagick to think there are errors.
        return new ObjectPair<Boolean, Throwable>(ImageUtils.getImageProperties(out) != null, t);
    }

    // identify -format "%m %G" favicon.ico
    // http://www.imagemagick.org/script/identify.php
    // http://www.imagemagick.org/script/escape.php
    // jw: changing format to make image magic give us separate height and width attributes as well as the number of
    //     images in sequence
    // bl: changing to use %W and %H, which are needed in order to determine proper dimensions for animated gifs.
    // additionally, adding an extra newline at the end in order to separate each frame from the result.
    // we'll just assume that the first frame returned holds true for all of the other frames (which ought to be a safe assumption).
    private static final String IDENTIFY_IMAGE_FIELDS = "%m\n%W\n%H\n%n\n";

    public static ImageProperties getImageProperties(byte[] fileData) {
        IMOperation operation = new IMOperation();
        operation.format(IDENTIFY_IMAGE_FIELDS);
        // lets just stream the file into the identify command using standard in
        operation.addImage("-");

        return getImagePropertiesFromResults(runIdentifyOperation(operation, new ByteArrayInputStream(fileData)));
    }

    public static ImageProperties getImageProperties(File in) {
        IMOperation operation = new IMOperation();
        operation.format(IDENTIFY_IMAGE_FIELDS);
        operation.addImage(in.getAbsolutePath());

        return getImagePropertiesFromResults(runIdentifyOperation(operation));
    }

    private static ImageProperties getImagePropertiesFromResults(List<String> results) {
        if (!isEmptyOrNull(results)) {
            ImageInfoType format = ImageInfoType.getFormatForFileFormat(results.get(0));
            if (format != null) {
                try {
                    int width = Integer.valueOf(results.get(1));
                    int height = Integer.valueOf(results.get(2));
                    int numberOfImages = Integer.valueOf(results.get(3));

                    return new ImageProperties(width, height, numberOfImages, format, results.get(0));
                } catch (NumberFormatException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed parsing image dimensions.", e);
                    }
                }
            }
        }

        return null;
    }

    /**
     * determine the given image's orientation
     */
    public static ImageOrientationType getImageOrientationType(File in) {
        // http://www.imagemagick.org/Usage/photos/#orient
        IMOperation operation = new IMOperation();
        operation.format("'%[exif:orientation]'");
        operation.addImage(in.getAbsolutePath());

        List<String> result = runIdentifyOperation(operation);
        if (!isEmptyOrNull(result)) {
            String output = result.get(0);
            if (!isEmpty(output)) {
                output = output.replaceAll("\\'", "");
                output = output.trim();
            }

            if (!isEmpty(output)) {
                try {
                    int orientation = Integer.parseInt(output);

                    // bl: an orientation value of 0 means it's undefined, so ignore that case
                    if(orientation!=0) {
                        // jw: Let's convert the orientation integer into a useful Enum!
                        ImageOrientationType orientationType = EnumRegistry.getForId(ImageOrientationType.class, orientation, false);

                        if (orientationType != null) {
                            return orientationType;
                        }

                        String warningMessage = "Encountered an orientation value that does not map to a known value: " + orientation + ". We should add support for this, or figure out why it came out of this call.";
                        if (logger.isWarnEnabled()) {
                            logger.warn(warningMessage);
                        }
                        // jw: I would like to know about these, since all known values are covered, and new ones should be added/addressed
                        StatisticManager.recordException(UnexpectedError.getRuntimeException(warningMessage), false, null);
                    }

                } catch (NumberFormatException nfe) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed parsing image orientation for string \"" + output + "\". Treating as normal (unrotated) orientation.");
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("No image orientation identified for image. Treating as normal (unrotated) orientation.");
        }
        return ImageOrientationType.STANDARD;
    }

    private static final Double DEFAULT_QUALITY = 100D;

    /**
     * Uses im4java to use imagemagick to perform the given conversion
     * operation. Returns true on success, false on failure. Does not check if
     * either file exists.
     *
     * @param in     File to resize
     * @param out    Resized image File
     * @param width  width of new image
     * @param height height of new image
     * @return Description of the Return Value
     */
    public static ObjectPair<Boolean, Throwable> convert(File in, File out, int width, int height) {
        if (logger.isDebugEnabled()) {
            logger.debug("convert(" + in.getPath() + ", " + out.getPath() + ", " + width + "x" + height + ")");
        }

        // bl: changed to use resize instead of geometry.
        // refer: http://www.imagemagick.org/script/command-line-options.php#resize
        IMOperation operation = new IMOperation();
        operation.resize(width, height);

        operation.quality(DEFAULT_QUALITY);

        operation.autoOrient();
        operation.addImage(in.getAbsolutePath());
        operation.addImage(out.getAbsolutePath());

        return runConvertOperation(operation, in, out);
    }

    private static final Double SQUARE_CONVERSION_QUALITY = 100D;

    public static ObjectPair<Boolean, Throwable> convertSquare(File in, File out, int size) {
        // from:  http://www.randomsequence.com/articles/making-square-thumbnails-with-imagemagick/
        //convert input.jpg -thumbnail x200 -resize '200x<' -resize 50% -gravity center -crop 100x100+0+0 +repage -format jpg -quality 91 square.jpg
        //convert input.jpg -auto-orient -thumbnail x200 -resize 200x< -resize 50% -gravity center -crop 100x100 +repage -quality 91.0 /usr/local/apache-tomcat-6.0.20/temp/74_580282727604720920_SQUARE_THUMBNAIL.jpg

        if (logger.isDebugEnabled()) {
            logger.debug("convertSquare(" + in.getPath() + ", " + out.getPath() + ", " + size + ")");
        }

        IMOperation operation = new IMOperation();
        operation.addImage(in.getAbsolutePath());
        operation.autoOrient();
        int sizeDouble = size * 2;

        operation.addRawArgs("-thumbnail", "x" + sizeDouble);
        operation.addRawArgs("-resize", sizeDouble + "x<");
        operation.addRawArgs("-resize", "50%");
        operation.gravity("center");
        operation.crop(size, size, 0, 0);
        operation.addRawArgs("+repage");
        operation.quality(SQUARE_CONVERSION_QUALITY);
        operation.addImage(out.getAbsolutePath());

        return runConvertOperation(operation, in, out);
    }

    public static ImageDimensions getImageDimensionsForResizedImage(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        double scaleFactor = 1;
        // be sure the scale factor is always 1 or less.  this will prevent us
        // from upsizing an image that already fits within the proper dimensions.
        if (maxWidth > 0 && originalWidth > maxWidth) {
            scaleFactor = (double) maxWidth / originalWidth;
        }
        if (maxHeight > 0 && originalHeight * scaleFactor > maxHeight) {
            scaleFactor = (double) maxHeight / originalHeight;
        }

        if (scaleFactor >= 1) {
            return new ImageDimensions(originalWidth, originalHeight);
        }
        return new ImageDimensions(Math.max((int) (scaleFactor * originalWidth), 1), Math.max((int) (scaleFactor * originalHeight), 1));
    }

    public static ObjectPair<Boolean, Throwable> coalesce(File in, File out) {
        if (logger.isDebugEnabled()) {
            logger.debug("coalesce(" + in.getAbsolutePath() + ", " + out.getAbsolutePath() + ")");
        }

        IMOperation operation = new IMOperation();
        operation.addImage(in.getAbsolutePath());
        operation.coalesce();
        operation.addImage(out.getAbsolutePath());

        return runConvertOperation(operation, in, out);
    }

    public static BufferedImage textToImage(String text, int width, int height) {

        int sizeFromHeight = (int) ((double) height * .8);
        int sizeFromWidth = (int) (((double) (width / text.length()) * 1.5));

        int size = sizeFromHeight < sizeFromWidth ? sizeFromHeight : sizeFromWidth;
        Font font = new Font("Lucida Grande", Font.PLAIN, size);

        BufferedImage buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = buffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontRenderContext fc = g2.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(text, fc);

        // prepare some output
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2 = buffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);

        // actually do the drawing
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.BLACK);
        g2.drawString(text, (float) (width - bounds.getWidth()) / 2, (float) ((height + (bounds.getHeight() / 2)) / 2));

        return buffer;
    }

    public static void stripGPS(File image) {
        ETOperation etOperation = new ETOperation();

        etOperation.addImage(image.getAbsolutePath());
        etOperation.delTags("GPSLatitude", "GPSLongitude", "GPSAltitude", "GPSDateStamp", "GPSLatitudeRef", "GPSLongitudeRef", "GPSAltitudeRef", "GPSTimeStamp");

        ExiftoolCmd exiftoolCmd = new ExiftoolCmd();
        try {
            exiftoolCmd.run(etOperation);
        } catch (Exception e) {
            logger.warn("Error removing gps from image.", e);
        }
    }
}
