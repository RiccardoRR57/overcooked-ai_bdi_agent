public class Grid {
    private final int width;
    private final int height;
    private final Cell[][] grid;
    private final Cell[][] objects;

    /**
     * Constructs a grid for the Overcooked AI game.
     *
     * @param width the width of the grid
     * @param height the height of the grid
     * @param layout a string representation of the grid layout
     */
    public Grid(int width, int height, String layout) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[height][width];
        this.objects = new Cell[height][width];
        
        if (layout.length() != width * height) {
            throw new IllegalArgumentException("Layout string length must match grid dimensions");
        }
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                char cellChar = layout.charAt(index);
                grid[y][x] = new Cell(x, y, cellChar);
            }
        }
    }

    /**
     * Gets the width of the grid.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the grid.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the cell type at the specified coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the cell type at (x, y)
     */
    public char getCellType(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return grid[y][x].getType();
    }

    /**
     * Sets the cell type at the specified coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param type the new cell type
     */
    public void setObject(int x, int y, char type) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        objects[y][x].setType(type);
    }

    public void resetObjects() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                objects[y][x] = new Cell(x, y, ' ');
            }
        }
    }

    /**
     * Cell class representing each position in the grid.
     */
    private static class Cell {
        private final int x;
        private final int y;
        private char type;
        
        public Cell(int x, int y, char type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public char getType() {
            return type;
        }

        public void setType(char type) {
            this.type = type;
        }
    }
}
