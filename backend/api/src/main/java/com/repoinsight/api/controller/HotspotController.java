package com.repoinsight.api.controller;

import java.util.UUID;

import com.repoinsight.api.dto.analysis.HotspotOverviewResponse;
import com.repoinsight.api.service.HotspotService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repositories/{repositoryId}/hotspots")
public class HotspotController {

	private final HotspotService hotspotService;

	public HotspotController(HotspotService hotspotService) {
		this.hotspotService = hotspotService;
	}

	@GetMapping
	public HotspotOverviewResponse overview(@PathVariable UUID repositoryId) {
		return hotspotService.overview(repositoryId);
	}
}