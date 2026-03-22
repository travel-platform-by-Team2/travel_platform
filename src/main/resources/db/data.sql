-- H2 seed data for core domain tables
insert into user_tb (username, password, email, tel, role, created_at, active)
values ('ssar', '1234', 'ssar@nate.com', '010-3333-4444', 'USER', now(), true);

insert into user_tb (username, password, email, tel, role, created_at, active)
values ('cos', '1234', 'cos@nate.com', '010-5555-6666', 'USER', now(), false);

insert into user_tb (username, password, email, tel, role, created_at, active)
values ('admin', '1234', 'admin@nate.com', '010-1111-2222', 'ADMIN', now(), true);


-- 여행 계획
insert into trip_plan_tb (user_id, title, start_date, end_date, created_at, img_url, region, who_with)
values (1, '제주 2박 3일', date '2026-04-10', date '2026-04-12', now(), 'https://lh3.googleusercontent.com/aida-public/AB6AXuDN0kqyZcFwdNEc-a6CCMDKpnpzxbfAUmAPFkAX3RwNlNcepNzVGzu0LVVOJqUyOIdJjo_eqOl9wMEd9LP5VSNREoO0Lef-esqF_C4P1l2xhs2XTLnsXlXc0ZSRpU2CYjyFHxQlNI7wOE5w8C95e7U8g91UWwemD07rPGmwD2nZltMUw3z2kfSZRMdnMTxzPxCmLpPf9a17cMoP4KKqZOaOuQOrnx0cpP_nqWwIQw9GKkKEH1GiccYRRDnWFSMFaASs-ixCCJuNu10', 'jeju', 'friend');

insert into trip_plan_tb (user_id, title, start_date, end_date, created_at, img_url, region, who_with)
values (1, '부산 1박 2일', date '2025-05-01', date '2025-05-02', now(), null, 'busan', 'solo');

insert into trip_plan_tb (user_id, title, start_date, end_date, created_at, img_url, region, who_with)
values (1, '서울 1박 2일', date '2025-05-01', date '2025-05-02', now(), null, 'seoul', 'family');

insert into trip_plan_tb (user_id, title, start_date, end_date, created_at, img_url, region, who_with)
values (1, '부산 1박 2일', date '2025-05-01', date '2025-05-02', now(), null, 'busan', 'couple');
     
-- 여행 장소
insert into trip_place_tb (trip_plan_id, place_name, address, latitude, longitude, trip_day)
values (1, '성산일출봉', '제주특별자치도 서귀포시 성산읍 성산리 1', 33.4588790, 126.9425580, 1);

insert into trip_place_tb (trip_plan_id, place_name, address, latitude, longitude, trip_day)
values (1, '협재해수욕장', '제주특별자치도 제주시 한림읍 협재리', 33.3947600, 126.2393290, 2);

insert into trip_place_tb (trip_plan_id, place_name, address, latitude, longitude, trip_day)
values (2, '해운대해수욕장', '부산광역시 해운대구 우동', 35.1586980, 129.1603840, 1);

-- 커뮤니티 게시글
insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 동쪽 하루 코스 짜봤어요', '아침엔 성산일출봉, 점심엔 섭지코지, 저녁은 세화 쪽으로 마무리하면 동선이 꽤 좋았습니다.', 128, timestamp '2026-01-05 08:30:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (2, 'qna', '부산 당일치기 가능할까요?', 'KTX로 오전 일찍 도착해서 해운대와 광안리만 보고 오려는데 너무 빡셀지 궁금합니다.', 67, timestamp '2026-01-07 10:15:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '전주 비빔밥집 두 군데 비교 후기', '관광객 많은 곳보다 한옥마을 바깥 골목 식당이 훨씬 만족스러웠고 반찬 구성이 정갈했습니다.', 94, timestamp '2026-01-09 12:40:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '눈 오는 강원도 여행 준비물 체크리스트', '핫팩이랑 방수 신발은 필수였고 차량 이용이면 성에 제거 스프레이도 꼭 챙기시는 걸 추천합니다.', 151, timestamp '2026-01-11 07:50:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '속초 오션뷰 숙소 솔직 후기', '사진보다 방은 조금 작았지만 창문으로 바다가 바로 보여서 새벽 풍경 하나는 정말 만족스러웠습니다.', 82, timestamp '2026-01-13 21:10:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '경주 1박 2일 뚜벅이 일정 공유', '불국사와 황리단길을 한날에 몰지 않고 첫날과 둘째날로 나누니 체력적으로 훨씬 편했습니다.', 56, timestamp '2026-01-15 09:20:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '부산 돼지국밥집 아침 오픈 시간 정리', '서면 쪽은 이른 시간에도 여는 집이 많아서 체크아웃하고 바로 식사하기 좋았습니다.', 73, timestamp '2026-01-18 08:05:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '여수 밤바다 포인트 추천 부탁드려요', '돌산대교 쪽이 나을지 해양공원 쪽이 나을지 고민이라 사진 찍기 좋은 위치 알려주세요.', 48, timestamp '2026-01-20 18:45:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '비 오는 날 서울 실내 여행 코스 추천', '박물관 하나만 넣기보다 북카페나 실내 전망대까지 같이 묶으면 하루가 금방 지나갑니다.', 111, timestamp '2026-01-22 11:00:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '서울역 근처 비즈니스 호텔 후기', '교통은 최고였지만 방음은 조금 아쉬웠고, 체크인 응대는 빨라서 일정이 촉박할 때 괜찮았습니다.', 64, timestamp '2026-01-24 22:30:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '남해 드라이브 코스 이렇게 돌았어요', '독일마을에서 시작해서 다랭이마을로 내려오는 루트가 바다 풍경 보기에 가장 만족스러웠습니다.', 90, timestamp '2026-01-27 16:10:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '강릉 커피거리보다 조용한 카페 찾는다면', '안목해변 메인 거리보다 한 블록 안쪽 카페들이 좌석 간격도 넓고 덜 붐볐습니다.', 52, timestamp '2026-01-30 14:25:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '렌터카 없이 제주 서쪽 여행 가능할까요?', '협재와 애월 쪽만 생각 중인데 버스로도 충분한지 실제 다녀오신 분들 의견 듣고 싶어요.', 88, timestamp '2026-02-01 09:55:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '숙소 체크인 전에 짐 보관할 때 팁', '역 근처 코인락커가 다 찼을 때는 관광안내소 제휴 보관 서비스를 찾으면 의외로 해결이 빠릅니다.', 135, timestamp '2026-02-03 13:35:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '대전 성심당 근처 숙소 묵어본 후기', '빵 사서 바로 숙소 들어가기 좋았고, 주변이 조용해서 가족 여행 숙소로 무난한 편이었습니다.', 41, timestamp '2026-02-05 20:05:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '여수 2박 3일 일정표 공유합니다', '첫날은 오동도와 해양공원, 둘째 날은 향일암, 마지막 날은 카페 위주로 잡으니 균형이 좋았습니다.', 77, timestamp '2026-02-08 07:25:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '제주 흑돼지보다 만족한 고등어회 집', '관광지 메인보다 현지인 추천 받은 식당이 신선도나 가격 면에서 훨씬 만족도가 높았습니다.', 119, timestamp '2026-02-10 19:50:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '부모님 모시고 가기 좋은 제주 코스 있을까요?', '계단 많은 곳은 피하고 싶고 바다 보면서 쉬기 좋은 장소가 많으면 좋겠습니다.', 58, timestamp '2026-02-12 10:40:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '혼자 여행할 때 사진 남기는 방법', '삼각대 못 챙겼을 때는 난간이나 벤치를 이용하고, 연사로 찍으면 생각보다 건질 사진이 많습니다.', 102, timestamp '2026-02-14 17:30:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '제주 공항 근처 1박 숙소 짧은 후기', '늦은 비행기 도착 후 하룻밤 묵기엔 괜찮았고, 주차 공간이 넉넉한 점이 가장 편했습니다.', 38, timestamp '2026-02-16 23:00:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '서울 근교 당일치기 봄꽃 코스 추천', '아침에 남양주 수목원 들렀다가 오후에 카페 거리 쪽으로 이동하면 걷기와 휴식이 적당히 섞입니다.', 86, timestamp '2026-02-19 08:10:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '통영에서 회 말고 먹을 만한 메뉴 추천', '충무김밥은 물론이고 멍게비빔밥이나 장어구이도 생각보다 만족도가 높았습니다.', 69, timestamp '2026-02-21 13:15:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '강릉 1박 2일이면 렌터카 필수인가요?', '뚜벅이로 카페랑 바다 위주 일정 생각 중인데 이동 시간이 너무 길지 않을지 고민됩니다.', 72, timestamp '2026-02-24 15:40:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '주말 여행에서 덜 피곤한 이동 팁', '첫날 욕심내지 말고 핵심 장소 두세 군데만 넣는 편이 결국 만족도가 높았습니다.', 143, timestamp '2026-02-26 09:05:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '부산 광안리 오션뷰 호텔 장단점', '야경은 정말 좋았지만 주말엔 주변 소음이 꽤 있어서 예민한 분들은 상층부를 요청하는 편이 좋겠습니다.', 95, timestamp '2026-03-01 22:55:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '전주와 군산 같이 묶는 2박 3일 코스', '한옥마을만 오래 머무르기보다 군산 근대거리까지 함께 넣으니 분위기가 확 달라져서 좋았습니다.', 61, timestamp '2026-03-03 11:30:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '속초 중앙시장 먹거리 솔직 정리', '닭강정은 줄이 길어도 회전이 빨랐고, 오징어순대는 식기 전에 바로 먹는 게 훨씬 맛있었습니다.', 88, timestamp '2026-03-05 18:25:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '아이와 함께 가기 좋은 서울 실내 장소 추천', '초등학생이 지루해하지 않을 곳이면 좋겠고, 주차가 편하면 더 좋겠습니다.', 44, timestamp '2026-03-07 10:20:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '새벽 비행기 탈 때 공항 근처 동선 팁', '짐이 많으면 첫날은 공항 근처에서 숙박하고 다음 날 본격적으로 이동하는 편이 훨씬 덜 피곤했습니다.', 126, timestamp '2026-03-09 06:45:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '경주 한옥 숙소 감성은 좋았지만', '마당과 분위기는 만족스러웠지만 겨울철엔 바닥 난방이 천천히 올라와서 두꺼운 잠옷이 필요했습니다.', 57, timestamp '2026-03-10 21:45:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '포항 바다 위주 1박 2일 일정 메모', '영일대 해수욕장과 스페이스워크, 호미곶을 나눠 담으니 이동 스트레스가 덜했습니다.', 71, timestamp '2026-03-12 09:15:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '제주 공항 근처 고기국수집 비교', '대기 줄이 짧은 집이 오히려 국물이 더 진했고, 공항 가기 전 한 끼로 만족스러웠습니다.', 63, timestamp '2026-03-13 12:05:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '울릉도 배 멀미 심한 편인가요?', '멀미약은 챙길 예정인데 평소 차 멀미 있는 편이라 실제 체감이 어떤지 궁금합니다.', 53, timestamp '2026-03-14 16:50:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '렌터카 반납 직전 주유할 때 팁', '공항 바로 앞 주유소만 찾지 말고 반납 지점 10분 전 구간을 검색하면 대기 없는 곳이 꽤 있습니다.', 117, timestamp '2026-03-15 08:35:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '여수 펜션 바비큐 포함 후기', '객실은 평범했지만 테라스에서 먹는 저녁 분위기가 좋았고 가족 단위 이용객이 많았습니다.', 47, timestamp '2026-03-15 20:40:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '춘천 당일치기 코스 이렇게 다녀왔어요', '소양강 스카이워크와 카페, 닭갈비 거리만 묶어도 하루 일정으로 충분히 알찼습니다.', 59, timestamp '2026-03-16 10:00:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '통영 충무김밥보다 기억에 남은 집', '작은 분식집에서 먹은 우동 국물이 의외로 훨씬 인상적이었고 가격도 부담이 적었습니다.', 66, timestamp '2026-03-16 13:55:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '부산역에서 바로 갈 수 있는 바다 추천', '택시 오래 타지 않고도 바다를 빨리 보고 싶은데 어디가 가장 접근성이 좋을까요?', 37, timestamp '2026-03-16 18:20:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '사진 찍기 좋은 시간대는 결국 아침이더라', '사람 없는 장면을 원하면 조금만 부지런해져도 결과물이 완전히 달라졌습니다.', 109, timestamp '2026-03-17 06:30:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '가평 감성 숙소 후기 남깁니다', '인테리어는 예뻤지만 계단이 많아서 짐이 많으면 조금 불편할 수 있겠다는 생각이 들었습니다.', 42, timestamp '2026-03-17 22:10:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '서울 야간 드라이브 코스 공유', '북악스카이웨이와 한강변을 묶어 돌면 짧은 시간에도 분위기 있게 다녀올 수 있습니다.', 84, timestamp '2026-03-18 00:20:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '경주 황리단길에서 웨이팅 적었던 맛집', '메인 거리 끝쪽으로 조금만 걸어가면 훨씬 여유 있게 식사할 수 있는 곳들이 있었습니다.', 58, timestamp '2026-03-18 11:10:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '여행 일정표를 너무 빡빡하게 짜는 편인데', '하루에 명소 네다섯 군데 넣는 습관이 있는데 실제로는 두세 군데가 적당한지 궁금합니다.', 49, timestamp '2026-03-18 14:35:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '여행 후반 일정은 비워두는 게 좋았어요', '첫날부터 끝날 때까지 촘촘하게 잡는 것보다 마지막 반나절은 여유 있게 남기는 편이 만족도가 높았습니다.', 121, timestamp '2026-03-18 16:05:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '제주 서귀포 가족 숙소 후기', '조식은 무난했지만 수영장 관리가 깔끔해서 아이 동반 여행에는 꽤 만족스러운 편이었습니다.', 54, timestamp '2026-03-18 18:50:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '강릉에서 속초 넘어가는 일정 어떤가요?', '오전엔 강릉 카페와 바다, 오후엔 속초 시장과 야경으로 잡아봤는데 이동이 빡센지 고민입니다.', 74, timestamp '2026-03-18 20:00:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '부산 밀면집 여름 시즌 후기', '줄은 길었지만 회전이 빨랐고 양념장을 다 넣기보다 조금씩 섞는 편이 더 깔끔했습니다.', 80, timestamp '2026-03-18 21:15:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '제주 우도 들어갈 때 차량 선적 필요할까요?', '아이랑 함께라 짐이 좀 많은데 성수기 아니라면 차 없이 들어가도 괜찮은지 궁금합니다.', 62, timestamp '2026-03-18 21:55:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '체크아웃 날엔 카페보다 산책 코스 추천', '짐 정리하고 이동할 시간이 필요해서 체크아웃 날은 오래 앉는 코스보다 짧은 산책이 훨씬 편했습니다.', 97, timestamp '2026-03-18 22:10:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '해운대 레지던스형 숙소 후기', '취사가 가능해서 편했지만 주차장이 다소 좁아 늦게 들어오면 자리가 부족할 수 있었습니다.', 45, timestamp '2026-03-18 22:30:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '서울에서 전주 가는 주말 일정 공유', '금요일 저녁 출발해서 토요일은 한옥마을, 일요일은 남부시장 위주로 잡으니 무리가 없었습니다.', 68, timestamp '2026-03-18 22:50:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '광주 떡갈비 골목 첫 방문 후기', '유명한 집 말고도 주변에 괜찮은 곳이 많아서 웨이팅 긴 곳만 고집할 필요는 없었습니다.', 57, timestamp '2026-03-18 23:05:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '혼자 부산 여행하면 숙소는 어디가 좋을까요?', '해운대와 서면 중에서 밤에도 이동 편하고 식당 찾기 쉬운 곳으로 고민 중입니다.', 51, timestamp '2026-03-18 23:20:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '여행 사진 정리는 돌아오는 날 바로', '며칠 미루면 정말 손대기 싫어져서 공항이나 기차 안에서 바로 1차 정리하는 습관이 좋았습니다.', 132, timestamp '2026-03-18 23:35:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '제주 애월 감성 숙소 재방문 후기', '지난번보다 객실 컨디션이 더 좋아졌고, 카페 거리 접근성이 좋아 저녁 산책하기 편했습니다.', 79, timestamp '2026-03-18 23:45:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '봄철 경주 벚꽃 코스 추천', '보문단지와 대릉원 쪽을 시간차 두고 돌면 사람 많은 구간을 조금 피할 수 있었습니다.', 92, timestamp '2026-03-18 23:50:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '서울 성수 브런치 카페 평일 방문 팁', '오픈 직후보다 오히려 점심 직전 시간이 한산해서 오래 기다리지 않고 들어갈 수 있었습니다.', 84, timestamp '2026-03-18 23:52:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '속초 시장은 몇 시쯤 가야 덜 붐빌까요?', '저녁 시간대만 피하면 되는지, 아니면 아예 오픈 초반에 가는 게 나은지 궁금합니다.', 39, timestamp '2026-03-18 23:54:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '짐 줄이려면 겉옷 하나가 중요해요', '레이어드 가능한 얇은 겉옷 한 벌만 잘 챙겨도 여행 가방 부피가 확 줄었습니다.', 118, timestamp '2026-03-18 23:56:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '강릉 바다 보이는 숙소 후기 남겨요', '해변과 매우 가까워 산책은 좋았지만 바람 소리가 꽤 들려 예민하면 창문 방향을 확인하는 편이 좋겠습니다.', 60, timestamp '2026-03-18 23:57:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '주말 부산 먹방 위주 일정 짜봤어요', '관광보다 식당과 카페 중심으로 잡으니 이동 반경을 좁게 두는 게 훨씬 만족도가 높았습니다.', 76, timestamp '2026-03-18 23:58:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'food', '제주 시장 회포장 어디가 괜찮았나요?', '숙소에서 간단히 먹으려고 하는데 양이 넉넉하고 포장 깔끔했던 곳이 있으면 추천 부탁드립니다.', 71, timestamp '2026-03-18 23:59:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'qna', '여행 마지막 날 반나절 코스는 어떻게 짜세요?', '숙소 체크아웃 이후 이동 시간 때문에 애매한데 보통 카페만 들르는 편인지 궁금합니다.', 46, timestamp '2026-03-19 00:01:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'tips', '비행기 지연될 때 일정 수정 기준', '첫 예약 하나만 고정하고 나머지는 유동적으로 두면 예상치 못한 지연에도 스트레스를 덜 받았습니다.', 124, timestamp '2026-03-19 00:03:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'review', '서울 도심 호캉스 숙소 장단점 정리', '주변 편의시설은 최고였지만 주말 가격이 확 올라가서 할인 시점에 예약하는 게 중요했습니다.', 87, timestamp '2026-03-19 00:05:00');

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '비 오는 날 제주 일정 바꿔본 후기', '오름 대신 실내 전시와 카페를 넣었는데 생각보다 훨씬 여유롭고 만족스러운 하루가 됐습니다.', 98, timestamp '2026-03-19 00:08:00');
insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, now());

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, now());

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, now());

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, now());

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, now());

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, now());

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, now());

insert into board_tb (user_id, category, title, content, view_count, created_at)
values (1, 'plan', '제주 여행 코스 추천', '성산일출봉과 우도 코스를 추천합니다.', 12, now());


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

-- 예약
insert into booking_tb (user_id, trip_plan_id, lodging_name, room_name, check_in, check_out, guest_count, price_per_night, tax_and_service_fee, region_key, image_url, status, cancelled_at, created_at)
values (1, 1, '제주 오션뷰 호텔', '오션뷰 스탠다드', date '2026-04-10', date '2026-04-12', 2, 280000, 50400, 'jeju', null, 'BOOKED', null, now());

insert into booking_tb (user_id, trip_plan_id, lodging_name, room_name, check_in, check_out, guest_count, price_per_night, tax_and_service_fee, region_key, image_url, status, cancelled_at, created_at)
values (2, 2, '해운대 비치 호텔', '시티 더블', date '2025-05-01', date '2025-05-02', 1, 140000, 25200, 'busan', null, 'BOOKED', null, now());

-- 캘린더 일정
insert into calendar_event_tb (user_id, trip_plan_id, title, start_at, end_at, event_type, memo)
values (1, 1, '제주 출발', timestamp '2026-04-10 08:00:00', timestamp '2026-04-10 10:00:00', 'TRIP', '공항 출발 일정');

insert into calendar_event_tb (user_id, trip_plan_id, title, start_at, end_at, event_type, memo)
values (2, 2, '체크인', timestamp '2026-05-01 15:00:00', timestamp '2026-05-01 16:00:00', 'BOOKING', '숙소 체크인');

-- map-detail 페이지용 지도/숙소 데이터
runscript from 'classpath:db/mapdata.sql';
