package com.example.parkinglot.entity;

public class SingleCheckData {

    public Response response;

    public class Response {
        public Header header;
        public Body body;
    }

    public class Header {
        public String resultCode;                       // 응답 메시지 코드
        public String resultMsg;                        // 응답 메시지 설명
    }

    public class Body {
        public String pageNo;                           // 페이지 번호
        public String numOfRows;                        // 한 페이지 결과 수
        public String totalCount;                       // 데이터 총 개수
    }
}