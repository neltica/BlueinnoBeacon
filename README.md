# BlueinnoBeacon

##Summary
>블루이노에 BLE모듈(HM-10)을 추가로 연결하여 블루이노와 스마트폰 사이의 거리를 확인하고 블루이노에 연결된 도트 메트릭스를 이용해서 거리에 따라 이모티콘을 변경하는 외주 프로젝트

##TODO List
- [x] 도트메트릭스 동작 확인
- [x] 비콘 동작확인 (앱은 앱스토어에서 다운받아 테스트)
- [x] 앱 레이아웃 및 기초 작업 진행
- [x] 비콘 라이브러리 자료 수집
- [x] 비콘 라이브러리 선택 및 사용 (AltBeacon Library)
- [x] 비콘 거리 미터로 표시                                                    
- [x] 이모티콘 정보 스마트폰안에 저장 (FILE I/O)
- [x] 비콘으로 동작하는 블루이노랑 안드로이드랑 데이터 송수신이 가능한지 테스트  =>불가능,BLE모듈또는 Bluetooth모듈 추가로 블루이노에 연결
- [x] HM-10블루이노에 연결(안드로이드로부터 이모티콘데이터, 이모티콘 변경 명령 수신용)
- [x] 안드로이드에서 이모티콘을 직접 만들수 있고 이를 블루이노로 전송
- [x] 그래프 표현 (HzGrapher Library)

##The goal
>* 블루이노 비콘모드로 동작
>* 안드로이드에서 비콘거리 수신
>* 안드로이드에서 비콘거리 그래프로 표현
>* 안드로이드에서 이모티콘 수정기능 구현
>* HM-10 안드로이드와 페이링되면 안드로이드로부터 이모티콘을 수신받아 블루이노에 이모티콘 데이터 초기화
>* HM-10이 안드로이드로 부터 수신한 거리데이터를 기준으로 블루이노가 도트메트릭스의 이모티콘을 변경

##Environment
>* Android
>* Bluinno2
>* 8X8 Dot Matrix
>* HM-10 (BLE)

##Develop Tool
>* Android studio
>* Arduino IDE v1.5.8 <-해당 버전이 블루이노가 가장 잘 동작함.(공식카페 글 참조)

##Library
>* HzGrapher library (그래프 라이브러리)
>* Bluinno library  (블루이노 라이브러리)
>* LedControl  (도트메트릭스 사용을 위한 LED 라이브러리)


##Implementation
###8X8 Dot Matrix Layout

##Link
>* Blueinno library download:  http://cafe.naver.com/arduinoplusble
>* LedControl library download:
>* HzGrapher library download:



