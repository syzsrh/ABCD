package code_ABCD;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 *
 * @author sarahsyazwina
 */
public class Game extends JPanel{
    
    //The phases of the game
    enum Status{
        WIN, LOSE, RUNNING, NOTRUNNING
    }
    
    //CONSTANTS
    private static final int Z = 25;
    private static final String FONT = "Adobe Gothic Std";
    private static final Color EMPTY_TILE = new Color(0xCDC1B4);
    private static final Color BG_COLOR = new Color(0xbbada0);
    private static final int PADDING = 20;
    private static final int WIDTH_HEIGHT = 60;
    private final Color[] TILE_COLOR = {
        new Color(0xfff4d3), new Color(0xffe4c3), new Color(0xeee4da),
        new Color(0xffdac3), new Color(0xe7b08e), new Color(0xe7bf8e),
        new Color(0xffc4c3), new Color(0xe7948e), new Color(0xbe7e56),
        new Color(0xbe5e56), new Color(0x9c3931), new Color(0x701710),
        new Color(0x701710), new Color(0xede0c8), new Color(0xf2b179),
        new Color(0xf59563), new Color(0xf67c5f), new Color(0xf65e3b),
        new Color(0xedcf72), new Color(0xedcc61), new Color(0xedc850),
        new Color(0xedc53f), new Color(0xedc22e), new Color(0xf0f199),
        new Color(0xd8d889), new Color(0xdbdd00)};
    
    //VARIABLES
    private ArrayList<Tile[][]> undoList;
    private ArrayList<Integer> scoreList;
    private static int row;
    private static int col;
    private static int playerHighest;
    private static int score;
    private boolean canMove;
    private boolean canUndo;
    private static final int[] highestScore = new int[10];  //intialises an array to store the highest scores
    private Tile[][] gameBoard;
    private Status gameStatus = Status.NOTRUNNING; //sets the state as not running initially
    
    /**
     * 
     * Constructor for game.
     */
    public Game(){
        
        //focuses so the listeners will function
        setFocusable(true);
        
        //calls method startGame() at mouse click
        //calls method repaint() to update GUI
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                
                startGame();
                repaint();
            }
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:    //calls moveUp() for up arrow key input
                        moveUp();
                        break;
                    case KeyEvent.VK_DOWN:  //calls moveDown() for down arrow key input
                        moveDown();
                        break;
                    case KeyEvent.VK_LEFT:  //calls moveLeft() for left arrow key input
                        moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT: //calls moveRight() for right arrow key input
                        moveRight();
                        break;
                    case KeyEvent.VK_SPACE:    //calls undoMove() method for spacebar input
                        if(score != 0 && canUndo){
                            undoMove();
                        } 
                        break;
                }
                //repaints the whole board after each key input/mouse click
                repaint();
            }
        });
    }
    
    /**
     * 
     * Method will be called when components on the GUI need to be repainted,
     * method calls drawGrid() to update the GUI after every input.
     * 
     * @param gg Graphics object to be casted to Graphics2D
     */
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
        drawGrid(g);
    }
    
    /**
     * 
     * Method initializes all game values.
     */
    public void startGame(){
        //If the enum Status is not set to RUNNING
        if(gameStatus != Status.RUNNING){
            canUndo = true;
            score = 0;              //Initialize user's score as 0
            playerHighest = -1;     //Initialize user's highest achieved tile value as -1
            gameStatus = Status.RUNNING;
            gameBoard = new Tile[row][col];     //Initialize new Tile[][] object
            undoList = new ArrayList<>();   //Initialize new list for previous gameBoards
            scoreList = new ArrayList<>();   //Initialize new list for previous scores
            addRandomTile();
            addRandomTile();
        }
    }
    
    /**
     * 
     * Method that draws the grid and tiles if the Status is RUNNING,
 else it will show either the start new game screen (default), the game over screen
 (if Status is LOSE) or the you won! screen (if Status is WIN).
     * 
     * @param g Graphics2D object from paintComponent()
     */
    public void drawGrid(Graphics2D g){
        
        if(gameStatus == Status.RUNNING){
            for (int r = 0; r < row; r++) {
                for (int c = 0; c < col; c++) {
                    if(gameBoard[r][c] == null){
                        g.setColor(EMPTY_TILE);
                        g.fillRoundRect((c+1 * PADDING) + c * WIDTH_HEIGHT, (r+1 * PADDING) + r * WIDTH_HEIGHT, WIDTH_HEIGHT - 5, WIDTH_HEIGHT - 5, 4, 4);
                    }
                    else{
                        drawTile(g, r, c);
                    }
                }
            }
        }
        else{
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, (WIDTH_HEIGHT+ PADDING) * (col + 3), WIDTH_HEIGHT * (row + 1));
            
            g.setColor(BG_COLOR.darker());
            g.setFont(new Font(FONT, Font.BOLD, PADDING));
            
            switch(gameStatus){
                case WIN:
                    g.drawString("you won!", 0, 25);
                    g.drawString("score: " + score, 0, 25 * 2);
                    break;
                case LOSE:
                    g.drawString("game over!", 0, 25);
                    g.drawString("Score: " + score, 0, 25 * 2);
                    g.drawString("click to play again", 0, 25 * 3);
                    break;
                default:
                    g.drawString("click to start new game", 0, 25);
                    g.drawString("arrow keys to move, space to undo", 0, 25 * 2);
                    g.setFont(new Font(FONT, Font.PLAIN, PADDING));
                    g.drawString("top scores", 0, 70);
                    //calls method to read saved score files
                    readScore();
                    //prints high score values
                    for (int i = 0; i < 10; i++) {
                        String s = String.valueOf(highestScore[i]);
                        g.drawString((i + 1) + ". " + s, 0, 90 + (i * (10 * (row / 2))));
                    }
                    break;
            }
        }
    }
    
    /**
     * 
     * Method for drawing new tiles that contain values.
     * 
     * @param g inherited from paintComponent()
     * 
     * @param r the row value of the tile to be drawn
     * 
     * @param c the column value of the tile to be drawn
     */
    public void drawTile(Graphics2D g, int r, int c){
        
        int value = gameBoard[r][c].getValue();
        
        g.setColor(TILE_COLOR[value]);
        g.fillRoundRect((c+1 * PADDING) + c * WIDTH_HEIGHT, (r+1 * PADDING) + r * WIDTH_HEIGHT, WIDTH_HEIGHT - 5, WIDTH_HEIGHT - 5, 4, 4);
        g.setFont(new Font(FONT, Font.BOLD, 30));
        g.setColor(BG_COLOR.darker());
        String tile = String.valueOf((char)(value + 'A'));  //adds the int value to char 'A'
        
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        int desc = fm.getDescent();
        
        int x = (c+1 * PADDING) + c * WIDTH_HEIGHT + (WIDTH_HEIGHT - 5 - fm.stringWidth(tile)) / 2;
        int y = (r+1 * PADDING) + r * WIDTH_HEIGHT + (asc + (WIDTH_HEIGHT - 5 - (asc + desc)) / 2);
        g.drawString(tile, x, y);
        g.setFont(new Font(FONT, Font.PLAIN, 20 * (col / 2)));
        String sco = Integer.toString(score);
        g.drawString("score", (col * WIDTH_HEIGHT) + (PADDING * col), PADDING * row);
        g.drawString(sco, (col * WIDTH_HEIGHT) + (PADDING * (col + 1)), PADDING * row * 2);
    }
    
    /**
     * 
     * Adds a random value to a random empty tile in the array gameBoard with the
     * value 0 = A (with a probability of 14/15) or 1 = B (with a probability of 1/15)
     * when called.
     */
    public void addRandomTile(){
        
        Random random = new Random();
        int r = 0, c = 0;
        do{
            r = random.nextInt(row);
            c = random.nextInt(col);
        }while(gameBoard[r][c] != null);
        
        int tile = random.nextInt(15) == 0 ? 1 : 0;
        gameBoard[r][c] = new Tile(tile);
    }
    
    /**
     * 
     * Method initializes moved as false and checks each tile for a value,
     * if the tile has a value then the tile (if keyboard input was left and so on)
     * to its left will be checked (next tile), if it is null then the current tile will replace the next tile,
     * if it is occupied then it will call canMergeWith(), if this returns true then
     * it calls mergeWith() to merge the two tiles and place the new merged value to
     * the next tile position.
     * 
     * @param direction     if direction == 0 it executes the tile checking from top to bottom
     *                      and if direction != 0 it executes the tile checking from bottom to top
     * 
     * @param yIncrement    determines which direction the tile moves in the y-axis
     *                      as the yIncrement will be added to the row value to update
     *                      the tile position
     * 
     * @param xIncrement    determines which direction the tile moves in the x-axis
     *                      as the xIncrement will be added to the column value to update
     *                      the tile position
     * 
     * @return              true if the tile has moved
     */
    public boolean moveTile(int direction, int yIncrement, int xIncrement){
        
        boolean moved = false;
        int tempR, tempC;
        
        if(direction == 0){
            tempR = 0;
            tempC = 0;
        }
        else{
            tempR = row - 1;
            tempC = col - 1;
        }
        
        for (int i = 0; i < row * col; i++) {
            
            if(i % col == 0){
                
                if(direction == 0)
                    tempC = 0;
                else
                    tempC = col - 1;
                
                if(i != 0){
                    if(direction == 0)
                        tempR++;
                    else
                        tempR--;
                }
            }
            else{
            if(direction == 0)
                tempC++;
            else
                tempC--;
            }
            
            int r = tempR;
            int c = tempC;
            if(gameBoard[r][c] == null)
                continue;
            
            int nextR = r + yIncrement;
            int nextC = c + xIncrement;
            
            while(nextR >= 0 && nextR < row && nextC >= 0 && nextC < col){
                
                Tile next = gameBoard[nextR][nextC];
                Tile current = gameBoard[r][c];
                
                if(next == null){
                    
                    if(canMove)
                        return true;
                    
                    
                    gameBoard[nextR][nextC] = current;
                    gameBoard[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncrement;
                    nextC += xIncrement;
                    moved = true;
                }
                else if(next.canMergeWith(current)){
                    
                    if(canMove)
                        return true;
                    
                    int value = next.mergeWith(current);
                    if(value > playerHighest)
                        playerHighest = value;
                    
                    score += value + 1;
                        
                    gameBoard[r][c] = null;
                    moved = true;
                    break;
                }
                else{
                    break;
                }
            }
        }
        
        if(moved){
            
            canUndo = true;
            if(playerHighest < Z){
                clearMerged();
                addRandomTile();
                if(!canUserMove()){
                    gameStatus = Status.LOSE;
                    checkScore();
                }
            }
            else if (playerHighest == Z){
                gameStatus = Status.WIN;
                checkScore();
            }
        }
        saveForUndo();
        return moved;
    }
    
    public boolean moveUp(){
        return moveTile(0, -1, 0);
    }
    
    public boolean moveDown() {
        return moveTile(1, 1, 0);
    }
    
    public boolean moveLeft() {
        return moveTile(0, 0, -1);
    }
    
    public boolean moveRight() {
        return moveTile(1, 0, 1);
    }
    
    /**
     * Method to undo the game one step, the board is set to a previous board and
     * the score is set to a previous score.
     */
    public void undoMove(){
        
        //user cannot undo immediately after the first undo
        canUndo = false;
        
        //sets the current gameBoard to the previous gameBoard
        Tile[][] temp = undoList.get(1);
        gameBoard = temp;
        
        //sets the current score to the previous score
        int tempScore = scoreList.get(1);
        score = tempScore;
        saveForUndo();
    }
    
    /**
     * 
     * Method to save the value of the score and the current gameBoard to a list
     * so that when the user chooses to undo they may be restored.
     */
    public void saveForUndo(){
        //initialise values for the list
        int tempScore;
        Tile[][] temp = new Tile[row][col];
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                if(gameBoard[r][c] != null)
                    temp[r][c] = new Tile(gameBoard[r][c].getValue());
            }
        }
        tempScore = score;
        
        //add the updated score and gameboard to a list of previous scores and gameboards
        undoList.add(0, temp);
        scoreList.add(0, tempScore);
        
        //controls the size of the list
        if(scoreList.size() > 2)
            scoreList.remove(scoreList.size() - 1);
        if(undoList.size() > 2)
            undoList.remove(undoList.size() - 1);
    }
    
    /**
     * 
     * Clears all previously set boolean values that determine if a Tile object
     * has merged with another tile and sets it as false.
     */
    public void clearMerged(){
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                Tile tile = gameBoard[i][j];
                if(tile != null)
                    tile.setMerged(false);
            }
        }     
    }
    
    /**
     * 
     * Checks if the board has any available space for the user to go on
     * playing the game by calling moveTile() which will return true if the tiles
     * can move.
     * 
     * @return  true if user can still move
     */
    public boolean canUserMove(){
        canMove = true;
        boolean hasMoves = moveUp() || moveDown() || moveLeft() || moveRight();
        canMove = false;
        return hasMoves;
    }
    
    /**
     * 
     * Method to check if the score is higher than any of the saved high scores
     * if yes then it calls saveScore().
     */
    public void checkScore(){
        
        for (int i = 0; i < 10; i++) {
            if(score > highestScore[i]){
                highestScore[9] = score;
                saveScore();
                break;
                }
            }
    }
    
    /**
     * 
     * Method to read scores from a saved text file.
     */
    public void readScore(){
        FileInputStream file;
        Scanner reader;
        try {
            file = new FileInputStream("scores.txt");
            reader = new Scanner(file);
            while(reader.hasNext()){
                for (int i = 0; i < 10; i++) {
                    int s = reader.nextInt();
                    highestScore[i] = s;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("FNFE");
        }
    }
        
    /**
     * 
     * Method to save the highest scores.
     */
    public static void saveScore(){
        //Save the values to a file
        try{
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if(highestScore[i] > highestScore[j]){
                        int temp1 = highestScore[i];
                        int temp2 = highestScore[j];
                        highestScore[i] = temp2;
                        highestScore[j] = temp1;
                    }
                }
            }
            
            FileOutputStream f = new FileOutputStream("scores.txt");
            try (PrintWriter pw = new PrintWriter(f)) {
                for (int k = 0; k < 10; k++) {
                    int save = highestScore[k];
                    pw.print(save + "\n");
                }
                pw.flush();
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("FNFE");
        }
    }
    /**
     * 
     * Method to open JFrame with height and width that varies with user's row and
     * column input, window is not resizable and is always on top.
     */
    public static void openFrame(){
        
        JFrame game = new JFrame();
        game.setTitle("ABCD");
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.setSize((WIDTH_HEIGHT) * (col * 2), WIDTH_HEIGHT * (row + 1));
        game.setResizable(false);
        
        game.add(new Game());
        game.setLocationRelativeTo(null);
        game.setVisible(true);
        game.setAlwaysOnTop(true);

    }
       
    /**
     * 
     * Main method, calls openWindow() to start game after receiving row and column
     * values from the user.
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        Scanner read = new Scanner(System.in);
        System.out.println("Please enter ROW and COLUMN: ");
        row = read.nextInt();
        col = read.nextInt();
        openFrame();
    }
    
    /**
     * 
     * Class for handling the tile values, with a constructor that initializes
     * the instance value with an accepted value, with an accessor method for 
     * the instance variable value, and a mutator method for the instance merged.
     */
    public class Tile{
        
        private boolean merged;
        private int value;
        
        //initializes an object with accepted value
        public Tile(int v){
            
            value = v;
        }
        
        //accessor for value
        public int getValue(){
            
            return value;
        }
        
        //mutator for merged
        public void setMerged(boolean m){
            
            merged = m;
        }
        
        /**
         * 
         * Method for checking if the two considered tiles can be "merged"
         * 
         * @param next  Tile object that is either null or has an integer variable
         * 
         * @return      true if Tile object next is not null, current Tile and next Tile have not
         *              merged, and current Tile and next Tile have equal value
         */
        public boolean canMergeWith(Tile next){
            
            return !this.merged && next != null && !next.merged && value == next.getValue();
        }
        
        /**
         * 
         * Method to "merge" two tiles
         * 
         * @param next  Tile object that is either null or has an integer variable
         * 
         * @return      the updated value (the original value plus 1 to indicate next letter)
         *              for the "merged" tile if the canMergeWith() returns true, else it returns -1
         */
        public int mergeWith(Tile next){
            
            if(canMergeWith(next)){
                value += 1;
                merged = true;
                return value;
            }
            return -1;
        }
    }
    }
