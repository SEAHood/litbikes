package com.litbikes.ai;

import java.util.ArrayList;
import java.util.List;

import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.engine.GameController;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;

public class BotController {
	
	private List<Bot> bots;
	private GameController gameController;
	
	public BotController( GameController _gameController ) {
		gameController = _gameController;
		bots = new ArrayList<>();
	}
	
	public void deployBots(int count) {
		for ( int i = 0; i < count; i++ ) {
			Bot bot = gameController.createBot();
			bot.attachController(this);
			bot.start();
		}
	}

	public void doUpdate( List<Bike> bikes, Arena arena ) {
		for ( Bot bot : bots ) {
			bot.updateWorld(bikes, arena);
		}
	}

	public void sentRequestWorld( BotIOClient client ) {
		gameController.clientRequestWorldEvent(client);
	}

	public void sentUpdate( BotIOClient client, ClientUpdateDto updateDto ) {
		gameController.clientUpdateEvent(client, updateDto);
	}

	public void sentRequestRespawn( BotIOClient client ) {
		gameController.clientRequestRespawnEvent(client);
	}
}
