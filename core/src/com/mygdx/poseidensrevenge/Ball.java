package com.mygdx.poseidensrevenge;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Ball{
    static float WIDTH;
    static float HEIGHT;
    static float MAX_VELOCITY = 10f;

    final Vector2 position = new Vector2();
    final Vector2 velocity = new Vector2();
    Texture image;
}
