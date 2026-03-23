package com.example.travel_platform.chatbot.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardCategory;
import com.example.travel_platform.booking.Booking;
import com.example.travel_platform.calendar.CalendarEvent;
import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatQueryRepository {

    private final EntityManager em;

    public List<ChatbotService.QueryBlock> execute(
            Integer userId,
            ChatbotRequest.ContextDTO context,
            List<ChatbotService.QueryPlan> queryPlans) {
        List<ChatbotService.QueryBlock> blocks = new ArrayList<>();
        for (ChatbotService.QueryPlan queryPlan : queryPlans) {
            blocks.add(executeSingle(userId, context, queryPlan));
        }
        return blocks;
    }

    private ChatbotService.QueryBlock executeSingle(
            Integer userId,
            ChatbotRequest.ContextDTO context,
            ChatbotService.QueryPlan queryPlan) {
        return switch (queryPlan.getDomain()) {
            case BOOKING -> bookingBlock(userId, context, queryPlan);
            case TRIP -> tripBlock(userId, context, queryPlan);
            case CALENDAR -> calendarBlock(userId, context, queryPlan);
            case BOARD -> boardBlock(queryPlan);
        };
    }

    private ChatbotService.QueryBlock bookingBlock(
            Integer userId,
            ChatbotRequest.ContextDTO context,
            ChatbotService.QueryPlan queryPlan) {
        List<Booking> bookings = findBookings(userId, context, queryPlan);
        List<Map<String, Object>> items = bookings.stream()
                .map(this::toBookingItem)
                .toList();
        return ChatbotService.QueryBlock.createQueryBlock(
                ChatbotService.Domain.BOOKING,
                resolveIntent(queryPlan, "UPCOMING_LIST"),
                "예약 조회 결과 " + items.size() + "건",
                items);
    }

    private ChatbotService.QueryBlock tripBlock(
            Integer userId,
            ChatbotRequest.ContextDTO context,
            ChatbotService.QueryPlan queryPlan) {
        List<TripPlan> tripPlans = findTrips(userId, context, queryPlan);
        List<Map<String, Object>> items = tripPlans.stream()
                .map(this::toTripItem)
                .toList();
        return ChatbotService.QueryBlock.createQueryBlock(
                ChatbotService.Domain.TRIP,
                resolveIntent(queryPlan, "UPCOMING_LIST"),
                "여행 계획 조회 결과 " + items.size() + "건",
                items);
    }

    private ChatbotService.QueryBlock calendarBlock(
            Integer userId,
            ChatbotRequest.ContextDTO context,
            ChatbotService.QueryPlan queryPlan) {
        List<CalendarEvent> events = findEvents(userId, context, queryPlan);
        List<Map<String, Object>> items = events.stream()
                .map(this::toCalendarItem)
                .toList();
        return ChatbotService.QueryBlock.createQueryBlock(
                ChatbotService.Domain.CALENDAR,
                resolveIntent(queryPlan, "MONTH_LIST"),
                "캘린더 조회 결과 " + items.size() + "건",
                items);
    }

    private ChatbotService.QueryBlock boardBlock(ChatbotService.QueryPlan queryPlan) {
        List<Board> boards = findBoards(queryPlan);
        List<Map<String, Object>> items = boards.stream()
                .map(this::toBoardItem)
                .toList();
        return ChatbotService.QueryBlock.createQueryBlock(
                ChatbotService.Domain.BOARD,
                resolveIntent(queryPlan, "RECENT_LIST"),
                "게시글 조회 결과 " + items.size() + "건",
                items);
    }

    private List<Booking> findBookings(
            Integer userId,
            ChatbotRequest.ContextDTO context,
            ChatbotService.QueryPlan queryPlan) {
        int limit = queryPlan.getLimit();
        String intent = resolveIntent(queryPlan, "UPCOMING_LIST");
        String keyword = normalizeKeyword(queryPlan.getKeyword());
        LocalDate startDate = parseDateOrNull(queryPlan.getStartDate());
        LocalDate endDate = parseDateOrNull(queryPlan.getEndDate());
        Integer tripPlanId = context == null ? null : context.getTripPlanId();

        if ("TRIP_PLAN_BOOKINGS".equals(intent) && tripPlanId != null) {
            return em.createQuery("""
                    select b
                    from Booking b
                    join fetch b.tripPlan tp
                    where b.user.id = :userId
                      and tp.id = :tripPlanId
                    order by b.checkIn asc, b.id asc
                    """, Booking.class)
                    .setParameter("userId", userId)
                    .setParameter("tripPlanId", tripPlanId)
                    .setMaxResults(limit)
                    .getResultList();
        }
        if (startDate != null && endDate != null) {
            return em.createQuery("""
                    select b
                    from Booking b
                    join fetch b.tripPlan tp
                    where b.user.id = :userId
                      and b.checkIn <= :endDate
                      and b.checkOut >= :startDate
                    order by b.checkIn asc, b.id asc
                    """, Booking.class)
                    .setParameter("userId", userId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setMaxResults(limit)
                    .getResultList();
        }
        if (keyword != null) {
            return em.createQuery("""
                    select b
                    from Booking b
                    join fetch b.tripPlan tp
                    where b.user.id = :userId
                      and (
                           lower(b.lodgingName) like :keyword
                        or lower(b.roomName) like :keyword
                        or lower(tp.title) like :keyword
                      )
                    order by b.createdAt desc, b.id desc
                    """, Booking.class)
                    .setParameter("userId", userId)
                    .setParameter("keyword", "%" + keyword + "%")
                    .setMaxResults(limit)
                    .getResultList();
        }
        if ("RECENT_LIST".equals(intent)) {
            return em.createQuery("""
                    select b
                    from Booking b
                    join fetch b.tripPlan tp
                    where b.user.id = :userId
                    order by b.createdAt desc, b.id desc
                    """, Booking.class)
                    .setParameter("userId", userId)
                    .setMaxResults(limit)
                    .getResultList();
        }
        return em.createQuery("""
                select b
                from Booking b
                join fetch b.tripPlan tp
                where b.user.id = :userId
                  and b.checkIn >= :today
                order by b.checkIn asc, b.id asc
                """, Booking.class)
                .setParameter("userId", userId)
                .setParameter("today", LocalDate.now())
                .setMaxResults(limit)
                .getResultList();
    }

    private List<TripPlan> findTrips(
            Integer userId,
            ChatbotRequest.ContextDTO context,
            ChatbotService.QueryPlan queryPlan) {
        int limit = queryPlan.getLimit();
        String intent = resolveIntent(queryPlan, "UPCOMING_LIST");
        String keyword = normalizeKeyword(queryPlan.getKeyword());
        LocalDate startDate = parseDateOrNull(queryPlan.getStartDate());
        LocalDate endDate = parseDateOrNull(queryPlan.getEndDate());
        Integer tripPlanId = context == null ? null : context.getTripPlanId();

        if ("CURRENT_TRIP".equals(intent) && tripPlanId != null) {
            return em.createQuery("""
                    select tp
                    from TripPlan tp
                    where tp.user.id = :userId
                      and tp.id = :tripPlanId
                    """, TripPlan.class)
                    .setParameter("userId", userId)
                    .setParameter("tripPlanId", tripPlanId)
                    .setMaxResults(1)
                    .getResultList();
        }
        if (startDate != null && endDate != null) {
            return em.createQuery("""
                    select tp
                    from TripPlan tp
                    where tp.user.id = :userId
                      and tp.startDate <= :endDate
                      and tp.endDate >= :startDate
                    order by tp.startDate asc, tp.id asc
                    """, TripPlan.class)
                    .setParameter("userId", userId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setMaxResults(limit)
                    .getResultList();
        }
        if (keyword != null) {
            return em.createQuery("""
                    select tp
                    from TripPlan tp
                    where tp.user.id = :userId
                      and lower(tp.title) like :keyword
                    order by tp.createdAt desc, tp.id desc
                    """, TripPlan.class)
                    .setParameter("userId", userId)
                    .setParameter("keyword", "%" + keyword + "%")
                    .setMaxResults(limit)
                    .getResultList();
        }
        if ("RECENT_LIST".equals(intent)) {
            return em.createQuery("""
                    select tp
                    from TripPlan tp
                    where tp.user.id = :userId
                    order by tp.createdAt desc, tp.id desc
                    """, TripPlan.class)
                    .setParameter("userId", userId)
                    .setMaxResults(limit)
                    .getResultList();
        }
        return em.createQuery("""
                select tp
                from TripPlan tp
                where tp.user.id = :userId
                  and tp.startDate >= :today
                order by tp.startDate asc, tp.id asc
                """, TripPlan.class)
                .setParameter("userId", userId)
                .setParameter("today", LocalDate.now())
                .setMaxResults(limit)
                .getResultList();
    }

    private List<CalendarEvent> findEvents(
            Integer userId,
            ChatbotRequest.ContextDTO context,
            ChatbotService.QueryPlan queryPlan) {
        int limit = queryPlan.getLimit();
        String intent = resolveIntent(queryPlan, "MONTH_LIST");
        String keyword = normalizeKeyword(queryPlan.getKeyword());
        LocalDate startDate = parseDateOrNull(queryPlan.getStartDate());
        LocalDate endDate = parseDateOrNull(queryPlan.getEndDate());
        Integer tripPlanId = context == null ? null : context.getTripPlanId();

        if ("TRIP_PLAN_EVENTS".equals(intent) && tripPlanId != null) {
            return em.createQuery("""
                    select e
                    from CalendarEvent e
                    left join fetch e.tripPlan tp
                    where e.user.id = :userId
                      and tp.id = :tripPlanId
                    order by e.startAt asc, e.id asc
                    """, CalendarEvent.class)
                    .setParameter("userId", userId)
                    .setParameter("tripPlanId", tripPlanId)
                    .setMaxResults(limit)
                    .getResultList();
        }
        if (keyword != null) {
            return em.createQuery("""
                    select e
                    from CalendarEvent e
                    left join fetch e.tripPlan tp
                    where e.user.id = :userId
                      and (
                           lower(e.title) like :keyword
                        or lower(coalesce(e.memo, '')) like :keyword
                      )
                    order by e.startAt asc, e.id asc
                    """, CalendarEvent.class)
                    .setParameter("userId", userId)
                    .setParameter("keyword", "%" + keyword + "%")
                    .setMaxResults(limit)
                    .getResultList();
        }
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today.withDayOfMonth(1);
            endDate = today.withDayOfMonth(today.lengthOfMonth());
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);
        return em.createQuery("""
                select e
                from CalendarEvent e
                left join fetch e.tripPlan tp
                where e.user.id = :userId
                  and e.startAt <= :endDateTime
                  and e.endAt >= :startDateTime
                order by e.startAt asc, e.id asc
                """, CalendarEvent.class)
                .setParameter("userId", userId)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .setMaxResults(limit)
                .getResultList();
    }

    private List<Board> findBoards(ChatbotService.QueryPlan queryPlan) {
        int limit = queryPlan.getLimit();
        String keyword = normalizeKeyword(queryPlan.getKeyword());
        BoardCategory category = BoardCategory.fromCodeOrNull(queryPlan.getCategory());
        String intent = resolveIntent(queryPlan, "RECENT_LIST");

        if ("CATEGORY_SEARCH".equals(intent) && category != null) {
            return em.createQuery("""
                    select b
                    from Board b
                    join fetch b.user u
                    where b.category = :category
                    order by b.createdAt desc, b.id desc
                    """, Board.class)
                    .setParameter("category", category)
                    .setMaxResults(limit)
                    .getResultList();
        }
        if (keyword != null) {
            return em.createQuery("""
                    select b
                    from Board b
                    join fetch b.user u
                    where lower(b.title) like :keyword
                       or lower(cast(b.content as string)) like :keyword
                    order by b.createdAt desc, b.id desc
                    """, Board.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .setMaxResults(limit)
                    .getResultList();
        }
        return em.createQuery("""
                select b
                from Board b
                join fetch b.user u
                order by b.createdAt desc, b.id desc
                """, Board.class)
                .setMaxResults(limit)
                .getResultList();
    }

    private Map<String, Object> toBookingItem(Booking booking) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", booking.getId());
        item.put("lodgingName", booking.getLodgingName());
        item.put("roomName", booking.getRoomName());
        item.put("checkIn", stringify(booking.getCheckIn()));
        item.put("checkOut", stringify(booking.getCheckOut()));
        item.put("status", booking.getStatusLabel());
        item.put("tripTitle", booking.getTripPlan().getTitle());
        item.put("region", booking.getLocation());
        return item;
    }

    private Map<String, Object> toTripItem(TripPlan tripPlan) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", tripPlan.getId());
        item.put("title", tripPlan.getTitle());
        item.put("region", tripPlan.getRegionLabel());
        item.put("companion", tripPlan.getWhoWithLabel());
        item.put("startDate", stringify(tripPlan.getStartDate()));
        item.put("endDate", stringify(tripPlan.getEndDate()));
        return item;
    }

    private Map<String, Object> toCalendarItem(CalendarEvent event) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", event.getId());
        item.put("title", event.getTitle());
        item.put("startAt", stringify(event.getStartAt()));
        item.put("endAt", stringify(event.getEndAt()));
        item.put("eventType", event.getEventTypeCode());
        item.put("tripTitle", event.getTripPlan() == null ? null : event.getTripPlan().getTitle());
        item.put("memo", summarize(event.getMemo()));
        return item;
    }

    private Map<String, Object> toBoardItem(Board board) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", board.getId());
        item.put("title", board.getTitle());
        item.put("category", board.getCategory() == null ? null : board.getCategory().getLabel());
        item.put("author", board.getUser().getUsername());
        item.put("viewCount", board.getViewCount());
        item.put("createdAt", stringify(board.getCreatedAt()));
        return item;
    }

    private String resolveIntent(ChatbotService.QueryPlan queryPlan, String defaultIntent) {
        if (queryPlan.getIntent() == null || queryPlan.getIntent().isBlank()) {
            return defaultIntent;
        }
        return queryPlan.getIntent();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase();
    }

    private LocalDate parseDateOrNull(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(rawDate.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String stringify(Object value) {
        return value == null ? null : value.toString();
    }

    private String summarize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() <= 80) {
            return value;
        }
        return value.substring(0, 80) + "...";
    }
}
