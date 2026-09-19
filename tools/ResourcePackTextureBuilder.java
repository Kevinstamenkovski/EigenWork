import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import javax.imageio.ImageIO;

/** Deterministically slices the generated 5x4 EigenWorks atlas into pack textures. */
public final class ResourcePackTextureBuilder {
	private static final int COLUMNS = 5;
	private static final int ROWS = 4;
	private static final int TEXTURE_SIZE = 32;
	private static final Color BLOCK_BACKGROUND = new Color(17, 21, 25);
	private static final String[] NAMES = {
		"engineering_test_bench", "digital_clock", "digital_counter", "digital_gate", "digital_register",
		"computer", "microcontroller", "oscilloscope", "motor_rig", "mathematics_workstation",
		"communication_hub", "robot_arm", "factory_cell", "advanced_engineering_console", "can_cable",
		"can_node", "robot_joint_module", "digital_linking_tool", "engineering_inspector", "pack_icon"
	};

	private ResourcePackTextureBuilder() { }

	public static void main(String[] arguments) throws IOException {
		if (arguments.length != 2) throw new IllegalArgumentException("Usage: ResourcePackTextureBuilder <atlas.png> <pack-directory>");
		BufferedImage atlas = ImageIO.read(Path.of(arguments[0]).toFile());
		if (atlas == null || atlas.getWidth() < 500 || atlas.getHeight() < 400) throw new IllegalArgumentException("Atlas must be a readable high-resolution PNG");
		Path pack = Path.of(arguments[1]);
		Path blocks = pack.resolve("assets/eigenworks/textures/block");
		Path items = pack.resolve("assets/eigenworks/textures/item");
		Files.createDirectories(blocks); Files.createDirectories(items);
		for (int index = 0; index < NAMES.length; index++) {
			int column = index % COLUMNS, row = index / COLUMNS;
			int x0 = Math.round(column * atlas.getWidth() / (float) COLUMNS) + 2;
			int y0 = Math.round(row * atlas.getHeight() / (float) ROWS) + 2;
			int x1 = Math.round((column + 1) * atlas.getWidth() / (float) COLUMNS) - 2;
			int y1 = Math.round((row + 1) * atlas.getHeight() / (float) ROWS) - 2;
			BufferedImage cell = atlas.getSubimage(x0, y0, x1 - x0, y1 - y0);
			if (index == 19) {
				writeScaled(cell, pack.resolve("pack.png"), 128, false);
			} else if (index >= 17) {
				writeScaled(cell, items.resolve(NAMES[index] + ".png"), TEXTURE_SIZE, true);
			} else {
				writeScaled(cell, blocks.resolve(NAMES[index] + ".png"), TEXTURE_SIZE, false);
			}
		}
	}

	private static void writeScaled(BufferedImage source, Path destination, int size, boolean preserveTransparency) throws IOException {
		BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = output.createGraphics();
		if (!preserveTransparency) { graphics.setColor(BLOCK_BACKGROUND); graphics.fillRect(0, 0, size, size); }
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.drawImage(source, 0, 0, size, size, null);
		graphics.dispose();
		if (preserveTransparency) {
			clearEdgeConnectedBackground(output);
			if (!hasTransparentPixel(output)) throw new IOException("Item background removal produced no transparency: " + destination);
		}
		Files.createDirectories(destination.getParent());
		if (!ImageIO.write(output, "PNG", destination.toFile())) throw new IOException("No PNG writer for " + destination);
	}

	/** Removes only near-black pixels reachable from an image edge, preserving dark details inside the icon. */
	private static void clearEdgeConnectedBackground(BufferedImage image) {
		boolean[][] visited = new boolean[image.getHeight()][image.getWidth()];
		ArrayDeque<int[]> pending = new ArrayDeque<>();
		for (int coordinate = 0; coordinate < image.getWidth(); coordinate++) {
			pending.add(new int[] { coordinate, 0 });
			pending.add(new int[] { coordinate, image.getHeight() - 1 });
		}
		for (int coordinate = 1; coordinate < image.getHeight() - 1; coordinate++) {
			pending.add(new int[] { 0, coordinate });
			pending.add(new int[] { image.getWidth() - 1, coordinate });
		}
		while (!pending.isEmpty()) {
			int[] pixel = pending.removeFirst();
			int x = pixel[0], y = pixel[1];
			if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight() || visited[y][x]) continue;
			visited[y][x] = true;
			Color color = new Color(image.getRGB(x, y), true);
			if (Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue())) > 48) continue;
			image.setRGB(x, y, 0);
			pending.add(new int[] { x - 1, y }); pending.add(new int[] { x + 1, y });
			pending.add(new int[] { x, y - 1 }); pending.add(new int[] { x, y + 1 });
		}
	}

	private static boolean hasTransparentPixel(BufferedImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) >>> 24) == 0) return true;
			}
		}
		return false;
	}
}
