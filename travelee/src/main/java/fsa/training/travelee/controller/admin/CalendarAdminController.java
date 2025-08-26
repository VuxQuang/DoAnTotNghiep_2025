package fsa.training.travelee.controller.admin;

import fsa.training.travelee.entity.TourSchedule;
import fsa.training.travelee.repository.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/calendar")
public class CalendarAdminController {

    private final TourScheduleRepository tourScheduleRepository;

    @GetMapping
    public String calendarPage(Model model) {
        return "admin/calendar";
    }

    // API sự kiện cho FullCalendar: chỉ dùng ngày bắt đầu (departureDate)
    @GetMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> getEvents(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        List<TourSchedule> schedules = tourScheduleRepository.findByDepartureDateBetweenWithTour(startDate, endDate);
        List<Map<String, Object>> events = new ArrayList<>();
        for (TourSchedule s : schedules) {
            Map<String, Object> e = new HashMap<>();
            e.put("id", s.getId());
            e.put("title", s.getTour() != null ? s.getTour().getTitle() : "Tour");
            e.put("start", s.getDepartureDate().toString());
            if (s.getTour() != null) {
                e.put("url", "/admin/tour/view/" + s.getTour().getId());
            }
            events.add(e);
        }
        return events;
    }
}


