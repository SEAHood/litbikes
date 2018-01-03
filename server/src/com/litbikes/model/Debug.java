package com.litbikes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.litbikes.dto.DebugDto;
import com.litbikes.dto.ImpactDto;
import com.litbikes.util.Vector;

public class Debug {
	private List<ImpactPoint> impacts;
	
	public Debug() {
		impacts = new ArrayList<>();
	}
	
	public void addImpact(ImpactPoint ip) {
		impacts.add(ip);

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				impacts.remove(ip);
			}
		}, 100, TimeUnit.MILLISECONDS);
	}
	
	public DebugDto getDto() {
		DebugDto dto = new DebugDto();
		dto.impacts = new ArrayList<>();		
		for (ImpactPoint impact : impacts) {
			ImpactDto impactDto = new ImpactDto();
			impactDto.pos = new Vector(impact.getPoint().getX(), impact.getPoint().getY());
			dto.impacts.add(impactDto);
		}		
		return dto;
	}
}
