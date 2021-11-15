package com.mygdx.poseidensrevenge;

import com.badlogic.gdx.Game;

public class PoseidensRevenge extends Game {
	private Game game;
	@Override
	public void create () {
		this.game = this;
		setScreen(new MenuScreen(this.game));
	}
}
