package com.mygdx.poseidensrevenge;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    private Ball ball;
    private OrthographicCamera camera;
    private Boolean accelerometerAvail;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject () {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();
    private Array<Rectangle> endTiles = new Array<Rectangle>();
    private Texture end;
    private Game game;

    public GameScreen(Game game){
        this.game = game;
    }

    @Override
    public void show() {
        // check if the platform has accelerometer
        accelerometerAvail = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);

        // load the endpoint
        end = new Texture("end.png");

        // create ball
        ball = new Ball();
        ball.image = new Texture("Ball.png");
        ball.position.set(0,0);

        // set the WIDTH and HEIGHT of the ball
        Ball.WIDTH = 50;
        Ball.HEIGHT = 50;

        // load the map
        map = new TmxMapLoader().load("test.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);

        // load end tiles
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("End");
        // add end tile rectangles into tiles
        for (int y = 22; y <= 23; y++) {
            for (int x = 11; x <= 12; x++) {
                Rectangle rect = rectPool.obtain();
                rect.set(x*32, y*32, 32, 32);
                endTiles.add(rect);
            }
        }
        System.out.println(endTiles.size);

        // create a camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();
    }

    @Override
    public void render(float delta) {
        // clear the screen
        ScreenUtils.clear(0.7f, 0.7f, 1.0f, 1);

        // update the ball
        updateBall();

        // let the camera follow the koala, x-axis only
        camera.update();

        // set the TiledMapRenderer view based on what the
        // camera sees, and render the map
        renderer.setView(camera);
        renderer.render();

        // render the ball
        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(ball.image, ball.position.x, ball.position.y, Ball.WIDTH, Ball.HEIGHT);
        batch.end();
    }

    // update ball's movement
    private void updateBall(){
        if (accelerometerAvail) {
            // get acceleration of the ball by accelerometer
            float[] mat = new float[4 * 4];
            Gdx.input.getRotationMatrix(mat);
            Matrix4 m = new Matrix4(mat);
            Quaternion q = m.getRotation(new Quaternion());

            // assign velocity to ball
            ball.velocity.y = ((int)((-1)*(q.y*10)))%10;
            ball.velocity.x = ((int)((-1)*(q.x*10)))%10;

            // do collision detection with wall tiles
            // use a rectangle to store ball's position and do the comparison
            Rectangle ballRec = rectPool.obtain();
            ballRec.set(ball.position.x, ball.position.y, Ball.WIDTH, Ball.HEIGHT);
            // check ball's velocity on x and y axes
            int startX, startY, endX, endY;
            startX = (int)(ball.position.x + ball.velocity.x);
            endX = (int)(ball.position.x + Ball.WIDTH + ball.velocity.x);
            startY = (int)(ball.position.y + ball.velocity.y);
            endY = (int)(ball.position.y + Ball.HEIGHT + ball.velocity.y);
            // get walls rect
            getTiles(startX, startY, endX, endY, tiles);
            // move ball's rect
            ballRec.x += ball.velocity.x;
            ballRec.y += ball.velocity.y;
            // check end
            checkEnd(ballRec);
            // check collision
            for (Rectangle tile : tiles) {
                if (ballRec.overlaps(tile)) {
                    if (ballRec.x + ballRec.width > tile.x && ball.velocity.x > 0){
                        ballRec.x -= ball.velocity.x;
                        ball.velocity.x = 0;
                    }else if(ballRec.x < tile.x + tile.width && ball.velocity.x < 0){
                        ballRec.x -= ball.velocity.x;
                        ball.velocity.x = 0;
                    }
                    else if (ballRec.y + ballRec.height > tile.y && ball.velocity.y > 0){
                        ballRec.y -= ball.velocity.y;
                        ball.velocity.y = 0;
                    }else if(ballRec.y < tile.y + tile.height && ball.velocity.y < 0){
                        ballRec.y -= ball.velocity.y;
                        ball.velocity.y = 0;
                    }
                }
            }
            rectPool.free(ballRec);
            // move ball
            ball.position.add(ball.velocity);
            if (ball.position.x > Gdx.graphics.getWidth()- Ball.WIDTH){
                ball.position.x = Gdx.graphics.getWidth()- Ball.WIDTH;
            }else if (ball.position.x < 0){
                ball.position.x = 0;
            }
            if (ball.position.y > Gdx.graphics.getHeight()- Ball.HEIGHT){
                ball.position.y = Gdx.graphics.getHeight()- Ball.HEIGHT;
            }else if (ball.position.y < 0){
                ball.position.y = 0;
            }
        }
    }

    private void checkEnd(Rectangle ballRec) {
        for (Rectangle tile : endTiles) {
            if (ballRec.overlaps(tile)) {
                System.out.println("touch");
                game.setScreen(new SucceedScreen(game));
            }
        }
    }

    // get walls rect
    private void getTiles (int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        // get walls layer
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("Wall");
        rectPool.freeAll(tiles);
        tiles.clear();
        // add wall tile rectangles into tiles
        for (int y = (startY/32); y <= (endY/32); y++) {
            for (int x = (startX/32); x <= (endX/32); x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x*32, y*32, 32, 32);
                    tiles.add(rect);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
    }
}
