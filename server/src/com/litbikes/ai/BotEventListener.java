package com.litbikes.ai;

import com.litbikes.dto.ClientUpdateDto;

public interface BotEventListener {
	void sentRequestWorld(Bot bot);
	void sentUpdate(BotIOClient client, ClientUpdateDto updateDto);
	void sentRequestRespawn(Bot bot);
}