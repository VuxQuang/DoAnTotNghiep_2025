package fsa.training.travelee.service;

import fsa.training.travelee.dto.RecentActivityDto;

import java.util.List;

public interface ActivityService {
    List<RecentActivityDto> getRecentActivities(int limit);
}
