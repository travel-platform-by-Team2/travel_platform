-- H2 seed data for core domain tables
insert into user_tb (username, password, email, created_at)
values ('ssar', '1234', 'ssar@nate.com', now());

insert into user_tb (username, password, email, created_at)
values ('cos', '1234', 'cos@nate.com', now());

-- 여행 계획
insert into trip_plan_tb (user_id, title, start_date, end_date, created_at)
values (1, '제주 2박 3일', date '2026-04-10', date '2026-04-12', now());

insert into trip_plan_tb (user_id, title, start_date, end_date, created_at)
values (2, '부산 1박 2일', date '2026-05-01', date '2026-05-02', now());

-- 여행 장소
insert into trip_place_tb (trip_plan_id, place_name, address, latitude, longitude, day_order)
values (1, '성산일출봉', '제주특별자치도 서귀포시 성산읍 성산리 1', 33.4588790, 126.9425580, 1);

insert into trip_place_tb (trip_plan_id, place_name, address, latitude, longitude, day_order)
values (1, '협재해수욕장', '제주특별자치도 제주시 한림읍 협재리', 33.3947600, 126.2393290, 2);

insert into trip_place_tb (trip_plan_id, place_name, address, latitude, longitude, day_order)
values (2, '해운대해수욕장', '부산광역시 해운대구 우동', 35.1586980, 129.1603840, 1);

-- 커뮤니티 게시글
insert into board_tb (user_id, category, title, content, view_count, like_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, 0, now());

insert into board_tb (user_id, category, title, content, view_count, like_count, created_at)
values (2, 'qna', '부산 당일치기 가능할까요?', '해운대 중심으로 일정을 짜보려는데 조언 부탁드립니다.', 5, 0, now());

-- 게시글 좋아요
insert into board_like_tb (board_id, user_id, created_at)
values (1, 2, now());

insert into board_like_tb (board_id, user_id, created_at)
values (2, 1, now());

-- 커뮤니티 댓글
insert into board_reply_tb (board_id, user_id, content, created_at) 
values (1, 2, '좋은 정보 감사합니다! 다음 주에 가볼게요.', now()); 

insert into board_reply_tb (board_id, user_id, content, created_at)
values (2, 1, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

insert into board_reply_tb (board_id, user_id, content, created_at)
values (1, 2, '가능하지만 이동 동선을 미리 정하시면 좋아요.', now());

-- 예약
insert into booking_tb (user_id, trip_plan_id, lodging_name, check_in, check_out, guest_count, total_price, created_at)
values (1, 1, '제주 오션뷰 호텔', date '2026-04-10', date '2026-04-12', 2, 320000, now());

insert into booking_tb (user_id, trip_plan_id, lodging_name, check_in, check_out, guest_count, total_price, created_at)
values (2, 2, '해운대 비치 호텔', date '2026-05-01', date '2026-05-02', 1, 140000, now());

-- 캘린더 일정
insert into calendar_event_tb (user_id, trip_plan_id, title, start_at, end_at, event_type)
values (1, 1, '제주 출발', timestamp '2026-04-10 08:00:00', timestamp '2026-04-10 10:00:00', 'TRIP');

insert into calendar_event_tb (user_id, trip_plan_id, title, start_at, end_at, event_type)
values (2, 2, '체크인', timestamp '2026-05-01 15:00:00', timestamp '2026-05-01 16:00:00', 'BOOKING');

-- map-detail 페이지용 지도/숙소 데이터
runscript from 'classpath:db/mapdata.sql';
