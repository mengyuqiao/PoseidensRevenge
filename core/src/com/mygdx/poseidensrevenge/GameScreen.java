package com.mygdx.poseidensrevenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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

    @Override
    public void show() {
        // check if the platform has accelerometer
        accelerometerAvail = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);

        // create ball
        ball = new Ball();
        ball.image = new Texture("Ball.png");
        ball.position.set(0,0);

        // set the WIDTH and HEIGHT of the ball and resize it for collision detection by 32(1 unit = 32 pixels)
        Ball.WIDTH = 1 / 32f * ball.image.getWidth();
        Ball.HEIGHT = 1 / 32f * ball.image.getHeight();

        // load the map, set the unit scale to 1/32(1 unit = 32 pixels)
        map = new TmxMapLoader().load("test.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 32f);

        // create a camera, shows 100x100 units
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 100, 100);
        camera.update();
    }

    @Override
    public void render(float delta) {

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
            if (ball.velocity.x > 0){
                startX = endX = (int)(ball.position.x + Ball.WIDTH + ball.velocity.x);
            }else {
                startX = endX = (int)(ball.position.x + ball.velocity.x);
            }
            if (ball.velocity.y > 0){
                startY = endY = (int)(ball.position.y + Ball.HEIGHT + ball.velocity.y);
            }else {
                startY = endY = (int)(ball.position.y + ball.velocity.y);
            }
            // get walls rect
            getTiles(startX, startY, endX, endY, tiles);
            // move ball's rect
            ballRec.x += ball.velocity.x;
            ballRec.y += ball.velocity.y;
            // check collision
            for (Rectangle tile : tiles) {
                if (ballRec.overlaps(tile)) {
                    if (ballRec.x + ballRec.width > tile.x && ball.velocity.x > 0){
                        ball.velocity.x = 0;
                    }else if(ballRec.x < tile.x + tile.width && ball.velocity.x < 0){
                        ball.velocity.x = 0;
                    }
                    if (ballRec.y + ballRec.height > tile.y && ball.velocity.y > 0){
                        ball.velocity.y = 0;
                    }else if(ballRec.y < tile.y + tile.height && ball.velocity.y < 0){
                        ball.velocity.y = 0;
                    }
                }
            }

        }
    }

    // get walls rect
    private void getTiles (int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");
        rectPool.freeAll(tiles);
        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
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

    }
}
