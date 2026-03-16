-- =========================================================
-- Travel Platform MySQL 전체 초기화 스크립트
-- - DB 생성
-- - 관련 테이블 전체 삭제
-- - 스키마 재생성(엔티티 기준)
-- - 시드 데이터 입력(data.sql + mapdata.sql + 숙소 샘플)
-- =========================================================

create database if not exists travel_platform
  character set utf8mb4
  collate utf8mb4_unicode_ci;

use travel_platform;
set names utf8mb4;

-- -----------------------------
-- 1) 초기화(테이블 삭제)
-- -----------------------------
set foreign_key_checks = 0;

drop table if exists lodging_image_tb;
drop table if exists lodging_tb;
drop table if exists map_place_image_tb;

drop table if exists calendar_event_tb;
drop table if exists booking_tb;
drop table if exists board_reply_tb;
drop table if exists board_tb;
drop table if exists trip_place_tb;
drop table if exists trip_plan_tb;
drop table if exists user_tb;

set foreign_key_checks = 1;

-- -----------------------------
-- 2) 기본 테이블 생성(엔티티 기준)
-- -----------------------------
create table user_tb (
  id int auto_increment primary key,
  username varchar(255) not null unique,
  password varchar(100) not null,
  email varchar(255),
  created_at datetime not null default current_timestamp
);

create table trip_plan_tb (
  id int auto_increment primary key,
  user_id int not null,
  title varchar(100) not null,
  start_date date not null,
  end_date date not null,
  created_at datetime not null default current_timestamp,
  img_url varchar(500) not null,
  constraint fk_trip_plan_user foreign key (user_id) references user_tb(id)
);

create table trip_place_tb (
  id int auto_increment primary key,
  trip_plan_id int not null,
  place_name varchar(100) not null,
  address varchar(255),
  latitude decimal(10,7),
  longitude decimal(10,7),
  day_order int not null,
  constraint fk_trip_place_plan foreign key (trip_plan_id) references trip_plan_tb(id)
);

create table board_tb (
  id int auto_increment primary key,
  user_id int not null,
  category varchar(20) not null,
  title varchar(150) not null,
  content longtext not null,
  view_count int not null default 0,
  like_count int not null default 0,
  created_at datetime not null default current_timestamp,
  constraint fk_board_user foreign key (user_id) references user_tb(id)
);

create table board_reply_tb (
  id int auto_increment primary key,
  board_id int not null,
  user_id int not null,
  content longtext not null,
  created_at datetime not null default current_timestamp,
  constraint fk_board_reply_board foreign key (board_id) references board_tb(id),
  constraint fk_board_reply_user foreign key (user_id) references user_tb(id)
);

create table board_like_tb (
  id int auto_increment primary key,
  board_id int not null,
  user_id int not null,
  created_at timestamp not null default current_timestamp,
  constraint uk_board_like_board_user unique (board_id, user_id),
  constraint fk_board_like_board foreign key (board_id) references board_tb(id) on delete cascade,
  constraint fk_board_like_user foreign key (user_id) references user_tb(id) on delete cascade
);

-- 조회 빠르게 해주는 코드 넣어도 빼도 상관없음
create index idx_board_like_board_id on board_like_tb(board_id);
create index idx_board_like_user_id on board_like_tb(user_id);

create table booking_tb (
  id int auto_increment primary key,
  user_id int not null,
  trip_plan_id int not null,
  lodging_name varchar(120) not null,
  check_in date not null,
  check_out date not null,
  guest_count int not null,
  total_price int not null,
  created_at datetime not null default current_timestamp,
  constraint fk_booking_user foreign key (user_id) references user_tb(id),
  constraint fk_booking_plan foreign key (trip_plan_id) references trip_plan_tb(id)
);

create table calendar_event_tb (
  id int auto_increment primary key,
  user_id int not null,
  trip_plan_id int null,
  title varchar(120) not null,
  start_at datetime not null,
  end_at datetime not null,
  event_type varchar(50) not null,
  constraint fk_calendar_event_user foreign key (user_id) references user_tb(id),
  constraint fk_calendar_event_plan foreign key (trip_plan_id) references trip_plan_tb(id)
);

-- -----------------------------
-- 3) 기존 지도 이미지 캐시 테이블(mapdata.sql 기준)
-- -----------------------------
create table map_place_image_tb (
  id bigint auto_increment primary key,
  normalized_name varchar(200) not null unique,
  place_name varchar(200) not null,
  image_url varchar(2000) not null,
  source varchar(50) not null,
  created_at datetime not null default current_timestamp
);

-- -----------------------------
-- 4) 숙소 테이블(지도/체크아웃 DB 연동용)
-- -----------------------------
create table lodging_tb (
  id bigint auto_increment primary key,
  external_place_id varchar(64) not null unique,
  name varchar(200) not null,
  normalized_name varchar(200) not null,
  category_group_code varchar(10) not null, -- 숙소 카테고리 코드(예: AD5)
  category_name varchar(300) null,
  phone varchar(50) null,
  address varchar(300) null,
  road_address varchar(300) null,
  region_key varchar(50) not null, -- 지역 키(seoul, busan, jeju...)
  lat decimal(10,7) not null,
  lng decimal(10,7) not null,
  place_url varchar(500) null,
  room_price int not null default 0,
  fee int not null default 0,
  is_active tinyint(1) not null default 1,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  index idx_lodging_region_active (region_key, is_active),
  index idx_lodging_geo (lat, lng),
  index idx_lodging_name (normalized_name)
);

create table lodging_image_tb (
  id bigint auto_increment primary key,
  lodging_id bigint not null,
  image_url varchar(2000) not null,
  image_type varchar(30) not null default 'INTERIOR', -- 이미지 유형(INTERIOR / EXTERIOR / THUMBNAIL)
  sort_order int not null default 0,
  source varchar(50) not null default 'MANUAL',       -- 수집 출처(KAKAO / MANUAL / SEED)
  is_active tinyint(1) not null default 1,
  created_at datetime not null default current_timestamp,
  unique key uk_lodging_image (lodging_id, image_url(255)),
  index idx_lodging_image_order (lodging_id, sort_order),
  constraint fk_lodging_image_lodging foreign key (lodging_id) references lodging_tb(id)
);

-- -----------------------------
-- 5) 시드 데이터 입력(data.sql 기준)
-- -----------------------------
insert into user_tb (username, password, email, created_at) values
('ssar', '1234', 'ssar@nate.com', now()),
('cos', '1234', 'cos@nate.com', now());

insert into trip_plan_tb (user_id, title, start_date, end_date, created_at) values
(1, '제주 2박 3일', '2026-04-10', '2026-04-12', now()),
(2, '부산 1박 2일', '2026-05-01', '2026-05-02', now());

insert into trip_place_tb (trip_plan_id, place_name, address, latitude, longitude, day_order) values
(1, '성산일출봉', '제주특별자치도 서귀포시 성산읍 성산리 1', 33.4588790, 126.9425580, 1),
(1, '협재해수욕장', '제주특별자치도 제주시 한림읍 협재리', 33.3947600, 126.2393290, 2),
(2, '해운대해수욕장', '부산광역시 해운대구 우동', 35.1586980, 129.1603840, 1);

insert into board_tb (user_id, title, content, view_count, created_at) values
(1, '제주 여행 코스 추천', '성산일출봉과 해안도로 코스를 추천합니다.', 12, now()),
(2, '부산 뚜벅이 가능할까요?', '해운대 중심으로 일정 짜보는데 조언 부탁드립니다.', 5, now());

insert into board_reply_tb (board_id, user_id, content, created_at) values
(1, 2, '좋은 정보 감사합니다. 다음 주에 가볼게요!', now()),
(2, 1, '가능하지만 이동 동선은 미리 짜시면 좋아요.', now());

insert into booking_tb (user_id, trip_plan_id, lodging_name, check_in, check_out, guest_count, total_price, created_at) values
(1, 1, '제주 오션뷰 호텔', '2026-04-10', '2026-04-12', 2, 320000, now()),
(2, 2, '해운대 비치 호텔', '2026-05-01', '2026-05-02', 1, 140000, now());

insert into calendar_event_tb (user_id, trip_plan_id, title, start_at, end_at, event_type) values
(1, 1, '제주 출발', '2026-04-10 08:00:00', '2026-04-10 10:00:00', 'TRIP'),
(2, 2, '체크인', '2026-05-01 15:00:00', '2026-05-01 16:00:00', 'BOOKING');

-- -----------------------------
-- 6) 시드 데이터 입력(mapdata.sql 기준, UTF-8 보정)
-- -----------------------------
insert into map_place_image_tb (normalized_name, place_name, image_url, source, created_at) values
('감천문화마을', '감천문화마을', 'https://upload.wikimedia.org/wikipedia/commons/7/70/Gamcheon_Culture_Village.jpg', 'SEED', now()),
('파라다이스호텔부산', '파라다이스 호텔 부산', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80', 'SEED', now()),
('시그니엘부산', '시그니엘 부산', 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80', 'SEED', now()),
('해운대해수욕장', '해운대해수욕장', 'https://upload.wikimedia.org/wikipedia/commons/a/a8/Haeundae_Beach_Busan.jpg', 'SEED', now()),
('광안리해수욕장', '광안리해수욕장', 'https://upload.wikimedia.org/wikipedia/commons/5/5f/Gwangan_Bridge_2015.jpg', 'SEED', now()),
('부산타워', '부산타워', 'https://upload.wikimedia.org/wikipedia/commons/2/27/Busan_Tower_at_Yongdusan_Park.jpg', 'SEED', now()),
('송도해상케이블카', '송도해상케이블카', 'https://upload.wikimedia.org/wikipedia/commons/0/05/Songdo_Beach%2C_Busan.jpg', 'SEED', now()),
('태종대', '태종대', 'https://upload.wikimedia.org/wikipedia/commons/3/33/Taejongdae_Resort_Park_Busan.jpg', 'SEED', now()),
('부산시민공원', '부산시민공원', 'https://upload.wikimedia.org/wikipedia/commons/7/75/Busan_Citizen_Park.jpg', 'SEED', now()),
('아쿠아리움', '씨라이프부산아쿠아리움', 'https://upload.wikimedia.org/wikipedia/commons/4/49/Aquarium_tunnel.jpg', 'SEED', now()),
('국립해양박물관', '국립해양박물관', 'https://upload.wikimedia.org/wikipedia/commons/8/85/National_Maritime_Museum_of_Korea.jpg', 'SEED', now()),
('롯데월드어드벤처부산', '롯데월드어드벤처부산', 'https://upload.wikimedia.org/wikipedia/commons/f/f9/Theme_park_roller_coaster.jpg', 'SEED', now()),
('경복궁', '경복궁', 'https://upload.wikimedia.org/wikipedia/commons/8/8a/Gyeongbokgung-Geunjeongjeon.jpg', 'SEED', now()),
('북촌한옥마을', '북촌한옥마을', 'https://upload.wikimedia.org/wikipedia/commons/b/b7/Bukchon_Hanok_Village.jpg', 'SEED', now()),
('인사동거리', '인사동거리', 'https://upload.wikimedia.org/wikipedia/commons/e/e6/Insadong_Gil.jpg', 'SEED', now()),
('창덕궁', '창덕궁', 'https://upload.wikimedia.org/wikipedia/commons/c/cd/Changdeokgung_Injeongjeon.jpg', 'SEED', now());

-- -----------------------------
-- 7) 숙소 샘플 데이터
-- -----------------------------
insert into lodging_tb
  (external_place_id, name, normalized_name, category_group_code, category_name, phone, address, road_address, region_key, lat, lng, place_url, room_price, fee, is_active)
values
  ('kakao_10001', '파라다이스 호텔 부산', '파라다이스호텔부산', 'AD5', '숙박 > 호텔', '051-000-0001', '부산 해운대구', '부산 해운대구 해운대해변로 296', 'busan', 35.1586970, 129.1603840, 'https://place.map.kakao.com/10001', 392500, 70650, 1),
  ('kakao_10002', '시그니엘 부산', '시그니엘부산', 'AD5', '숙박 > 호텔', '051-000-0002', '부산 해운대구', '부산 해운대구 달맞이길 30', 'busan', 35.1587600, 129.1667410, 'https://place.map.kakao.com/10002', 397450, 71541, 1),
  ('kakao_20001', '서울 롯데 호텔', '서울롯데호텔', 'AD5', '숙박 > 호텔', '02-000-0001', '서울 중구', '서울 중구 을지로 30', 'seoul', 37.5651000, 126.9810000, 'https://place.map.kakao.com/20001', 350000, 63000, 1),
  ('kakao_30001', '제주 오션뷰 호텔', '제주오션뷰호텔', 'AD5', '숙박 > 호텔', '064-000-0001', '제주 제주시', '제주 제주시 애월해안로 100', 'jeju', 33.4627000, 126.3094000, 'https://place.map.kakao.com/30001', 280000, 50400, 1);

insert into lodging_image_tb (lodging_id, image_url, image_type, sort_order, source, is_active)
select id, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80', 'INTERIOR', 0, 'SEED', 1
from lodging_tb where external_place_id = 'kakao_10001';

insert into lodging_image_tb (lodging_id, image_url, image_type, sort_order, source, is_active)
select id, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80', 'INTERIOR', 0, 'SEED', 1
from lodging_tb where external_place_id = 'kakao_10002';

insert into lodging_image_tb (lodging_id, image_url, image_type, sort_order, source, is_active)
select id, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=80', 'INTERIOR', 0, 'SEED', 1
from lodging_tb where external_place_id = 'kakao_20001';

insert into lodging_image_tb (lodging_id, image_url, image_type, sort_order, source, is_active)
select id, 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80', 'INTERIOR', 0, 'SEED', 1
from lodging_tb where external_place_id = 'kakao_30001';
