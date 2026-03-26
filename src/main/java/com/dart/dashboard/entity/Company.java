package com.dart.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity // DB 테이블이랑 연결될 도면
@Table(name = "COMPANIES") // MySQL에 있는 'COMPANIES' 테이블이랑 연결
@Getter
@Setter
public class Company {

    @Id // 테이블의 Primary Key
    @Column(name = "corp_code", length = 8)
    private String corpCode; // DART 고유번호 (예: 00126380)

    @Column(name = "corp_name", nullable = false, length = 100)
    private String corpName; // 기업명 (예: 삼성전자)

    @Column(name = "stock_code", length = 6)
    private String stockCode; // 주식 종목코드 (예: 005930)

    private String revenue;         // 매출액
    
    private String operatingProfit; // 영업이익
}