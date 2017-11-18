package com.litbikes.ai;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.litbikes.dto.ClientUpdateDto;
import com.litbikes.engine.GameController;
import com.litbikes.model.Arena;
import com.litbikes.model.Bike;

public class BotController {
	
	private List<Bot> bots;
	private GameController gameController;
	private static Logger LOG = Log.getLogger(BotController.class);
	
	public BotController( GameController _gameController ) {
		gameController = _gameController;
		bots = new ArrayList<>();
	}
		
	private void deployBots(int count) {
		LOG.info("Deploying " + count + " bots");
		for ( int i = 0; i < count; i++ ) {
			Bot bot = gameController.botCreated();
			bot.attachController(this);
			bot.start();
			bots.add(bot);
		}
		LOG.info("Current bot count: " + bots.size());
	}
	
	private void destroyBots(int count) {
		LOG.info("Destroying " + count + " bots - ");
		List<Bot> doomedBots = new ArrayList<>();
		
		for ( int i = 0; i < count; i++ ) {
			doomedBots.add(bots.get(i));
		}
		
		for (Bot b : doomedBots) {
			gameController.botDestroyed(b);
			bots.remove(b);
		}
		
		LOG.info("Current bot count: " + bots.size());
	}
	
	public int getBotCount() {
		return bots.size();
	}
	
	public void setBotCount(int botCount) {
		LOG.info("Setting bot count to " + botCount);
		int currentBotCount = bots.size();
		int botDeficit = botCount - currentBotCount;
		if (botDeficit > 0) {
			deployBots(botDeficit);
		} else if (botDeficit < 0) {
			destroyBots(Math.abs(botDeficit));
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
